package cm.univ.library.policy;

import cm.univ.library.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryPolicyRepository extends JpaRepository<LibraryPolicy, Long> {
    Optional<LibraryPolicy> findByRole(Role role);
}
