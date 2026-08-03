package io.restaurant.platform.modules.product.mapper;

import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import io.restaurant.platform.modules.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Product Mapper - Simplified for master catalog
 * Product entity only contains price and availability
 * Name, description, and category come from master_product
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "masterProduct", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "masterId", source = "masterProduct.id")
    @Mapping(target = "name", expression = "java(getMasterProductName(product))")
    @Mapping(target = "description", expression = "java(getMasterProductDescription(product))")
    @Mapping(target = "categoryId", expression = "java(getCategoryId(product))")
    @Mapping(target = "categoryName", expression = "java(getCategoryName(product))")
    ProductResponse toResponse(Product product);

    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "masterProduct", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    default String getMasterProductName(Product product) {
        return product.getMasterProduct() != null ? 
                product.getMasterProduct().getName() : null;
    }

    default String getMasterProductDescription(Product product) {
        return product.getMasterProduct() != null ? 
                product.getMasterProduct().getDescription() : null;
    }

    default Long getCategoryId(Product product) {
        return product.getMasterProduct() != null && 
               product.getMasterProduct().getMasterCategory() != null ?
                product.getMasterProduct().getMasterCategory().getId() : null;
    }

    default String getCategoryName(Product product) {
        return product.getMasterProduct() != null && 
               product.getMasterProduct().getMasterCategory() != null ?
                product.getMasterProduct().getMasterCategory().getName() : null;
    }
}
