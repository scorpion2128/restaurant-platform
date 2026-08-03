package io.restaurant.platform.modules.user.entity;

import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.shared.entity.BaseEntity;
import io.restaurant.platform.shared.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * UserRestaurantAccess maps users to restaurants with specific roles.
 * This allows users to have access to multiple restaurant locations (sedes)
 * with potentially different roles in each location.
 */
@Getter
@Setter
@Entity
@Table(
    name = "user_restaurant_access",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_restaurant_access__user_restaurant",
        columnNames = {"user_id", "restaurant_id"}
    )
)
public class UserRestaurantAccess extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
}
