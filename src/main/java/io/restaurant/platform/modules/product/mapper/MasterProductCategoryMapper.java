package io.restaurant.platform.modules.product.mapper;

import io.restaurant.platform.modules.product.dto.request.CreateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.response.MasterCategoryResponse;
import io.restaurant.platform.modules.product.entity.MasterProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MasterProductCategoryMapper {

    @Mapping(target = "organization", ignore = true)
    MasterProductCategory toEntity(CreateMasterCategoryRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    MasterCategoryResponse toResponse(MasterProductCategory category);

    @Mapping(target = "organization", ignore = true)
    void updateEntity(UpdateMasterCategoryRequest request, @MappingTarget MasterProductCategory category);
}
