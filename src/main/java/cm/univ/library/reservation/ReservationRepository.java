package cm.univ.library.reservation;

import cm.univ.library.common.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);
    long countByBookIdAndStatus(Long bookId, ReservationStatus status);
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant instant);
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, ReservationStatus status);
}
