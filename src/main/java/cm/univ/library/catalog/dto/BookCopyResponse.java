package cm.univ.library.catalog.dto;

import cm.univ.library.catalog.BookCopy;
import cm.univ.library.common.enums.CopyStatus;

public record BookCopyResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String inventoryCode,
        CopyStatus status
) {
    public static BookCopyResponse from(BookCopy copy) {
        return new BookCopyResponse(
                copy.getId(),
                copy.getBook().getId(),
                copy.getBook().getTitle(),
                copy.getInventoryCode(),
                copy.getStatus()
        );
    }
}
