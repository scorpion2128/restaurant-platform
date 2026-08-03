package io.restaurant.platform.modules.restaurant.entity;

import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant")
public class Restaurant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

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

    @Column(name = "receipt_footer", columnDefinition = "TEXT")
    private String receiptFooter;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

}