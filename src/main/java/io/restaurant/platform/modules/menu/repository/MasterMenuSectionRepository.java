package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.MasterMenuSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterMenuSectionRepository extends JpaRepository<MasterMenuSection, Long> {
    
    List<MasterMenuSection> findByMasterTemplateIdOrderByDisplayOrderAsc(Long templateId);
    
    boolean existsByMasterTemplateIdAndName(Long templateId, String name);
}
