package cm.univ.library.user;

import cm.univ.library.common.exception.BusinessRuleException;
import cm.univ.library.security.CurrentUserService;
import cm.univ.library.user.dto.AdminUpdateUserRequest;
import cm.univ.library.user.dto.CreateUserRequest;
import cm.univ.library.user.dto.UpdateStatusRequest;
import cm.univ.library.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Managed by librarians/admins only: students and lecturers do not self-register,
 *  accounts are provisioned by library staff (typically imported from the registrar). */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
public class UserController {

    private final UserService userService;
    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    public List<UserResponse> listAll() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/by-matricule/{matricule}")
    public UserResponse getByMatricule(@PathVariable String matricule) {
        return UserResponse.from(userService.findEntityByMatricule(matricule));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        ensureNotSelf(id, authentication, "You cannot delete your own account");
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request,
                                      Authentication authentication) {
        ensureNotSelf(id, authentication, "You cannot change the status of your own account");
        return userService.updateStatus(id, request.status());
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadAvatar(@PathVariable Long id, @RequestParam MultipartFile file) {
        return profileService.updateAvatar(userService.findEntityById(id), file);
    }

    @DeleteMapping("/{id}/avatar")
    public UserResponse removeAvatar(@PathVariable Long id) {
        return profileService.removeAvatar(userService.findEntityById(id));
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<InputStreamResource> avatar(@PathVariable Long id) {
        User user = userService.findEntityById(id);
        var stream = profileService.loadAvatar(user);
        MediaType mediaType = user.getAvatarContentType() != null
                ? MediaType.parseMediaType(user.getAvatarContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(stream));
    }

    private void ensureNotSelf(Long targetId, Authentication authentication, String message) {
        if (currentUserService.resolve(authentication).getId().equals(targetId)) {
            throw new BusinessRuleException(message);
        }
    }
}
