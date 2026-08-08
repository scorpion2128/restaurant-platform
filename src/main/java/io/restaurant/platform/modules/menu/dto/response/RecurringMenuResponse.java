package io.restaurant.platform.modules.menu.dto.response;

public record RecurringMenuResponse(

        Long id,

        Integer dayOfWeek,

        String dayName,

        Long templateId,

        String templateName

) {
}
