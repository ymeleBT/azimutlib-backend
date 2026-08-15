package cm.univ.library.policy;

import cm.univ.library.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Loan rules per borrower role (STUDENT / LECTURER), configurable by admins
 *  without a code change or redeploy. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "library_policy")
public class LibraryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private Role role;

    @Column(name = "loan_duration_days", nullable = false)
    private int loanDurationDays;

    @Column(name = "max_concurrent_loans", nullable = false)
    private int maxConcurrentLoans;

    @Column(name = "max_renewals", nullable = false)
    private int maxRenewals;

    @Column(name = "fine_per_day_xaf", nullable = false)
    private BigDecimal finePerDayXaf;
}
