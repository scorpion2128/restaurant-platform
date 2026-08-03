package io.restaurant.platform.modules.table.mapper;

import io.restaurant.platform.modules.table.dto.response.RestaurantTableResponse;
import io.restaurant.platform.modules.table.entity.RestaurantTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantTableMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    RestaurantTableResponse toResponse(RestaurantTable table);
}
