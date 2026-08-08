package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.RecurringMenuConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringMenuConfigRepository extends JpaRepository<RecurringMenuConfig, Long> {

    @Query("SELECT r FROM RecurringMenuConfig r " +
           "LEFT JOIN FETCH r.masterTemplate " +
           "WHERE r.restaurant.id = :restaurantId " +
           "ORDER BY r.dayOfWeek")
    List<RecurringMenuConfig> findAllByRestaurantIdWithTemplate(@Param("restaurantId") Long restaurantId);

    @Query("SELECT r FROM RecurringMenuConfig r " +
           "LEFT JOIN FETCH r.masterTemplate " +
           "WHERE r.restaurant.id = :restaurantId AND r.dayOfWeek = :dayOfWeek")
    Optional<RecurringMenuConfig> findByRestaurantIdAndDayOfWeekWithTemplate(
            @Param("restaurantId") Long restaurantId, 
            @Param("dayOfWeek") Integer dayOfWeek);

    boolean existsByRestaurantIdAndDayOfWeek(Long restaurantId, Integer dayOfWeek);

    void deleteByRestaurantIdAndDayOfWeek(Long restaurantId, Integer dayOfWeek);

    Optional<RecurringMenuConfig> findByRestaurantIdAndDayOfWeek(Long restaurantId, Integer dayOfWeek);
}
