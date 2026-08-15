package cm.univ.library.fine.dto;

import cm.univ.library.common.enums.FineType;
import cm.univ.library.fine.Fine;

import java.math.BigDecimal;
import java.time.Instant;

public record FineResponse(
        Long id,
        Long loanId,
        Long userId,
        String userName,
        FineType type,
        BigDecimal amountXaf,
        String reason,
        boolean paid,
        Instant paidAt
) {
    public static FineResponse from(Fine fine) {
        return new FineResponse(
                fine.getId(),
                fine.getLoan() != null ? fine.getLoan().getId() : null,
                fine.getUser().getId(),
                fine.getUser().getFullName(),
                fine.getType(),
                fine.getAmountXaf(),
                fine.getReason(),
                fine.isPaid(),
                fine.getPaidAt()
        );
    }
}
