package io.restaurant.platform.modules.menu.entity;

import io.restaurant.platform.modules.product.entity.MasterProduct;
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
 * Item in a Master Menu Template.
 * Links master menu templates to master products.
 */
@Getter
@Setter
@Entity
@Table(
    name = "master_menu_template_item",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_master_menu_item__template_product",
        columnNames = {"master_template_id", "master_product_id"}
    )
)
public class MasterMenuTemplateItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_template_id", nullable = false)
    private MasterMenuTemplate masterTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_product_id", nullable = false)
    private MasterProduct masterProduct;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
