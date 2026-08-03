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

import java.util.ArrayList;
import java.util.List;

/**
 * Master Product Category shared across all restaurants in an organization.
 * Provides a centralized catalog that can be used by all restaurant locations (sedes).
 */
@Getter
@Setter
@Entity
@Table(
    name = "master_product_category",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_master_product_category__organization_name",
        columnNames = {"organization_id", "name"}
    )
)
public class MasterProductCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "masterCategory")
    private List<MasterProduct> masterProducts = new ArrayList<>();
}
