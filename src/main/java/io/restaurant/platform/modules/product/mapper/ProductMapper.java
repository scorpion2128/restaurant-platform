package io.restaurant.platform.modules.product.mapper;

import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Product Mapper - Working directly with master_product
 * This mapper is kept for potential future use but not currently used by ProductService
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "masterCategory", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "basePrice", source = "price")
    MasterProduct toEntity(CreateProductRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "price", source = "basePrice")
    @Mapping(target = "categoryId", source = "masterCategory.id")
    @Mapping(target = "categoryName", source = "masterCategory.name")
    ProductResponse toResponse(MasterProduct product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "masterCategory", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "basePrice", source = "price")
    void updateEntity(UpdateProductRequest request, @MappingTarget MasterProduct product);
}
