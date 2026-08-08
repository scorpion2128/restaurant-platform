package io.restaurant.platform.modules.product.dto.response;

import java.math.BigDecimal;

/**
 * Master Product Response
 * Represents a product in the centralized catalog
 */
public record MasterProductResponse(

        Long id,

        Long organizationId,

        Long categoryId,

        String categoryName,

        String code,

        String name,

        String description,

        BigDecimal basePrice,

        Boolean active

) {
}
