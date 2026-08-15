package cm.univ.library.catalog;

import cm.univ.library.catalog.dto.BookCopyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/copies")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookService bookService;

    @GetMapping("/{inventoryCode}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public BookCopyResponse getByCode(@PathVariable String inventoryCode) {
        return bookService.getCopyByInventoryCode(inventoryCode);
    }
}
