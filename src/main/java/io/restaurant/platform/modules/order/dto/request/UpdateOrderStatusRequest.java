package io.restaurant.platform.modules.order.dto.request;

import io.restaurant.platform.modules.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "Status is required")
        OrderStatus status

) {
}
