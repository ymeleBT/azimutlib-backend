package cm.univ.library.loan;

import cm.univ.library.common.enums.LoanStatus;
import cm.univ.library.report.dto.LoansPerMonthItem;
import cm.univ.library.report.dto.TopBorrowedBookItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);
    long countByBorrowerIdAndStatus(Long borrowerId, LoanStatus status);
    Optional<Loan> findByBookCopyIdAndStatus(Long bookCopyId, LoanStatus status);
    Optional<Loan> findByBookCopyIdAndStatusIn(Long bookCopyId, Collection<LoanStatus> statuses);
    List<Loan> findByStatusInOrderByDueDateAsc(Collection<LoanStatus> statuses);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
    List<Loan> findByStatusAndDueDateBetween(LoanStatus status, LocalDate start, LocalDate end);
    List<Loan> findByBorrowerIdOrderByBorrowDateDesc(Long borrowerId);

    @Query("""
        select new cm.univ.library.report.dto.TopBorrowedBookItem(b.id, b.title, count(l))
        from Loan l join l.bookCopy c join c.book b
        group by b.id, b.title order by count(l) desc
        """)
    List<TopBorrowedBookItem> topBorrowedBooks(Pageable pageable);

    @Query("""
        select new cm.univ.library.report.dto.LoansPerMonthItem(cast(function('DATE_FORMAT', l.borrowDate, '%Y-%m') as string), count(l))
        from Loan l group by function('DATE_FORMAT', l.borrowDate, '%Y-%m') order by function('DATE_FORMAT', l.borrowDate, '%Y-%m')
        """)
    List<LoansPerMonthItem> loansPerMonth();
}
