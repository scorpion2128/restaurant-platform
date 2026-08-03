package io.restaurant.platform.auth.dto.response;

import io.restaurant.platform.shared.enums.UserRole;

public record RestaurantAccessResponse(
    Long restaurantId,
    String restaurantName,
    UserRole role
) {
}
