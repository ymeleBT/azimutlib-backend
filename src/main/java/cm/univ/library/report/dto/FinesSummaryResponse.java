package cm.univ.library.report.dto;

import java.math.BigDecimal;

public record FinesSummaryResponse(
        BigDecimal totalUnpaidXaf,
        long unpaidCount,
        BigDecimal totalPaidXaf,
        long paidCount
) {
}
