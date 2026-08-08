package io.restaurant.platform.modules.menu.entity;

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
 * Section within a Master Menu Template.
 * Used to organize menu items into groups (e.g., Entradas, Platos Fuertes, Postres)
 */
@Getter
@Setter
@Entity
@Table(
    name = "master_menu_section",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_master_menu_section__template_name",
        columnNames = {"master_template_id", "name"}
    )
)
public class MasterMenuSection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_template_id", nullable = false)
    private MasterMenuTemplate masterTemplate;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean visible = true;

    @OneToMany(mappedBy = "section")
    private List<MasterMenuTemplateItem> items = new ArrayList<>();
}
