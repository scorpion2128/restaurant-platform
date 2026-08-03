package io.restaurant.platform.modules.payment.repository;

import io.restaurant.platform.modules.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdAndRestaurantId(Long id, Long restaurantId);

    Page<Payment> findByRestaurantIdOrderByPaidAtDesc(Long restaurantId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.restaurant.id = :restaurantId AND p.paidAt >= :startOfDay AND p.paidAt < :endOfDay ORDER BY p.paidAt DESC")
    Page<Payment> findByRestaurantIdAndPaidAtBetween(
            @Param("restaurantId") Long restaurantId, 
            @Param("startOfDay") LocalDateTime startOfDay, 
            @Param("endOfDay") LocalDateTime endOfDay, 
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.restaurant.id = :restaurantId AND p.paidAt >= :startOfDay AND p.paidAt < :endOfDay")
    long countTodayPayments(
            @Param("restaurantId") Long restaurantId, 
            @Param("startOfDay") LocalDateTime startOfDay, 
            @Param("endOfDay") LocalDateTime endOfDay);
}
