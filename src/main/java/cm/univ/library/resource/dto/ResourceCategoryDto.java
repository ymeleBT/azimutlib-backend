package cm.univ.library.resource.dto;

import cm.univ.library.resource.ResourceCategory;
import jakarta.validation.constraints.NotBlank;

public record ResourceCategoryDto(
        Long id,
        @NotBlank String name,
        String description
) {
    public static ResourceCategoryDto from(ResourceCategory category) {
        return new ResourceCategoryDto(category.getId(), category.getName(), category.getDescription());
    }
}
