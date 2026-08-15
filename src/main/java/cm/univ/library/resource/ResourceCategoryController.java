package cm.univ.library.resource;

import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.resource.dto.ResourceCategoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-categories")
@RequiredArgsConstructor
public class ResourceCategoryController {

    private final ResourceCategoryRepository categoryRepository;

    @GetMapping
    public List<ResourceCategoryDto> listAll() {
        return categoryRepository.findAll().stream().map(ResourceCategoryDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER','ADMIN')")
    public ResponseEntity<ResourceCategoryDto> create(@Valid @RequestBody ResourceCategoryDto request) {
        if (categoryRepository.findAll().stream().anyMatch(c -> c.getName().equalsIgnoreCase(request.name()))) {
            throw new BusinessRuleException("A category named '" + request.name() + "' already exists");
        }
        ResourceCategory category = ResourceCategory.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ResourceCategoryDto.from(categoryRepository.save(category)));
    }
}
