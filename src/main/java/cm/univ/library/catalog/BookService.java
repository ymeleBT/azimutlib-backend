package cm.univ.library.catalog;

import cm.univ.library.catalog.dto.BookCopyResponse;
import cm.univ.library.catalog.dto.BookResponse;
import cm.univ.library.catalog.dto.CreateBookRequest;
import cm.univ.library.common.enums.CopyStatus;
import cm.univ.library.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        }

        Book book = Book.builder()
                .isbn(request.isbn())
                .title(request.title())
                .authors(request.authors())
                .publisher(request.publisher())
                .publicationYear(request.publicationYear())
                .category(category)
                .language(request.language() != null ? request.language() : "EN")
                .description(request.description())
                .coverUrl(request.coverUrl())
                .build();
        book = bookRepository.save(book);

        int copiesToCreate = Math.max(request.initialCopies(), 0);
        for (int i = 1; i <= copiesToCreate; i++) {
            BookCopy copy = BookCopy.builder()
                    .book(book)
                    .inventoryCode(book.getId() + "-" + i)
                    .status(CopyStatus.AVAILABLE)
                    .build();
            bookCopyRepository.save(copy);
        }

        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> search(String query, Pageable pageable) {
        Page<Book> books = (query == null || query.isBlank())
                ? bookRepository.findAll(pageable)
                : bookRepository.findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(query, query, pageable);
        return books.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return toResponse(findEntityById(id));
    }

    public Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public BookCopyResponse getCopyByInventoryCode(String inventoryCode) {
        BookCopy copy = bookCopyRepository.findByInventoryCode(inventoryCode)
                .orElseThrow(() -> new ResourceNotFoundException("No copy found for code: " + inventoryCode));
        return BookCopyResponse.from(copy);
    }

    private BookResponse toResponse(Book book) {
        long available = bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE);
        long total = bookCopyRepository.countByBookId(book.getId());
        return BookResponse.from(book, available, total);
    }
}
