package cm.univ.library.auth.dto;

import cm.univ.library.user.dto.UserResponse;

public record LoginResponse(
        String token,
        UserResponse user
) {
}
