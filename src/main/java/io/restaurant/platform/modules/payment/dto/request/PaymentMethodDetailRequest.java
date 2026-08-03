package io.restaurant.platform.modules.payment.dto.request;

import io.restaurant.platform.modules.payment.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentMethodDetailRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        BigDecimal amountReceived,  // Solo para CASH
        BigDecimal changeGiven       // Solo para CASH
) {
}
