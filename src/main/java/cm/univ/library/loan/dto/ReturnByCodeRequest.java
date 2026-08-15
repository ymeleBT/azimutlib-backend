package cm.univ.library.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReturnByCodeRequest(
        @NotBlank String inventoryCode,
        @NotNull ReturnOutcome outcome,
        BigDecimal feeXaf
) {
}
