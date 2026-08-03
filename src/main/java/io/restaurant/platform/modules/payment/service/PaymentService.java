package io.restaurant.platform.modules.payment.service;

import io.restaurant.platform.modules.payment.dto.request.CreatePaymentRequest;
import io.restaurant.platform.modules.payment.dto.response.PaymentReceiptResponse;
import io.restaurant.platform.modules.payment.dto.response.PaymentResponse;
import io.restaurant.platform.modules.payment.dto.response.TableAccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /**
     * Obtiene la cuenta de una mesa (órdenes no pagadas)
     */
    TableAccountResponse getTableAccount(Long tableId, Long restaurantId);

    /**
     * Procesa el pago de una o más órdenes
     */
    PaymentReceiptResponse processPayment(CreatePaymentRequest request, Long restaurantId, Long userId);

    /**
     * Obtiene el recibo de un pago (para reimprimir)
     */
    PaymentReceiptResponse getPaymentReceipt(Long paymentId, Long restaurantId);

    /**
     * Lista todos los pagos del restaurante
     */
    Page<PaymentResponse> getAllPayments(Long restaurantId, Pageable pageable);

    /**
     * Obtiene un pago por ID
     */
    PaymentResponse getPaymentById(Long paymentId, Long restaurantId);
}
