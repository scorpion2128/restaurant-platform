package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.MasterMenuTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterMenuTemplateItemRepository extends JpaRepository<MasterMenuTemplateItem, Long> {
    
    List<MasterMenuTemplateItem> findByMasterTemplateId(Long masterTemplateId);
    
    void deleteByMasterTemplateId(Long masterTemplateId);
}
