package cm.univ.library.report;

import cm.univ.library.common.enums.LoanStatus;
import cm.univ.library.fine.FineRepository;
import cm.univ.library.loan.LoanRepository;
import cm.univ.library.loan.dto.LoanResponse;
import cm.univ.library.report.dto.FinesSummaryResponse;
import cm.univ.library.report.dto.LoansPerMonthItem;
import cm.univ.library.report.dto.TopBorrowedBookItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final List<LoanStatus> OPEN_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;

    @Transactional(readOnly = true)
    public List<LoanResponse> overdueLoans() {
        return loanRepository.findByStatusInOrderByDueDateAsc(OPEN_STATUSES).stream()
                .map(LoanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopBorrowedBookItem> topBorrowedBooks(int limit) {
        return loanRepository.topBorrowedBooks(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<LoansPerMonthItem> loansPerMonth() {
        return loanRepository.loansPerMonth();
    }

    @Transactional(readOnly = true)
    public FinesSummaryResponse finesSummary() {
        return new FinesSummaryResponse(
                fineRepository.sumUnpaid(),
                fineRepository.countByPaid(false),
                fineRepository.sumPaid(),
                fineRepository.countByPaid(true)
        );
    }
}
