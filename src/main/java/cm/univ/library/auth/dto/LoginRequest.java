package cm.univ.library.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String matricule,
        @NotBlank String password
) {
}
