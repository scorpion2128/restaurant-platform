package io.restaurant.platform.modules.product.repository;

import io.restaurant.platform.modules.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository - Simplified for master catalog
 * Queries use master_product for filtering
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByRestaurantId(Long restaurantId, Pageable pageable);

    boolean existsByRestaurantIdAndMasterProductId(Long restaurantId, Long masterProductId);

    Optional<Product> findByRestaurantIdAndMasterProductId(Long restaurantId, Long masterProductId);

    @Query("SELECT p FROM Product p " +
           "WHERE p.restaurant.id = :restaurantId " +
           "AND p.masterProduct.masterCategory.id = :categoryId")
    Page<Product> findByRestaurantIdAndMasterProductCategoryId(
            @Param("restaurantId") Long restaurantId,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "WHERE p.restaurant.id = :restaurantId " +
           "AND p.available = :available")
    Page<Product> findByRestaurantIdAndAvailable(
            @Param("restaurantId") Long restaurantId,
            @Param("available") Boolean available,
            Pageable pageable);

    List<Product> findByIdIn(List<Long> ids);
}
