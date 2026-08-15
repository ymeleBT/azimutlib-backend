package cm.univ.library.loan.dto;

import cm.univ.library.common.enums.LoanStatus;
import cm.univ.library.loan.Loan;

import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String inventoryCode,
        Long borrowerId,
        String borrowerName,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        LoanStatus status,
        int renewalCount
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBookCopy().getBook().getId(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getInventoryCode(),
                loan.getBorrower().getId(),
                loan.getBorrower().getFullName(),
                loan.getBorrowDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getStatus(),
                loan.getRenewalCount()
        );
    }
}
