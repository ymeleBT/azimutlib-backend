package cm.univ.library.user;

import cm.univ.library.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMatricule(String matricule);
    boolean existsByMatricule(String matricule);
    boolean existsByEmail(String email);
    long countByRole(Role role);
}
