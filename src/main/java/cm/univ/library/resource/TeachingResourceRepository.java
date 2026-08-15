package cm.univ.library.resource;

import cm.univ.library.common.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingResourceRepository extends JpaRepository<TeachingResource, Long> {
    List<TeachingResource> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    List<TeachingResource> findByTypeOrderByCreatedAtDesc(ResourceType type);
    List<TeachingResource> findByCategoryIdAndTypeOrderByCreatedAtDesc(Long categoryId, ResourceType type);
    List<TeachingResource> findAllByOrderByCreatedAtDesc();
    List<TeachingResource> findByUploadedByIdOrderByCreatedAtDesc(Long uploadedById);
}
