package io.restaurant.platform.modules.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Update Product Request - Simplified for master catalog
 * Only price and availability can be updated locally
 * To change name/description/category, update master_product
 */
public record UpdateProductRequest(

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or positive")
        BigDecimal price,

        Boolean available

) {
}
