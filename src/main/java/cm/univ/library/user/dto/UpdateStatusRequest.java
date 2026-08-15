package cm.univ.library.user.dto;

import cm.univ.library.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull UserStatus status) {
}
