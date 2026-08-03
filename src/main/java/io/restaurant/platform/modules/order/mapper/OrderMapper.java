package io.restaurant.platform.modules.order.mapper;

import io.restaurant.platform.modules.order.dto.response.OrderResponse;
import io.restaurant.platform.modules.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "tableId", source = "tableId")
    @Mapping(target = "waiterId", source = "waiter.id")
    @Mapping(target = "waiterName", expression = "java(order.getWaiter().getFirstName() + \" \" + order.getWaiter().getLastName())")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order order);
}
