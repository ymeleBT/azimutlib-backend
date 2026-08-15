package cm.univ.library.catalog.dto;

import cm.univ.library.catalog.Category;
import jakarta.validation.constraints.NotBlank;

public record CategoryDto(
        Long id,
        @NotBlank String name,
        String description
) {
    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getName(), category.getDescription());
    }
}
