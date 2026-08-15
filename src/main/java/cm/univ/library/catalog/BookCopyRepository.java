package cm.univ.library.catalog;

import cm.univ.library.common.enums.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);
    Optional<BookCopy> findByInventoryCode(String inventoryCode);
    long countByBookIdAndStatus(Long bookId, CopyStatus status);
    long countByBookId(Long bookId);
}
