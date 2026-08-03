package io.restaurant.platform.modules.user.dto.response;

import io.restaurant.platform.shared.enums.UserRole;

import java.util.List;

public record UserResponse(

        Long id,

        Long organizationId,

        String organizationName,

        String firstName,

        String lastName,

        String username,

        Boolean enabled,

        List<RestaurantAccessInfo> restaurantAccess

) {
    public record RestaurantAccessInfo(
        Long restaurantId,
        String restaurantName,
        UserRole role
    ) {}
}