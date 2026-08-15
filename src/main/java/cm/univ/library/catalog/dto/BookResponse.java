package cm.univ.library.catalog.dto;

import cm.univ.library.catalog.Book;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String authors,
        String publisher,
        Integer publicationYear,
        String categoryName,
        String language,
        String description,
        String coverUrl,
        long availableCopies,
        long totalCopies
) {
    public static BookResponse from(Book book, long availableCopies, long totalCopies) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthors(),
                book.getPublisher(),
                book.getPublicationYear(),
                book.getCategory() != null ? book.getCategory().getName() : null,
                book.getLanguage(),
                book.getDescription(),
                book.getCoverUrl(),
                availableCopies,
                totalCopies
        );
    }
}
