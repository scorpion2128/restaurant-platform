package io.restaurant.platform.modules.payment.service;

import io.restaurant.platform.modules.order.entity.Order;
import io.restaurant.platform.modules.order.enums.OrderStatus;
import io.restaurant.platform.modules.order.repository.OrderRepository;
import io.restaurant.platform.modules.payment.dto.request.CreatePaymentRequest;
import io.restaurant.platform.modules.payment.dto.request.PaymentMethodDetailRequest;
import io.restaurant.platform.modules.payment.dto.response.*;
import io.restaurant.platform.modules.payment.entity.Payment;
import io.restaurant.platform.modules.payment.entity.PaymentMethodDetail;
import io.restaurant.platform.modules.payment.entity.PaymentOrder;
import io.restaurant.platform.modules.payment.enums.PaymentMethod;
import io.restaurant.platform.modules.payment.repository.PaymentRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
    private static final BigDecimal ONE_PLUS_IGV = new BigDecimal("1.18");

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public TableAccountResponse getTableAccount(Long tableId, Long restaurantId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + tableId));
        
        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Table not found with id: " + tableId);
        }

        // Get DELIVERED orders from the table
        List<Order> orders = orderRepository.findByTableIdAndStatusOrderByCreatedAtAsc(
                tableId, OrderStatus.DELIVERED
        );

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No delivered orders found for table: " + table.getNumber());
        }

        // Calculate total
        BigDecimal totalWithIgv = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate IGV (tax)
        BigDecimal subtotal = totalWithIgv.divide(ONE_PLUS_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igvAmount = totalWithIgv.subtract(subtotal);

        // Get waiter from the first order
        User waiter = orders.get(0).getWaiter();

        List<OrderAccountResponse> orderResponses = orders.stream()
                .map(this::mapToOrderAccountResponse)
                .collect(Collectors.toList());

        return new TableAccountResponse(
                table.getId(),
                "Mesa " + table.getNumber(),
                orderResponses,
                totalWithIgv,
                subtotal,
                igvAmount,
                new TableAccountResponse.WaiterInfo(
                        waiter.getId(),
                        waiter.getFirstName() + " " + waiter.getLastName()
                )
        );
    }

    @Override
    @Transactional
    public PaymentReceiptResponse processPayment(CreatePaymentRequest request, Long restaurantId, Long userId) {
        // Validate that orders exist and are DELIVERED
        List<Order> orders = orderRepository.findAllById(request.orderIds());
        if (orders.size() != request.orderIds().size()) {
            throw new ResourceNotFoundException("Some orders not found");
        }

        // Validate that all orders are DELIVERED
        boolean allDelivered = orders.stream()
                .allMatch(order -> order.getStatus() == OrderStatus.DELIVERED);
        if (!allDelivered) {
            throw new IllegalStateException("All orders must be in DELIVERED status");
        }

        // Validate that all belong to the same table
        Long tableId = orders.get(0).getTableId();
        boolean sameTable = orders.stream()
                .allMatch(order -> order.getTableId().equals(tableId));
        if (!sameTable) {
            throw new IllegalStateException("All orders must belong to the same table");
        }

        // Calculate total
        BigDecimal totalWithIgv = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validate payment sum
        BigDecimal paymentSum = request.paymentMethods().stream()
                .map(PaymentMethodDetailRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paymentSum.compareTo(totalWithIgv) != 0) {
            throw new IllegalStateException(
                    String.format("Payment sum (%.2f) does not match total (%.2f)",
                            paymentSum, totalWithIgv)
            );
        }

        // Get entities
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));

        User paidBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User waiter = orders.get(0).getWaiter();

        // Calculate IGV (tax)
        BigDecimal subtotal = totalWithIgv.divide(ONE_PLUS_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igvAmount = totalWithIgv.subtract(subtotal);

        // Create Payment
        Payment payment = new Payment();
        payment.setPaymentNumber(generatePaymentNumber(restaurantId));
        payment.setRestaurant(restaurant);
        payment.setTable(table);
        payment.setWaiter(waiter);
        payment.setSubtotal(subtotal);
        payment.setIgvAmount(igvAmount);
        payment.setTotalAmount(totalWithIgv);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaidBy(paidBy);
        payment.setObservations(request.observations());

        // Create payment methods
        for (PaymentMethodDetailRequest methodReq : request.paymentMethods()) {
            PaymentMethodDetail detail = new PaymentMethodDetail();
            detail.setPayment(payment);
            detail.setPaymentMethod(methodReq.method());
            detail.setAmount(methodReq.amount());
            detail.setAmountReceived(methodReq.amountReceived());
            detail.setChangeGiven(methodReq.changeGiven());
            payment.getPaymentMethods().add(detail);
        }

        // Link orders
        for (Order order : orders) {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setPayment(payment);
            paymentOrder.setOrder(order);
            payment.getPaymentOrders().add(paymentOrder);

            // Update order status to PAID
            order.setStatus(OrderStatus.PAID);
        }

        // Save payment (cascade saves details and links)
        payment = paymentRepository.save(payment);
        orderRepository.saveAll(orders);

        // Release table if no more active orders
        checkAndReleaseTable(tableId, restaurantId);

        // Return receipt
        return buildPaymentReceipt(payment, orders, restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentReceiptResponse getPaymentReceipt(Long paymentId, Long restaurantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        if (!payment.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Payment not found with id: " + paymentId);
        }

        List<Order> orders = payment.getPaymentOrders().stream()
                .map(PaymentOrder::getOrder)
                .collect(Collectors.toList());

        Restaurant restaurant = payment.getRestaurant();

        return buildPaymentReceipt(payment, orders, restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Long restaurantId, Pageable pageable) {
        return paymentRepository.findByRestaurantIdOrderByPaidAtDesc(restaurantId, pageable)
                .map(this::mapToPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId, Long restaurantId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        if (!payment.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Payment not found with id: " + paymentId);
        }
        
        return mapToPaymentResponse(payment);
    }

    // ===== PRIVATE METHODS =====

    private String generatePaymentNumber(Long restaurantId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        long todayCount = paymentRepository.countTodayPayments(restaurantId, startOfDay, endOfDay);
        long nextNumber = todayCount + 1;

        return String.format("BOL-%03d", nextNumber);
    }

    private void checkAndReleaseTable(Long tableId, Long restaurantId) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY,
                OrderStatus.DELIVERED
        );

        long activeOrdersCount = orderRepository.countByTableIdAndStatusIn(tableId, activeStatuses);

        if (activeOrdersCount == 0) {
            RestaurantTable table = tableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
            
            if (table.getRestaurant().getId().equals(restaurantId)) {
                table.setStatus(TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
        }
    }

    private PaymentReceiptResponse buildPaymentReceipt(Payment payment, List<Order> orders, Restaurant restaurant) {
        PaymentResponse paymentResponse = mapToPaymentResponse(payment);

        List<OrderAccountResponse> orderResponses = orders.stream()
                .map(this::mapToOrderAccountResponse)
                .collect(Collectors.toList());

        PaymentReceiptResponse.RestaurantInfo restaurantInfo = new PaymentReceiptResponse.RestaurantInfo(
                restaurant.getName(),
                restaurant.getCompanyName(),
                restaurant.getRuc(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getReceiptFooter()
        );

        return new PaymentReceiptResponse(paymentResponse, restaurantInfo, orderResponses);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        List<PaymentMethodDetailResponse> methods = payment.getPaymentMethods().stream()
                .map(detail -> new PaymentMethodDetailResponse(
                        detail.getId(),
                        detail.getPaymentMethod(),
                        detail.getAmount(),
                        detail.getAmountReceived(),
                        detail.getChangeGiven()
                ))
                .collect(Collectors.toList());

        List<Long> orderIds = payment.getPaymentOrders().stream()
                .map(po -> po.getOrder().getId())
                .collect(Collectors.toList());

        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getRestaurant().getId(),
                payment.getTable().getId(),
                "Mesa " + payment.getTable().getNumber(),
                payment.getWaiter().getId(),
                payment.getWaiter().getFirstName() + " " + payment.getWaiter().getLastName(),
                payment.getSubtotal(),
                payment.getIgvAmount(),
                payment.getTotalAmount(),
                payment.getPaidAt(),
                payment.getPaidBy().getId(),
                payment.getPaidBy().getFirstName() + " " + payment.getPaidBy().getLastName(),
                payment.getObservations(),
                methods,
                orderIds
        );
    }

    private OrderAccountResponse mapToOrderAccountResponse(Order order) {
        return new OrderAccountResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new io.restaurant.platform.modules.order.dto.response.OrderItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProduct().getMasterProduct() != null ? 
                                    item.getProduct().getMasterProduct().getName() : "Unknown Product",
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal(),
                                item.getNotes(),
                                item.getIsPartOfMenu(),
                                item.getMenuGroupId(),
                                item.getProduct().getMasterProduct() != null && 
                                    item.getProduct().getMasterProduct().getMasterCategory() != null ? 
                                    item.getProduct().getMasterProduct().getMasterCategory().getName() : null
                        ))
                        .collect(Collectors.toList()),
                order.getTotal()
        );
    }
}
