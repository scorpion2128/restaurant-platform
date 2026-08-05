package io.restaurant.platform.modules.product.mapper;

import io.restaurant.platform.modules.product.dto.request.CreateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.response.MasterCategoryResponse;
import io.restaurant.platform.modules.product.entity.MasterProductCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MasterProductCategoryMapper {

    @Mapping(target = "organization", ignore = true)
    MasterProductCategory toEntity(CreateMasterCategoryRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    MasterCategoryResponse toResponse(MasterProductCategory category);

    @Mapping(target = "organization", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateMasterCategoryRequest request, @MappingTarget MasterProductCategory category);
}
