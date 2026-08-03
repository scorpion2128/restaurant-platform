package io.restaurant.platform.modules.payment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePaymentRequest(
        @NotNull(message = "Table ID is required")
        Long tableId,

        @NotEmpty(message = "At least one order is required")
        List<Long> orderIds,

        @NotEmpty(message = "At least one payment method is required")
        @Valid
        List<PaymentMethodDetailRequest> paymentMethods,

        String observations
) {
}
