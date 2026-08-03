package io.restaurant.platform.modules.menu.entity;

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
 * Master Menu Template shared across all restaurants in an organization.
 * Provides a centralized menu definition that can be used by all restaurant locations.
 */
@Getter
@Setter
@Entity
@Table(
    name = "master_menu_template",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_master_menu_template__organization_name",
        columnNames = {"organization_id", "name"}
    )
)
public class MasterMenuTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "masterTemplate")
    private List<MasterMenuTemplateItem> items = new ArrayList<>();
}
