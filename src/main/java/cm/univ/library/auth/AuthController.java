package cm.univ.library.auth;

import cm.univ.library.auth.dto.LoginRequest;
import cm.univ.library.auth.dto.LoginResponse;
import cm.univ.library.security.JwtService;
import cm.univ.library.user.User;
import cm.univ.library.user.UserRepository;
import cm.univ.library.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.matricule(), request.password()));

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        User user = userRepository.findByMatricule(request.matricule()).orElseThrow();
        return new LoginResponse(token, UserResponse.from(user));
    }
}
