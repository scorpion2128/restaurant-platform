package io.restaurant.platform.modules.product.repository;

import io.restaurant.platform.modules.product.entity.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterProductRepository extends JpaRepository<MasterProduct, Long> {
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "ORDER BY mp.name")
    List<MasterProduct> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.active = :active " +
           "ORDER BY mp.name")
    List<MasterProduct> findByOrganizationIdAndActive(
            @Param("organizationId") Long organizationId, 
            @Param("active") Boolean active);
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.masterCategory.id = :categoryId " +
           "ORDER BY mp.name")
    List<MasterProduct> findByOrganizationIdAndMasterCategoryId(
            @Param("organizationId") Long organizationId, 
            @Param("categoryId") Long categoryId);
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.masterCategory.id = :categoryId " +
           "AND mp.active = :active " +
           "ORDER BY mp.name")
    List<MasterProduct> findByOrganizationIdAndMasterCategoryIdAndActive(
            @Param("organizationId") Long organizationId, 
            @Param("categoryId") Long categoryId, 
            @Param("active") Boolean active);
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.active = true " +
           "ORDER BY mp.name")
    List<MasterProduct> findByOrganizationIdAndActiveTrueWithCategory(@Param("organizationId") Long organizationId);
    
    List<MasterProduct> findByOrganizationIdAndActiveTrue(Long organizationId);
    
    List<MasterProduct> findByMasterCategoryId(Long masterCategoryId);
    
    Optional<MasterProduct> findByOrganizationIdAndCode(Long organizationId, String code);
    
    Optional<MasterProduct> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "LEFT JOIN FETCH mp.masterCategory " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.masterCategory.id = :categoryId " +
           "AND mp.active = true " +
           "ORDER BY mp.name")
    List<MasterProduct> findActiveByCategoryAndOrganizationWithCategory(
        @Param("organizationId") Long organizationId,
        @Param("categoryId") Long categoryId
    );
    
    @Query("SELECT mp FROM MasterProduct mp " +
           "WHERE mp.organization.id = :organizationId " +
           "AND mp.masterCategory.id = :categoryId " +
           "AND mp.active = true " +
           "ORDER BY mp.name")
    List<MasterProduct> findActiveByCategoryAndOrganization(
        @Param("organizationId") Long organizationId,
        @Param("categoryId") Long categoryId
    );
}
