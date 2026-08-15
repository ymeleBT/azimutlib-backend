package cm.univ.library.user.dto;

import cm.univ.library.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String matricule,
        @NotBlank String fullName,
        @Email String email,
        String phone,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotNull Role role,
        String department
) {
}
