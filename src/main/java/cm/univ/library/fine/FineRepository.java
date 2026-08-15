package cm.univ.library.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserIdAndPaid(Long userId, boolean paid);
    boolean existsByUserIdAndPaidFalse(Long userId);
    List<Fine> findAllByOrderByCreatedAtDesc();

    @Query("select coalesce(sum(f.amountXaf),0) from Fine f where f.paid = false")
    BigDecimal sumUnpaid();

    @Query("select coalesce(sum(f.amountXaf),0) from Fine f where f.paid = true")
    BigDecimal sumPaid();

    long countByPaid(boolean paid);
}
