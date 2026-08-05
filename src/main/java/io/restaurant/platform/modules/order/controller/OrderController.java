package io.restaurant.platform.modules.order.controller;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.order.dto.request.CreateOrderRequest;
import io.restaurant.platform.modules.order.dto.request.UpdateOrderStatusRequest;
import io.restaurant.platform.modules.order.dto.response.OrderResponse;
import io.restaurant.platform.modules.order.enums.OrderStatus;
import io.restaurant.platform.modules.order.service.OrderService;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse response = orderService.createOrder(request, securityContextHelper.getActiveRestaurantId(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'KITCHEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {
        OrderResponse response = orderService.findById(id, securityContextHelper.getActiveRestaurantId());
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponse> response = orderService.findByRestaurant(securityContextHelper.getActiveRestaurantId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByStatus(
            @PathVariable("status") OrderStatus status,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponse> response = orderService.findByRestaurantAndStatus(securityContextHelper.getActiveRestaurantId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/waiter/active")
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getActiveOrdersByWaiter(
            @AuthenticationPrincipal User user) {
        List<OrderResponse> response = orderService.findActiveOrdersByWaiter(securityContextHelper.getActiveRestaurantId(), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Active orders retrieved successfully", response));
    }

    @GetMapping("/kitchen")
    @PreAuthorize("hasRole('KITCHEN')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersForKitchen(
            @AuthenticationPrincipal User user) {
        List<OrderResponse> response = orderService.findOrdersForKitchen(securityContextHelper.getActiveRestaurantId());
        return ResponseEntity.ok(ApiResponse.success("Kitchen orders retrieved successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'KITCHEN', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse response = orderService.updateStatus(id, request, securityContextHelper.getActiveRestaurantId());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
    }
}
