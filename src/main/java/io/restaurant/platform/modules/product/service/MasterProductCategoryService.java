package io.restaurant.platform.modules.product.service;

import io.restaurant.platform.modules.product.dto.request.CreateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.response.MasterCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing master product categories
 */
public interface MasterProductCategoryService {

    MasterCategoryResponse create(CreateMasterCategoryRequest request);

    MasterCategoryResponse update(Long id, UpdateMasterCategoryRequest request);

    void delete(Long id);

    MasterCategoryResponse findById(Long id);

    Page<MasterCategoryResponse> findAllByOrganization(Pageable pageable);

    List<MasterCategoryResponse> findAllActive();
}
