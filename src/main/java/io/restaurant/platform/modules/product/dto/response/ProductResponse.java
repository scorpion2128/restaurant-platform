package io.restaurant.platform.modules.product.dto.response;

import java.math.BigDecimal;

/**
 * Product Response - Includes master catalog data
 * Local data: price, available
 * Master data: name, description, categoryId, categoryName
 */
public record ProductResponse(

        Long id,

        Long restaurantId,

        Long masterId,

        String name,

        String description,

        BigDecimal price,

        Boolean available,

        Long categoryId,

        String categoryName

) {
}
