package cm.univ.library.report;

import cm.univ.library.loan.dto.LoanResponse;
import cm.univ.library.report.dto.FinesSummaryResponse;
import cm.univ.library.report.dto.LoansPerMonthItem;
import cm.univ.library.report.dto.TopBorrowedBookItem;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overdue-loans")
    public List<LoanResponse> overdueLoans() {
        return reportService.overdueLoans();
    }

    @GetMapping("/top-borrowed-books")
    public List<TopBorrowedBookItem> topBorrowedBooks(@RequestParam(defaultValue = "10") int limit) {
        return reportService.topBorrowedBooks(limit);
    }

    @GetMapping("/loans-per-month")
    public List<LoansPerMonthItem> loansPerMonth() {
        return reportService.loansPerMonth();
    }

    @GetMapping("/fines-summary")
    public FinesSummaryResponse finesSummary() {
        return reportService.finesSummary();
    }
}
