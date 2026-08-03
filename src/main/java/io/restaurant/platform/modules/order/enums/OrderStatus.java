package io.restaurant.platform.modules.order.enums;

public enum OrderStatus {
    PENDING,         // Order just created, waiting for preparation
    IN_PREPARATION,  // In preparation process in kitchen
    READY,           // Ready to be delivered
    DELIVERED,       // Delivered to customer
    PAID,            // Order paid
    CANCELLED        // Order cancelled
}
