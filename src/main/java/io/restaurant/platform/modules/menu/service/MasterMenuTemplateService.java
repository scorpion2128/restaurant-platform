package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.AddMasterTemplateItemsRequest;
import io.restaurant.platform.modules.menu.dto.request.CreateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.response.MasterTemplateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing master menu templates
 */
public interface MasterMenuTemplateService {

    MasterTemplateResponse create(CreateMasterTemplateRequest request);

    MasterTemplateResponse update(Long id, UpdateMasterTemplateRequest request);

    void delete(Long id);

    MasterTemplateResponse findById(Long id);

    Page<MasterTemplateResponse> findAllByOrganization(Pageable pageable);

    List<MasterTemplateResponse> findAllActive();

    MasterTemplateResponse addItems(Long templateId, AddMasterTemplateItemsRequest request);

    void removeItem(Long templateId, Long itemId);
}
