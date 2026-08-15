package cm.univ.library.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
        String isbn,
        @NotBlank String title,
        @NotBlank String authors,
        String publisher,
        Integer publicationYear,
        Long categoryId,
        String language,
        String description,
        String coverUrl,
        int initialCopies
) {
}
