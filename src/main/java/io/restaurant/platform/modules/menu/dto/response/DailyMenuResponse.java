package io.restaurant.platform.modules.menu.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyMenuResponse(

        Long id,

        Long restaurantId,

        LocalDate menuDate,

        Long templateId,

        String templateName,

        Boolean isOverride,

        List<MenuItemResponse> items

) {
}
