package cm.univ.library.config;

import cm.univ.library.common.enums.Role;
import cm.univ.library.user.User;
import cm.univ.library.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Ensures at least one ADMIN account exists so the system is usable on first boot.
 *  The seeded password must be changed immediately after first login. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_MATRICULE = "ADMIN001";
    private static final String DEFAULT_ADMIN_PASSWORD = "ChangeMe123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }

        User admin = User.builder()
                .matricule(DEFAULT_ADMIN_MATRICULE)
                .fullName("System Administrator")
                .email("admin@library.local")
                .passwordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.warn("Seeded default admin account (matricule={}, password={}) - CHANGE THIS PASSWORD IMMEDIATELY",
                DEFAULT_ADMIN_MATRICULE, DEFAULT_ADMIN_PASSWORD);
    }
}
