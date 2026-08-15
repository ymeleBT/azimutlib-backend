package cm.univ.library.user.dto;

import cm.univ.library.common.enums.Role;
import cm.univ.library.common.enums.UserStatus;
import cm.univ.library.user.User;

public record UserResponse(
        Long id,
        String matricule,
        String fullName,
        String email,
        String phone,
        Role role,
        String department,
        UserStatus status,
        boolean hasAvatar
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getMatricule(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getDepartment(),
                user.getStatus(),
                user.getAvatarPath() != null
        );
    }
}
