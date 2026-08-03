package io.restaurant.platform.modules.user.dto.request;

import io.restaurant.platform.shared.enums.UserRole;

public record CreateUserRequest(

        Long restaurantId,

        String firstName,

        String lastName,

        UserRole role

) {
}