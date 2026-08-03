package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateDailyMenuRequest(

        Long templateId,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<MenuItemRequest> items,

        Boolean active

) {
}
