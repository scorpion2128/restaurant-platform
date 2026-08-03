package io.restaurant.platform.modules.order.repository;

import io.restaurant.platform.modules.order.entity.Order;
import io.restaurant.platform.modules.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    List<Order> findByRestaurantIdAndStatusIn(Long restaurantId, List<OrderStatus> statuses);

    Optional<Order> findByIdAndRestaurantId(Long id, Long restaurantId);

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    Page<Order> findByRestaurantIdAndDateRange(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.waiter.id = :waiterId AND o.status IN :statuses")
    List<Order> findByRestaurantIdAndWaiterIdAndStatusIn(
            @Param("restaurantId") Long restaurantId,
            @Param("waiterId") Long waiterId,
            @Param("statuses") List<OrderStatus> statuses
    );

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.tableId = :tableId AND o.status IN :statuses")
    List<Order> findByRestaurantIdAndTableIdAndStatusIn(
            @Param("restaurantId") Long restaurantId,
            @Param("tableId") Long tableId,
            @Param("statuses") List<OrderStatus> statuses
    );

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay ORDER BY o.createdAt DESC")
    List<Order> findOrdersCreatedToday(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    List<Order> findByTableIdAndStatusOrderByCreatedAtAsc(Long tableId, OrderStatus status);

    long countByTableIdAndStatusIn(Long tableId, List<OrderStatus> statuses);
}
