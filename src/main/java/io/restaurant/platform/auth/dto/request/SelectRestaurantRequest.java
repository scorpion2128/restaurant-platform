package io.restaurant.platform.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record SelectRestaurantRequest(
    
    @NotNull(message = "Restaurant ID is required")
    Long restaurantId
    
) {
}
