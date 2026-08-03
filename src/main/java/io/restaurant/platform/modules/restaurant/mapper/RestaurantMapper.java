package io.restaurant.platform.modules.restaurant.mapper;

import io.restaurant.platform.modules.restaurant.dto.request.CreateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.response.RestaurantResponse;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    Restaurant toEntity(CreateRestaurantRequest request);

    RestaurantResponse toResponse(Restaurant restaurant);

    void updateEntity(
            UpdateRestaurantRequest request,
            @MappingTarget Restaurant restaurant);

}