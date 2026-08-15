package cm.univ.library.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
        @NotNull Long bookId
) {
}
