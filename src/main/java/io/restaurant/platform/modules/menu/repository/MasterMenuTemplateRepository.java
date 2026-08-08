package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterMenuTemplateRepository extends JpaRepository<MasterMenuTemplate, Long> {
    
    List<MasterMenuTemplate> findByOrganizationId(Long organizationId);
    
    Optional<MasterMenuTemplate> findByIdAndOrganizationId(Long id, Long organizationId);
    
    List<MasterMenuTemplate> findByOrganizationIdAndActiveTrue(Long organizationId);
    
    Optional<MasterMenuTemplate> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
    
    @Query("SELECT DISTINCT t FROM MasterMenuTemplate t " +
           "LEFT JOIN FETCH t.items i " +
           "LEFT JOIN FETCH i.masterProduct p " +
           "LEFT JOIN FETCH p.masterCategory " +
           "LEFT JOIN FETCH i.section " +
           "WHERE t.id = :id")
    Optional<MasterMenuTemplate> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT DISTINCT t FROM MasterMenuTemplate t " +
           "LEFT JOIN FETCH t.items i " +
           "LEFT JOIN FETCH i.masterProduct p " +
           "LEFT JOIN FETCH p.masterCategory " +
           "LEFT JOIN FETCH i.section " +
           "WHERE t.id IN :ids")
    List<MasterMenuTemplate> findAllByIdInWithItems(@Param("ids") List<Long> ids);
}
