package io.restaurant.platform.modules.restaurant.dto.request;

import io.restaurant.platform.shared.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record AssignUserToRestaurantRequest(
    @NotNull(message = "User ID is required")
    Long userId,
    
    @NotNull(message = "Role is required")
    UserRole role
) {}
