package io.restaurant.platform.modules.payment.dto.response;

import io.restaurant.platform.modules.order.dto.response.OrderItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderAccountResponse(
        Long orderId,
        String orderNumber,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        BigDecimal total
) {
}
