package cm.univ.library.security;

import cm.univ.library.user.User;
import cm.univ.library.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String matricule) throws UsernameNotFoundException {
        User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException("No user with matricule: " + matricule));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getMatricule())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .disabled(user.getStatus() != cm.univ.library.common.enums.UserStatus.ACTIVE)
                .build();
    }
}
