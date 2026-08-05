package io.restaurant.platform.modules.product.repository;

import io.restaurant.platform.modules.product.entity.MasterProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterProductCategoryRepository extends JpaRepository<MasterProductCategory, Long> {
    
    List<MasterProductCategory> findByOrganizationId(Long organizationId);
    
    List<MasterProductCategory> findByOrganizationIdAndActiveTrue(Long organizationId);
    
    Optional<MasterProductCategory> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);
}
