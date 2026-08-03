package io.restaurant.platform.modules.menu.entity;

import io.restaurant.platform.modules.product.entity.Product;
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
 * Daily Menu Item entity - Item in a daily menu
 * Links to Product (which links to master_product for details)
 */
@Getter
@Setter
@Entity
@Table(name = "daily_menu_item")
public class DailyMenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_menu_id", nullable = false)
    private DailyMenu dailyMenu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "price_override", precision = 10, scale = 2)
    private BigDecimal priceOverride;
}
