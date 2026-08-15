package cm.univ.library.loan;

import cm.univ.library.catalog.BookCopy;
import cm.univ.library.catalog.BookCopyRepository;
import cm.univ.library.common.enums.CopyStatus;
import cm.univ.library.common.enums.FineType;
import cm.univ.library.common.enums.LoanStatus;
import cm.univ.library.common.enums.NotificationType;
import cm.univ.library.common.enums.ReservationStatus;
import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.fine.FineService;
import cm.univ.library.loan.dto.IssueLoanRequest;
import cm.univ.library.loan.dto.LoanResponse;
import cm.univ.library.loan.dto.ReturnByCodeRequest;
import cm.univ.library.loan.dto.ReturnOutcome;
import cm.univ.library.notification.NotificationService;
import cm.univ.library.policy.LibraryPolicy;
import cm.univ.library.policy.LibraryPolicyRepository;
import cm.univ.library.reservation.Reservation;
import cm.univ.library.reservation.ReservationRepository;
import cm.univ.library.user.User;
import cm.univ.library.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final List<LoanStatus> OPEN_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    private final LoanRepository loanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final UserService userService;
    private final LibraryPolicyRepository libraryPolicyRepository;
    private final ReservationRepository reservationRepository;
    private final FineService fineService;
    private final NotificationService notificationService;

    @Transactional
    public LoanResponse issueLoan(IssueLoanRequest request, User issuedBy) {
        User borrower = userService.findEntityByMatricule(request.borrowerMatricule());
        BookCopy copy = bookCopyRepository.findByInventoryCode(request.inventoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("No copy found for code: " + request.inventoryCode()));
        LibraryPolicy policy = policyFor(borrower);

        if (fineService.hasUnpaidFines(borrower.getId())) {
            throw new BusinessRuleException("Borrower has unpaid fines and cannot check out books until settled");
        }

        long activeLoans = loanRepository.countByBorrowerIdAndStatus(borrower.getId(), LoanStatus.ACTIVE);
        if (activeLoans >= policy.getMaxConcurrentLoans()) {
            throw new BusinessRuleException("Borrower has reached the maximum of " + policy.getMaxConcurrentLoans() + " concurrent loans");
        }

        validateCopyAvailableForBorrower(copy, borrower);

        copy.setStatus(CopyStatus.BORROWED);
        bookCopyRepository.save(copy);

        LocalDate today = LocalDate.now();
        Loan loan = Loan.builder()
                .bookCopy(copy)
                .borrower(borrower)
                .issuedBy(issuedBy)
                .borrowDate(today)
                .dueDate(today.plusDays(policy.getLoanDurationDays()))
                .status(LoanStatus.ACTIVE)
                .build();

        return LoanResponse.from(loanRepository.save(loan));
    }

    /** A scanned copy must be free to lend: either sitting AVAILABLE, or RESERVED
     *  specifically for this borrower's fulfilled hold. Anything else (borrowed,
     *  reserved for someone else, lost, damaged, in repair) is rejected. */
    private void validateCopyAvailableForBorrower(BookCopy copy, User borrower) {
        if (copy.getStatus() == CopyStatus.AVAILABLE) {
            return;
        }
        if (copy.getStatus() == CopyStatus.RESERVED) {
            boolean reservedForThisBorrower = reservationRepository
                    .findFirstByBookIdAndStatusOrderByReservedAtAsc(copy.getBook().getId(), ReservationStatus.FULFILLED)
                    .filter(r -> r.getUser().getId().equals(borrower.getId()))
                    .isPresent();
            if (reservedForThisBorrower) {
                return;
            }
            throw new BusinessRuleException("This copy is held for another borrower's reservation");
        }
        throw new BusinessRuleException("Copy " + copy.getInventoryCode() + " is not available (status: " + copy.getStatus() + ")");
    }

    @Transactional
    public LoanResponse returnByCode(ReturnByCodeRequest request) {
        BookCopy copy = bookCopyRepository.findByInventoryCode(request.inventoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("No copy found for code: " + request.inventoryCode()));
        Loan loan = loanRepository.findByBookCopyIdAndStatusIn(copy.getId(), OPEN_STATUSES)
                .orElseThrow(() -> new BusinessRuleException("No active loan found for copy " + request.inventoryCode()));
        return finishReturn(loan, request.outcome(), request.feeXaf());
    }

    @Transactional
    public LoanResponse returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("Loan " + loanId + " was already returned");
        }

        return finishReturn(loan, ReturnOutcome.NORMAL, null);
    }

    private LoanResponse finishReturn(Loan loan, ReturnOutcome outcome, BigDecimal librarianFeeXaf) {
        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);

        if (outcome != ReturnOutcome.NORMAL) {
            if (librarianFeeXaf == null || librarianFeeXaf.signum() <= 0) {
                throw new BusinessRuleException("A replacement/damage fee amount is required");
            }
            boolean lost = outcome == ReturnOutcome.LOST;
            loan.setStatus(lost ? LoanStatus.LOST : LoanStatus.RETURNED);

            String reason = lost ? "Lost book replacement" : "Damage fee";
            fineService.createFine(loan, librarianFeeXaf, reason, lost ? FineType.LOST : FineType.DAMAGE);
            notificationService.notifyUser(loan.getBorrower(), NotificationType.FINE_ISSUED,
                    "A " + reason.toLowerCase() + " fine of " + librarianFeeXaf + " XAF was applied for '"
                            + loan.getBookCopy().getBook().getTitle() + "'.");

            BookCopy copy = loan.getBookCopy();
            copy.setStatus(lost ? CopyStatus.LOST : CopyStatus.DAMAGED);
            bookCopyRepository.save(copy);

            return LoanResponse.from(loan);
        }

        loan.setStatus(LoanStatus.RETURNED);

        if (today.isAfter(loan.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            LibraryPolicy policy = policyFor(loan.getBorrower());
            BigDecimal fineAmount = policy.getFinePerDayXaf().multiply(BigDecimal.valueOf(daysLate));
            fineService.createFine(loan, fineAmount, "Late return: " + daysLate + " day(s) overdue", FineType.LATE);
            notificationService.notifyUser(loan.getBorrower(), NotificationType.FINE_ISSUED,
                    "A fine of " + fineAmount + " XAF was applied for the late return of '"
                            + loan.getBookCopy().getBook().getTitle() + "'.");
        }

        releaseCopyAfterReturn(loan.getBookCopy());

        return LoanResponse.from(loan);
    }

    private void releaseCopyAfterReturn(BookCopy copy) {
        var nextReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(copy.getBook().getId(), ReservationStatus.PENDING);

        if (nextReservation.isPresent()) {
            Reservation reservation = nextReservation.get();
            reservation.setStatus(ReservationStatus.FULFILLED);
            reservation.setExpiresAt(java.time.Instant.now().plus(3, ChronoUnit.DAYS));

            copy.setStatus(CopyStatus.RESERVED);
            bookCopyRepository.save(copy);

            notificationService.notifyUser(reservation.getUser(), NotificationType.RESERVATION_AVAILABLE,
                    "'" + copy.getBook().getTitle() + "' is now available for you to collect. "
                            + "Please pick it up within 3 days before your hold expires.");
        } else {
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
        }
    }

    @Transactional
    public LoanResponse renewLoan(Long loanId, User requester) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (!loan.getBorrower().getId().equals(requester.getId())
                && requester.getRole() != cm.univ.library.common.enums.Role.LIBRARIAN
                && requester.getRole() != cm.univ.library.common.enums.Role.ADMIN) {
            throw new BusinessRuleException("You may only renew your own loans");
        }
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessRuleException("Only active loans can be renewed");
        }

        LibraryPolicy policy = policyFor(loan.getBorrower());
        if (loan.getRenewalCount() >= policy.getMaxRenewals()) {
            throw new BusinessRuleException("Renewal limit reached for this loan");
        }

        long pendingReservations = reservationRepository.countByBookIdAndStatus(
                loan.getBookCopy().getBook().getId(), ReservationStatus.PENDING);
        if (pendingReservations > 0) {
            throw new BusinessRuleException("Cannot renew: another user is waiting on a reservation for this title");
        }

        loan.setDueDate(loan.getDueDate().plusDays(policy.getLoanDurationDays()));
        loan.setRenewalCount(loan.getRenewalCount() + 1);

        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> loansForUser(Long userId) {
        return loanRepository.findByBorrowerIdOrderByBorrowDateDesc(userId).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> activeLoans() {
        return loanRepository.findByStatusInOrderByDueDateAsc(OPEN_STATUSES).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanResponse activeLoanByCode(String inventoryCode) {
        BookCopy copy = bookCopyRepository.findByInventoryCode(inventoryCode)
                .orElseThrow(() -> new ResourceNotFoundException("No copy found for code: " + inventoryCode));
        return loanRepository.findByBookCopyIdAndStatusIn(copy.getId(), OPEN_STATUSES)
                .map(LoanResponse::from)
                .orElseThrow(() -> new BusinessRuleException("No active loan found for copy " + inventoryCode));
    }

    private LibraryPolicy policyFor(User user) {
        return libraryPolicyRepository.findByRole(user.getRole())
                .orElseThrow(() -> new BusinessRuleException("No library policy configured for role " + user.getRole()));
    }
}
