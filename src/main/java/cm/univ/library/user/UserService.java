package cm.univ.library.user;

import cm.univ.library.common.enums.UserStatus;
import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.storage.FileStorageService;
import cm.univ.library.user.dto.AdminUpdateUserRequest;
import cm.univ.library.user.dto.CreateUserRequest;
import cm.univ.library.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByMatricule(request.matricule())) {
            throw new BusinessRuleException("A user with matricule '" + request.matricule() + "' already exists");
        }
        if (request.email() != null && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A user with email '" + request.email() + "' already exists");
        }

        User user = User.builder()
                .matricule(request.matricule())
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .department(request.department())
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findEntityById(id));
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User findEntityByMatricule(String matricule) {
        return userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with matricule: " + matricule));
    }

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        User user = findEntityById(id);
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A user with email '" + request.email() + "' already exists");
        }

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setDepartment(request.department());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateStatus(Long id, UserStatus status) {
        User user = findEntityById(id);
        user.setStatus(status);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findEntityById(id);
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessRuleException(
                    "This user has related records (loans, reservations, ...) and cannot be deleted. Suspend the account instead.");
        }
        if (user.getAvatarPath() != null) {
            fileStorageService.delete(user.getAvatarPath());
        }
    }
}
