package cm.univ.library.security;

import cm.univ.library.user.User;
import cm.univ.library.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** The JWT principal only carries the matricule (see {@link UserDetailsServiceImpl}), so
 *  "my loans" / "my fines" style endpoints resolve the full User entity through this. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User resolve(Authentication authentication) {
        return userRepository.findByMatricule(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists: " + authentication.getName()));
    }
}
