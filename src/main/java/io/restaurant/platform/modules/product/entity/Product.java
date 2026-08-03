package io.restaurant.platform.modules.product.entity;

import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Product entity - Restaurant-specific product data
 * Links to master_product for name, description, and category.
 * Only stores local price and availability.
 */
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_product_id", nullable = false)
    private MasterProduct masterProduct; // REQUIRED: Link to master catalog

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Restaurant-specific price

    @Column(nullable = false)
    private Boolean available = true; // Restaurant-specific availability
}
