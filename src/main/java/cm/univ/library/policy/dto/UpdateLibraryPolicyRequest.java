package cm.univ.library.policy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateLibraryPolicyRequest(
        @NotNull @Min(1) Integer loanDurationDays,
        @NotNull @Min(1) Integer maxConcurrentLoans,
        @NotNull @Min(0) Integer maxRenewals,
        @NotNull @DecimalMin("0.0") BigDecimal finePerDayXaf
) {
}
