package io.restaurant.platform.modules.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,

        Long productId,

        String productName,

        Integer quantity,

        BigDecimal unitPrice,

        BigDecimal subtotal,

        String notes,

        Boolean isPartOfMenu,

        Long menuGroupId,

        String sectionName

) {
}
