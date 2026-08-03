package io.restaurant.platform.modules.restaurant.repository;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByRuc(String ruc);
    
    List<Restaurant> findByOrganizationId(Long organizationId);

}