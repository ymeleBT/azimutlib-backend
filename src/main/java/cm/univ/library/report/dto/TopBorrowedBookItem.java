package cm.univ.library.report.dto;

public record TopBorrowedBookItem(Long bookId, String title, long loanCount) {
}
