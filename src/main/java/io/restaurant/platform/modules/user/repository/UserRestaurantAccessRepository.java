package io.restaurant.platform.modules.user.repository;

import io.restaurant.platform.modules.user.entity.UserRestaurantAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRestaurantAccessRepository extends JpaRepository<UserRestaurantAccess, Long> {
    
    List<UserRestaurantAccess> findByUserId(Long userId);
    
    List<UserRestaurantAccess> findByRestaurantId(Long restaurantId);
    
    @Query("SELECT ura FROM UserRestaurantAccess ura WHERE ura.user.id = :userId AND ura.restaurant.id = :restaurantId")
    Optional<UserRestaurantAccess> findByUserIdAndRestaurantId(@Param("userId") Long userId, @Param("restaurantId") Long restaurantId);
    
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);
    
    long countByUserId(Long userId);
    
    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
