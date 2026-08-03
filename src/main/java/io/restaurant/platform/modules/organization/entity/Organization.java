package io.restaurant.platform.modules.organization.entity;

import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization represents a business entity (empresa/negocio) that owns
 * multiple restaurant locations (sedes).
 */
@Getter
@Setter
@Entity
@Table(name = "organization")
public class Organization extends BaseEntity {

    @Column(nullable = false, length = 250)
    private String name;

    @Column(name = "company_name", nullable = false, length = 250)
    private String companyName;

    @Column(length = 20)
    private String ruc;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "organization")
    private List<Restaurant> restaurants = new ArrayList<>();
}
