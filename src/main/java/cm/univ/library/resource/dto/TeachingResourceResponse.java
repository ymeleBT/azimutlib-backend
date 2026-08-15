package cm.univ.library.resource.dto;

import cm.univ.library.common.enums.ResourceType;
import cm.univ.library.resource.TeachingResource;

import java.time.Instant;

public record TeachingResourceResponse(
        Long id,
        String title,
        String description,
        String author,
        String academicYear,
        ResourceType type,
        Long categoryId,
        String categoryName,
        Long uploadedById,
        String uploadedByName,
        String originalFilename,
        String contentType,
        long fileSizeBytes,
        Instant createdAt
) {
    public static TeachingResourceResponse from(TeachingResource resource) {
        return new TeachingResourceResponse(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getAuthor(),
                resource.getAcademicYear(),
                resource.getType(),
                resource.getCategory().getId(),
                resource.getCategory().getName(),
                resource.getUploadedBy().getId(),
                resource.getUploadedBy().getFullName(),
                resource.getOriginalFilename(),
                resource.getContentType(),
                resource.getFileSizeBytes(),
                resource.getCreatedAt()
        );
    }
}
