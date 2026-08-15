package cm.univ.library.user.dto;

import cm.univ.library.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Admin-only edit of another user's account. Unlike {@link UpdateProfileRequest}, this also
 *  allows changing role/department and, optionally, resetting the password — leave
 *  {@code password} null/blank to keep the current one. */
public record AdminUpdateUserRequest(
        @NotBlank String fullName,
        @Email String email,
        String phone,
        @NotNull Role role,
        String department,
        @Size(min = 8, message = "Password must be at least 8 characters") String password
) {
}
