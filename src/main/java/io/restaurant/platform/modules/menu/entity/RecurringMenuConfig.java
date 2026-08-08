package io.restaurant.platform.modules.menu.entity;

import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Recurring Menu Configuration entity - defines which template to use for each day of week
 * 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday
 */
@Getter
@Setter
@Entity
@Table(name = "recurring_menu_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "day_of_week"})
})
public class RecurringMenuConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1=Monday, 2=Tuesday, ..., 7=Sunday

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_template_id", nullable = false)
    private MasterMenuTemplate masterTemplate;
}
