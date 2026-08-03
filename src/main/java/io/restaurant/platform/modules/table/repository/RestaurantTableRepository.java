package io.restaurant.platform.modules.table.repository;

import io.restaurant.platform.modules.table.entity.RestaurantTable;
import io.restaurant.platform.modules.table.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    List<RestaurantTable> findByRestaurantIdOrderByNumberAsc(Long restaurantId);

    List<RestaurantTable> findByRestaurantIdAndStatus(Long restaurantId, TableStatus status);

    Optional<RestaurantTable> findByRestaurantIdAndNumber(Long restaurantId, Integer number);

    boolean existsByRestaurantIdAndNumber(Long restaurantId, Integer number);
}
