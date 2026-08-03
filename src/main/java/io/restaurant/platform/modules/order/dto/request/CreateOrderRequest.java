package io.restaurant.platform.modules.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        Long tableId,  // Optional: for dine-in orders

        Integer tableNumber,  // Optional: simple table number for now

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemRequest> items

) {
}
