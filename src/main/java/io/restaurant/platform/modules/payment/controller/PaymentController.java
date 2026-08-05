package io.restaurant.platform.modules.payment.controller;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.payment.dto.request.CreatePaymentRequest;
import io.restaurant.platform.modules.payment.dto.response.PaymentReceiptResponse;
import io.restaurant.platform.modules.payment.dto.response.PaymentResponse;
import io.restaurant.platform.modules.payment.dto.response.TableAccountResponse;
import io.restaurant.platform.modules.payment.service.PaymentService;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityContextHelper securityContextHelper;

    /**
     * GET /api/payments/table/{tableId}/account
     * Obtiene la cuenta de una mesa (órdenes no pagadas)
     */
    @GetMapping("/table/{tableId}/account")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CASHIER')")
    public ResponseEntity<ApiResponse<TableAccountResponse>> getTableAccount(
            @PathVariable("tableId") Long tableId,
            @AuthenticationPrincipal User user) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();
        TableAccountResponse response = paymentService.getTableAccount(tableId, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/payments
     * Procesa el pago de una o más órdenes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CASHIER')")
    public ResponseEntity<ApiResponse<PaymentReceiptResponse>> processPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal User user) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();
        Long userId = user.getId();
        PaymentReceiptResponse response = paymentService.processPayment(request, restaurantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", response));
    }

    /**
     * GET /api/payments/{id}/receipt
     * Obtiene el recibo de un pago (para reimprimir)
     */
    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER', 'CASHIER')")
    public ResponseEntity<ApiResponse<PaymentReceiptResponse>> getPaymentReceipt(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();
        PaymentReceiptResponse response = paymentService.getPaymentReceipt(id, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/payments
     * Lista todos los pagos del restaurante
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
            @PageableDefault(size = 20, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();
        Page<PaymentResponse> response = paymentService.getAllPayments(restaurantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/payments/{id}
     * Obtiene un pago por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();
        PaymentResponse response = paymentService.getPaymentById(id, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
