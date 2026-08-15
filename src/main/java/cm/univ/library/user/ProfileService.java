package cm.univ.library.user;

import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.storage.FileStorageService;
import cm.univ.library.user.dto.UpdateProfileRequest;
import cm.univ.library.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Set<String> ALLOWED_AVATAR_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "image/gif");
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Every method here receives `user` from CurrentUserService.resolve(), which loads it
    // outside any transaction — so it's detached by the time it reaches this class's
    // @Transactional methods. Dirty-checking won't persist setter calls on a detached
    // entity; each mutator must explicitly userRepository.save(user) to merge it back in.

    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A user with email '" + request.email() + "' already exists");
        }

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateAvatar(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("A file is required");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new BusinessRuleException("Avatar must be smaller than 5MB");
        }
        if (file.getContentType() == null || !ALLOWED_AVATAR_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleException("Avatar must be an image (PNG, JPEG, WEBP or GIF)");
        }

        String previousPath = user.getAvatarPath();
        String storageKey = fileStorageService.store(file, "avatars");
        user.setAvatarPath(storageKey);
        user.setAvatarContentType(file.getContentType());

        User saved = userRepository.save(user);

        if (previousPath != null) {
            fileStorageService.delete(previousPath);
        }

        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse removeAvatar(User user) {
        if (user.getAvatarPath() != null) {
            fileStorageService.delete(user.getAvatarPath());
            user.setAvatarPath(null);
            user.setAvatarContentType(null);
            user = userRepository.save(user);
        }
        return UserResponse.from(user);
    }

    public InputStream loadAvatar(User user) {
        if (user.getAvatarPath() == null) {
            throw new ResourceNotFoundException("No avatar set for this user");
        }
        return fileStorageService.load(user.getAvatarPath());
    }
}
