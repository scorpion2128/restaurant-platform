package io.restaurant.platform.modules.product.entity;

import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Master Product represents a product in the centralized catalog.
 * This product definition is shared across all restaurants in an organization.
 * Individual restaurants can reference this master product and optionally override
 * the price or other attributes.
 */
@Getter
@Setter
@Entity
@Table(
    name = "master_product",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_master_product__organization_name",
        columnNames = {"organization_id", "name"}
    )
)
public class MasterProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_category_id", nullable = false)
    private MasterProductCategory masterCategory;

    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "masterProduct")
    private List<Product> products = new ArrayList<>();
}
