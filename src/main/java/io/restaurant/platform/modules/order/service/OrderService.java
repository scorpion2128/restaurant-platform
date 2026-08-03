package io.restaurant.platform.modules.order.service;

import io.restaurant.platform.modules.order.dto.request.CreateOrderRequest;
import io.restaurant.platform.modules.order.dto.request.UpdateOrderStatusRequest;
import io.restaurant.platform.modules.order.dto.response.OrderResponse;
import io.restaurant.platform.modules.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Long restaurantId, Long waiterId);

    OrderResponse findById(Long id, Long restaurantId);

    Page<OrderResponse> findByRestaurant(Long restaurantId, Pageable pageable);

    Page<OrderResponse> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    List<OrderResponse> findActiveOrdersByWaiter(Long restaurantId, Long waiterId);

    List<OrderResponse> findOrdersForKitchen(Long restaurantId);

    OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long restaurantId);
}
