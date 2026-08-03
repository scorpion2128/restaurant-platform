package io.restaurant.platform.modules.organization.repository;

import io.restaurant.platform.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByRuc(String ruc);
    
    boolean existsByRuc(String ruc);
}
