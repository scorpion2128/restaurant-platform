package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.CreateMenuSectionRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMenuSectionRequest;
import io.restaurant.platform.modules.menu.dto.response.MenuSectionResponse;

import java.util.List;

public interface MenuSectionService {

    MenuSectionResponse create(Long templateId, CreateMenuSectionRequest request);

    MenuSectionResponse update(Long templateId, Long id, UpdateMenuSectionRequest request);

    void delete(Long templateId, Long id);

    MenuSectionResponse findById(Long templateId, Long id);

    List<MenuSectionResponse> findAllByTemplate(Long templateId);
}
