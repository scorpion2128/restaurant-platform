package io.restaurant.platform.modules.payment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentReceiptResponse(
        PaymentResponse payment,
        RestaurantInfo restaurant,
        List<OrderAccountResponse> orders
) {
    public record RestaurantInfo(
            String name,
            String companyName,
            String ruc,
            String address,
            String phone,
            String email,
            String receiptFooter
    ) {
    }
}
