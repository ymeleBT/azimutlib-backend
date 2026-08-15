package cm.univ.library.catalog;

import cm.univ.library.catalog.dto.CategoryDto;
import cm.univ.library.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<CategoryDto> listAll() {
        return categoryRepository.findAll().stream().map(CategoryDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryDto request) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryDto.from(categoryRepository.save(category)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
