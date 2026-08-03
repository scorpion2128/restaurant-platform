package io.restaurant.platform.modules.user.repository;

import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // Count users in an organization
    long countByOrganizationId(Long organizationId);

    // Find users by organization and role (via restaurantAccess)
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.restaurantAccess ra " +
           "WHERE ra.restaurant.id = :restaurantId AND ra.role = :role")
    Page<User> findByRestaurantIdAndRole(@Param("restaurantId") Long restaurantId, 
                                          @Param("role") UserRole role, 
                                          Pageable pageable);

    // Find all users with access to a specific restaurant
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN u.restaurantAccess ra " +
           "WHERE ra.restaurant.id = :restaurantId")
    Page<User> findByRestaurantId(@Param("restaurantId") Long restaurantId, Pageable pageable);

}
