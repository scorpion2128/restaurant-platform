package io.restaurant.platform.modules.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Create Product Request - Simplified for master catalog
 * Product only stores price and availability locally
 * Name, description, and category come from master_product
 */
public record CreateProductRequest(

        @NotNull(message = "Master product ID is required")
        Long masterProductId,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or positive")
        BigDecimal price,

        Boolean available

) {
}
