package cm.univ.library.user;

import cm.univ.library.security.CurrentUserService;
import cm.univ.library.user.dto.UpdateProfileRequest;
import cm.univ.library.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Self-service profile endpoints — deliberately separate from {@link UserController}, which
 *  is restricted to LIBRARIAN/ADMIN, so every authenticated role can manage their own profile. */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public UserResponse me(Authentication authentication) {
        return profileService.getProfile(currentUserService.resolve(authentication));
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return profileService.updateProfile(currentUserService.resolve(authentication), request);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadAvatar(@RequestParam MultipartFile file, Authentication authentication) {
        return profileService.updateAvatar(currentUserService.resolve(authentication), file);
    }

    @DeleteMapping("/avatar")
    public UserResponse removeAvatar(Authentication authentication) {
        return profileService.removeAvatar(currentUserService.resolve(authentication));
    }

    @GetMapping("/avatar")
    public ResponseEntity<InputStreamResource> avatar(Authentication authentication) {
        User user = currentUserService.resolve(authentication);
        var stream = profileService.loadAvatar(user);
        MediaType mediaType = user.getAvatarContentType() != null
                ? MediaType.parseMediaType(user.getAvatarContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(stream));
    }
}
