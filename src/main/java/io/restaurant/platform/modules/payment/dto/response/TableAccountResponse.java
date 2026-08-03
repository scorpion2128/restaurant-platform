package io.restaurant.platform.modules.payment.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record TableAccountResponse(
        Long tableId,
        String tableName,
        List<OrderAccountResponse> orders,
        BigDecimal totalWithIgv,
        BigDecimal subtotal,
        BigDecimal igvAmount,
        WaiterInfo waiter
) {
    public record WaiterInfo(
            Long id,
            String fullName
    ) {
    }
}
