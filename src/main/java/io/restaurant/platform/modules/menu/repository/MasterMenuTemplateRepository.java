package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterMenuTemplateRepository extends JpaRepository<MasterMenuTemplate, Long> {
    
    List<MasterMenuTemplate> findByOrganizationId(Long organizationId);
    
    List<MasterMenuTemplate> findByOrganizationIdAndActiveTrue(Long organizationId);
    
    Optional<MasterMenuTemplate> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
