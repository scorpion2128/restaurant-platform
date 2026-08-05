package io.restaurant.platform.modules.order.service;

import io.restaurant.platform.modules.order.dto.request.CreateOrderRequest;
import io.restaurant.platform.modules.order.dto.request.OrderItemRequest;
import io.restaurant.platform.modules.order.dto.request.UpdateOrderStatusRequest;
import io.restaurant.platform.modules.order.dto.response.OrderItemResponse;
import io.restaurant.platform.modules.order.dto.response.OrderResponse;
import io.restaurant.platform.modules.order.entity.Order;
import io.restaurant.platform.modules.order.entity.OrderItem;
import io.restaurant.platform.modules.order.enums.OrderStatus;
import io.restaurant.platform.modules.order.mapper.OrderItemMapper;
import io.restaurant.platform.modules.order.mapper.OrderMapper;
import io.restaurant.platform.modules.order.repository.OrderRepository;
import io.restaurant.platform.modules.product.entity.Product;
import io.restaurant.platform.modules.product.repository.ProductRepository;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.modules.table.entity.RestaurantTable;
import io.restaurant.platform.modules.table.enums.TableStatus;
import io.restaurant.platform.modules.table.repository.RestaurantTableRepository;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.modules.user.repository.UserRepository;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String RESTAURANT_NOT_FOUND = "Restaurant not found with id: %d";
    private static final String WAITER_NOT_FOUND = "Waiter not found with id: %d";
    private static final String TABLE_NUMBER_NOT_FOUND = "Table not found with number: %d";
    private static final String TABLE_ID_NOT_FOUND = "Table not found with id: %d";
    private static final String PRODUCT_NOT_FOUND = "Product not found with id: %d";
    private static final String ORDER_NOT_FOUND = "Order not found with id: %d";

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long restaurantId, Long waiterId) {
        // Validate restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(restaurantId)));

        // Validate waiter
        User waiter = userRepository.findById(waiterId)
                .orElseThrow(() -> new ResourceNotFoundException(WAITER_NOT_FOUND.formatted(waiterId)));

        // Find table if tableNumber was provided
        Long tableId = null;
        RestaurantTable table = null;
        if (request.tableNumber() != null) {
            table = restaurantTableRepository.findByRestaurantIdAndNumber(restaurantId, request.tableNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(TABLE_NUMBER_NOT_FOUND.formatted(request.tableNumber())));
            tableId = table.getId();
        } else if (request.tableId() != null) {
            // If tableId was provided directly, validate it exists
            table = restaurantTableRepository.findById(request.tableId())
                    .orElseThrow(() -> new ResourceNotFoundException(TABLE_ID_NOT_FOUND.formatted(request.tableId())));
            tableId = table.getId();
        }

        // Crear pedido
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setRestaurant(restaurant);
        order.setWaiter(waiter);
        order.setTableId(tableId);  // Set the tableId
        order.setOrderType("DINE_IN"); // Currently only dine-in
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);

        // Create items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // First, group notes by menuGroupId for menus
        var menuNotesMap = new java.util.HashMap<Long, String>();
        for (OrderItemRequest itemReq : request.items()) {
            if (itemReq.isPartOfMenu() != null && itemReq.isPartOfMenu() && itemReq.menuGroupId() != null) {
                // Take the first non-null/non-empty note for each menu group
                if (itemReq.notes() != null && !itemReq.notes().trim().isEmpty()) {
                    menuNotesMap.putIfAbsent(itemReq.menuGroupId(), itemReq.notes());
                }
            }
        }

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(itemReq.productId())));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(itemReq.unitPrice());
            
            // Calculate item subtotal
            BigDecimal itemSubtotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            item.setSubtotal(itemSubtotal);
            
            // Assign notes: if part of a menu, use the shared group note
            String notesToUse = itemReq.notes();
            if (itemReq.isPartOfMenu() != null && itemReq.isPartOfMenu() && itemReq.menuGroupId() != null) {
                notesToUse = menuNotesMap.get(itemReq.menuGroupId());
            }
            item.setNotes(notesToUse);
            
            item.setIsPartOfMenu(itemReq.isPartOfMenu() != null ? itemReq.isPartOfMenu() : false);
            item.setMenuGroupId(itemReq.menuGroupId());

            orderItems.add(item);
            totalAmount = totalAmount.add(itemSubtotal);
        }

        order.setItems(orderItems);
        order.setSubtotal(totalAmount);
        order.setTotal(totalAmount);

        // Save
        Order savedOrder = orderRepository.save(order);

        // Update table status to OCCUPIED if exists
        if (table != null && table.getStatus() != TableStatus.OCCUPIED) {
            table.setStatus(TableStatus.OCCUPIED);
            restaurantTableRepository.save(table);
        }

        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id, Long restaurantId) {
        Order order = orderRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND.formatted(id)));

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByRestaurant(Long restaurantId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByRestaurantId(restaurantId, pageable);
        return orders.map(this::buildOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByRestaurantIdAndStatus(restaurantId, status, pageable);
        return orders.map(this::buildOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findActiveOrdersByWaiter(Long restaurantId, Long waiterId) {
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.READY, OrderStatus.DELIVERED);
        List<Order> orders = orderRepository.findByRestaurantIdAndWaiterIdAndStatusIn(restaurantId, waiterId, activeStatuses);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersForKitchen(Long restaurantId) {
        List<OrderStatus> kitchenStatuses = List.of(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.READY);
        List<Order> orders = orderRepository.findByRestaurantIdAndStatusIn(restaurantId, kitchenStatuses);
        return orders.stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request, Long restaurantId) {
        Order order = orderRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(ORDER_NOT_FOUND.formatted(id)));

        order.setStatus(request.status());
        Order updatedOrder = orderRepository.save(order);

        // Si el estado es PAID o CANCELLED, verificar si la mesa puede liberarse
        if ((request.status() == OrderStatus.PAID || request.status() == OrderStatus.CANCELLED) 
                && order.getTableId() != null) {
            checkAndReleaseTable(order.getTableId(), restaurantId);
        }

        return buildOrderResponse(updatedOrder);
    }

    /**
     * Check if there are active orders on a table and release it if there are none
     */
    private void checkAndReleaseTable(Long tableId, Long restaurantId) {
        // Find active orders on this table (non-terminal statuses)
        List<OrderStatus> activeStatuses = List.of(
            OrderStatus.PENDING, 
            OrderStatus.IN_PREPARATION, 
            OrderStatus.READY, 
            OrderStatus.DELIVERED
        );
        
        List<Order> activeOrders = orderRepository.findByRestaurantIdAndTableIdAndStatusIn(
            restaurantId, tableId, activeStatuses
        );

        // If no active orders, release the table
        if (activeOrders.isEmpty()) {
            restaurantTableRepository.findById(tableId).ifPresent(table -> {
                table.setStatus(TableStatus.AVAILABLE);
                restaurantTableRepository.save(table);
            });
        }
    }

    private OrderResponse buildOrderResponse(Order order) {
        OrderResponse response = orderMapper.toResponse(order);
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                response.id(),
                response.orderNumber(),
                response.restaurantId(),
                response.tableId(),
                response.waiterId(),
                response.waiterName(),
                response.orderType(),
                response.status(),
                response.subtotal(),
                response.total(),
                response.createdAt(),
                response.updatedAt(),
                itemResponses
        );
    }

    private String generateOrderNumber() {
        // Get start and end of current day
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        // Get all orders for the day
        List<Order> todayOrders = orderRepository.findOrdersCreatedToday(startOfDay, endOfDay);
        
        // Find the last sequential number of the day
        int nextSequence = 1;
        for (Order order : todayOrders) {
            String orderNum = order.getOrderNumber();
            if (orderNum != null && orderNum.startsWith("ORD-")) {
                try {
                    // Extract the number after "ORD-"
                    int currentNum = Integer.parseInt(orderNum.substring(4));
                    if (currentNum >= nextSequence) {
                        nextSequence = currentNum + 1;
                    }
                } catch (NumberFormatException e) {
                    // Ignore orders with old format
                }
            }
        }
        
        // Generate new number with format ORD-XXX (3-digit padding)
        String orderNumber = String.format("ORD-%03d", nextSequence);
        
        // Verify uniqueness (for safety)
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            nextSequence++;
            orderNumber = String.format("ORD-%03d", nextSequence);
        }
        
        return orderNumber;
    }
}
