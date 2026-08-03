package io.restaurant.platform.modules.payment.dto.response;

import io.restaurant.platform.modules.payment.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodDetailResponse(
        Long id,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        BigDecimal amountReceived,
        BigDecimal changeGiven
) {
}
