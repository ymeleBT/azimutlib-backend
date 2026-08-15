package cm.univ.library.scheduling;

import cm.univ.library.catalog.BookCopyRepository;
import cm.univ.library.common.enums.CopyStatus;
import cm.univ.library.common.enums.LoanStatus;
import cm.univ.library.common.enums.NotificationType;
import cm.univ.library.common.enums.ReservationStatus;
import cm.univ.library.loan.Loan;
import cm.univ.library.loan.LoanRepository;
import cm.univ.library.notification.NotificationService;
import cm.univ.library.reservation.Reservation;
import cm.univ.library.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Daily housekeeping: flag overdue loans, expire unclaimed reservation holds, and remind
 *  borrowers whose books are due soon. Runs once a day at 06:00 server time. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryScheduledTasks {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final BookCopyRepository bookCopyRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void markOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<Loan> overdue = loanRepository.findByStatusAndDueDateBefore(LoanStatus.ACTIVE, today);
        for (Loan loan : overdue) {
            loan.setStatus(LoanStatus.OVERDUE);
            notificationService.notifyUser(loan.getBorrower(), NotificationType.OVERDUE,
                    "'" + loan.getBookCopy().getBook().getTitle() + "' was due on " + loan.getDueDate()
                            + " and is now overdue. Fines accrue daily until it is returned.");
        }
        log.info("Marked {} loan(s) overdue", overdue.size());
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void sendDueSoonReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in2Days = today.plusDays(2);
        List<Loan> dueSoon = loanRepository.findByStatusAndDueDateBetween(LoanStatus.ACTIVE, today, in2Days);
        for (Loan loan : dueSoon) {
            notificationService.notifyUser(loan.getBorrower(), NotificationType.DUE_SOON,
                    "'" + loan.getBookCopy().getBook().getTitle() + "' is due back on " + loan.getDueDate() + ".");
        }
        log.info("Sent {} due-soon reminder(s)", dueSoon.size());
    }

    @Scheduled(cron = "0 30 6 * * *")
    @Transactional
    public void expireUnclaimedReservations() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.FULFILLED, Instant.now());

        for (Reservation reservation : expired) {
            reservation.setStatus(ReservationStatus.EXPIRED);

            var heldCopies = bookCopyRepository.findByBookIdAndStatus(reservation.getBook().getId(), CopyStatus.RESERVED);
            if (heldCopies.isEmpty()) {
                continue;
            }
            var copy = heldCopies.get(0);

            reservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(
                            reservation.getBook().getId(), ReservationStatus.PENDING)
                    .ifPresentOrElse(
                            next -> {
                                next.setStatus(ReservationStatus.FULFILLED);
                                next.setExpiresAt(Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS));
                                notificationService.notifyUser(next.getUser(), NotificationType.RESERVATION_AVAILABLE,
                                        "'" + copy.getBook().getTitle() + "' is now available for you to collect.");
                            },
                            () -> copy.setStatus(CopyStatus.AVAILABLE)
                    );
            bookCopyRepository.save(copy);
        }
        log.info("Expired {} unclaimed reservation(s)", expired.size());
    }
}
