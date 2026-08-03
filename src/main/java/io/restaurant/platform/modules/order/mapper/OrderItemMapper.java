package io.restaurant.platform.modules.order.mapper;

import io.restaurant.platform.modules.order.dto.response.OrderItemResponse;
import io.restaurant.platform.modules.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", expression = "java(getProductName(orderItem))")
    @Mapping(target = "sectionName", expression = "java(getCategoryName(orderItem))")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(orderItem))")
    OrderItemResponse toResponse(OrderItem orderItem);

    default String getProductName(OrderItem orderItem) {
        return orderItem.getProduct() != null && orderItem.getProduct().getMasterProduct() != null ?
                orderItem.getProduct().getMasterProduct().getName() : "Unknown Product";
    }

    default String getCategoryName(OrderItem orderItem) {
        return orderItem.getProduct() != null && 
               orderItem.getProduct().getMasterProduct() != null && 
               orderItem.getProduct().getMasterProduct().getMasterCategory() != null ?
                orderItem.getProduct().getMasterProduct().getMasterCategory().getName() : null;
    }

    default BigDecimal calculateSubtotal(OrderItem orderItem) {
        return orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }
}
