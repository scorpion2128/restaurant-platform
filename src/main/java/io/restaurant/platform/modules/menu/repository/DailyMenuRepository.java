package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.DailyMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    Page<DailyMenu> findByRestaurantId(Long restaurantId, Pageable pageable);

    Optional<DailyMenu> findByRestaurantIdAndMenuDate(Long restaurantId, LocalDate menuDate);

    Optional<DailyMenu> findByIdAndRestaurantId(Long id, Long restaurantId);

    boolean existsByRestaurantIdAndMenuDate(Long restaurantId, LocalDate menuDate);

    @Query(
            value = "SELECT * FROM daily_menu " +
                    "WHERE restaurant_id = :restaurantId AND is_override = true " +
                    "ORDER BY menu_date DESC",
            countQuery = "SELECT COUNT(*) FROM daily_menu " +
                    "WHERE restaurant_id = :restaurantId AND is_override = true",
            nativeQuery = true
    )
    Page<DailyMenu> findOverridesOrderedByNearestDate(
            @Param("restaurantId") Long restaurantId,
            Pageable pageable
    );

    List<DailyMenu> findByRestaurantIdAndMenuDateBetween(Long restaurantId, LocalDate startDate, LocalDate endDate);
}
