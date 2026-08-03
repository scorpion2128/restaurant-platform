package io.restaurant.platform.modules.menu.mapper;

import io.restaurant.platform.modules.menu.dto.request.CreateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.response.MasterTemplateResponse;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplateItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MasterMenuTemplateMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "items", ignore = true)
    MasterMenuTemplate toEntity(CreateMasterTemplateRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "items", expression = "java(mapItems(template.getItems()))")
    MasterTemplateResponse toResponse(MasterMenuTemplate template);

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntity(UpdateMasterTemplateRequest request, @MappingTarget MasterMenuTemplate template);

    default List<MasterTemplateResponse.MasterTemplateItemResponse> mapItems(List<MasterMenuTemplateItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(item -> new MasterTemplateResponse.MasterTemplateItemResponse(
                        item.getId(),
                        item.getMasterProduct() != null ? item.getMasterProduct().getId() : null,
                        item.getMasterProduct() != null ? item.getMasterProduct().getName() : "Unknown",
                        item.getMasterProduct() != null && item.getMasterProduct().getMasterCategory() != null ? 
                            item.getMasterProduct().getMasterCategory().getName() : null,
                        item.getDisplayOrder()
                ))
                .collect(Collectors.toList());
    }
}
