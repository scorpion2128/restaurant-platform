package io.restaurant.platform.modules.user.dto.request;

import io.restaurant.platform.shared.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 80)
        String lastName,

        @NotNull
        UserRole role,

        @NotNull
        Boolean enabled

) {
}