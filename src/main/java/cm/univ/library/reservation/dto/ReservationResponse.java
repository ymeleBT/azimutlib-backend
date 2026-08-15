package cm.univ.library.reservation.dto;

import cm.univ.library.common.enums.ReservationStatus;
import cm.univ.library.reservation.Reservation;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long bookId,
        String bookTitle,
        ReservationStatus status,
        Instant reservedAt,
        Instant expiresAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getBook().getId(),
                reservation.getBook().getTitle(),
                reservation.getStatus(),
                reservation.getReservedAt(),
                reservation.getExpiresAt()
        );
    }
}
