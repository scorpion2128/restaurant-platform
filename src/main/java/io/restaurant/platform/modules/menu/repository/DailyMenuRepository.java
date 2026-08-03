package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.DailyMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    Page<DailyMenu> findByRestaurantId(Long restaurantId, Pageable pageable);

    Optional<DailyMenu> findByRestaurantIdAndMenuDate(Long restaurantId, LocalDate menuDate);

    boolean existsByRestaurantIdAndMenuDate(Long restaurantId, LocalDate menuDate);

    Optional<DailyMenu> findByRestaurantIdAndActiveTrue(Long restaurantId);
}
