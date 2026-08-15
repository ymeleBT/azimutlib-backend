package cm.univ.library.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(
            String title, String authors, Pageable pageable);
}
