package io.restaurant.platform.modules.menu.mapper;

import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import io.restaurant.platform.modules.menu.entity.DailyMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DailyMenuMapper {

    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "masterTemplate", ignore = true)
    DailyMenu toEntity(CreateDailyMenuRequest request);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "templateId", source = "masterTemplate.id")
    @Mapping(target = "templateName", source = "masterTemplate.name")
    @Mapping(target = "items", ignore = true)
    DailyMenuResponse toResponse(DailyMenu dailyMenu);
}
