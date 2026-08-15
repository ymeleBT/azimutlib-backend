package cm.univ.library.reservation;

import cm.univ.library.catalog.Book;
import cm.univ.library.catalog.BookCopyRepository;
import cm.univ.library.catalog.BookService;
import cm.univ.library.common.enums.CopyStatus;
import cm.univ.library.common.enums.ReservationStatus;
import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.reservation.dto.CreateReservationRequest;
import cm.univ.library.reservation.dto.ReservationResponse;
import cm.univ.library.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookService bookService;

    @Transactional
    public ReservationResponse reserve(CreateReservationRequest request, User user) {
        Book book = bookService.findEntityById(request.bookId());

        long availableCopies = bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE);
        if (availableCopies > 0) {
            throw new BusinessRuleException("Copies are currently available — borrow directly instead of reserving");
        }

        if (reservationRepository.existsByBookIdAndUserIdAndStatus(book.getId(), user.getId(), ReservationStatus.PENDING)) {
            throw new BusinessRuleException("You already have a pending reservation for this title");
        }

        Reservation reservation = Reservation.builder()
                .book(book)
                .user(user)
                .status(ReservationStatus.PENDING)
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void cancel(Long reservationId, User requester) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));

        boolean isOwner = reservation.getUser().getId().equals(requester.getId());
        boolean isStaff = requester.getRole() == cm.univ.library.common.enums.Role.LIBRARIAN
                || requester.getRole() == cm.univ.library.common.enums.Role.ADMIN;
        if (!isOwner && !isStaff) {
            throw new BusinessRuleException("You may only cancel your own reservations");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.FULFILLED) {
            throw new BusinessRuleException("Reservation is already " + reservation.getStatus());
        }

        boolean wasFulfilled = reservation.getStatus() == ReservationStatus.FULFILLED;
        reservation.setStatus(ReservationStatus.CANCELLED);

        if (wasFulfilled) {
            releaseHeldCopyToNextInLine(reservation.getBook().getId());
        }
    }

    /** A FULFILLED reservation holds a copy in RESERVED status for pickup; cancelling it
     *  must free that copy for the next pending reservation (or back to AVAILABLE). */
    private void releaseHeldCopyToNextInLine(Long bookId) {
        var heldCopies = bookCopyRepository.findByBookIdAndStatus(bookId, CopyStatus.RESERVED);
        if (heldCopies.isEmpty()) {
            return;
        }
        var copy = heldCopies.get(0);

        reservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.PENDING)
                .ifPresentOrElse(
                        next -> {
                            next.setStatus(ReservationStatus.FULFILLED);
                            next.setExpiresAt(java.time.Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS));
                        },
                        () -> copy.setStatus(CopyStatus.AVAILABLE)
                );
        bookCopyRepository.save(copy);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsForUser(Long userId) {
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
