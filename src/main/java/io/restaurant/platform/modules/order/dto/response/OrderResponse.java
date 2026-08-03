package io.restaurant.platform.modules.order.dto.response;

import io.restaurant.platform.modules.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,

        String orderNumber,

        Long restaurantId,

        Long tableId,

        Long waiterId,

        String waiterName,

        String orderType,

        OrderStatus status,

        BigDecimal subtotal,

        BigDecimal total,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<OrderItemResponse> items

) {
}
