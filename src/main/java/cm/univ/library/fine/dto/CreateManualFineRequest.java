package cm.univ.library.fine.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateManualFineRequest(
        @NotNull Long userId,
        @NotNull @DecimalMin("0.01") BigDecimal amountXaf,
        @NotBlank String reason
) {
}
