package io.restaurant.platform.modules.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        Long id,
        String paymentNumber,
        Long restaurantId,
        Long tableId,
        String tableName,
        Long waiterId,
        String waiterName,
        BigDecimal subtotal,
        BigDecimal igvAmount,
        BigDecimal totalAmount,
        LocalDateTime paidAt,
        Long paidById,
        String paidByName,
        String observations,
        List<PaymentMethodDetailResponse> paymentMethods,
        List<Long> orderIds
) {
}
