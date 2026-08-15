package cm.univ.library.policy.dto;

import cm.univ.library.common.enums.Role;
import cm.univ.library.policy.LibraryPolicy;

import java.math.BigDecimal;

public record LibraryPolicyResponse(
        Long id,
        Role role,
        int loanDurationDays,
        int maxConcurrentLoans,
        int maxRenewals,
        BigDecimal finePerDayXaf
) {
    public static LibraryPolicyResponse from(LibraryPolicy policy) {
        return new LibraryPolicyResponse(
                policy.getId(),
                policy.getRole(),
                policy.getLoanDurationDays(),
                policy.getMaxConcurrentLoans(),
                policy.getMaxRenewals(),
                policy.getFinePerDayXaf()
        );
    }
}
