package io.restaurant.platform.modules.menu.entity;

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

import java.time.LocalDate;

/**
 * Daily Menu entity - Menu for a specific date at a restaurant
 * Uses master_menu_template from master catalog
 */
@Getter
@Setter
@Entity
@Table(name = "daily_menu")
public class DailyMenu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_template_id")
    private MasterMenuTemplate masterTemplate;

    @Column(nullable = false)
    private Boolean active = false;
}
