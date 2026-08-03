package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuItemRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        Long sectionId,

        @DecimalMin(value = "0.0", inclusive = true, message = "Price override must be zero or positive")
        BigDecimal priceOverride

) {
}
