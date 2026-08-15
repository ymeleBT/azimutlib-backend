package cm.univ.library.resource;

import cm.univ.library.common.enums.ResourceType;
import cm.univ.library.common.enums.Role;
import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.resource.dto.TeachingResourceResponse;
import cm.univ.library.storage.FileStorageService;
import cm.univ.library.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingResourceService {

    private final TeachingResourceRepository resourceRepository;
    private final ResourceCategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public TeachingResourceResponse upload(MultipartFile file, String title, String description,
                                            ResourceType type, Long categoryId, String author,
                                            String academicYear, User uploader) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("A file is required");
        }
        ResourceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        String storageKey = fileStorageService.store(file, "resources");

        TeachingResource resource = TeachingResource.builder()
                .title(title)
                .description(description)
                .author(author)
                .academicYear(academicYear)
                .type(type)
                .category(category)
                .uploadedBy(uploader)
                .filePath(storageKey)
                .originalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .build();

        return TeachingResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public List<TeachingResourceResponse> list(Long categoryId, ResourceType type) {
        List<TeachingResource> resources;
        if (categoryId != null && type != null) {
            resources = resourceRepository.findByCategoryIdAndTypeOrderByCreatedAtDesc(categoryId, type);
        } else if (categoryId != null) {
            resources = resourceRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
        } else if (type != null) {
            resources = resourceRepository.findByTypeOrderByCreatedAtDesc(type);
        } else {
            resources = resourceRepository.findAllByOrderByCreatedAtDesc();
        }
        return resources.stream().map(TeachingResourceResponse::from).toList();
    }

    public TeachingResource findEntityById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }

    public InputStream download(Long id) {
        return fileStorageService.load(findEntityById(id).getFilePath());
    }

    @Transactional
    public void delete(Long id, User requester) {
        TeachingResource resource = findEntityById(id);

        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwner = resource.getUploadedBy().getId().equals(requester.getId());
        if (!isAdmin && !isOwner) {
            throw new BusinessRuleException("Only the uploading lecturer or an administrator can remove this resource");
        }

        resourceRepository.delete(resource);
        fileStorageService.delete(resource.getFilePath());
    }
}
