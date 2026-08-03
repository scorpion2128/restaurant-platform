package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateDailyMenuRequest(

        @NotNull(message = "Menu date is required")
        LocalDate menuDate,

        Long templateId,

        @NotEmpty(message = "At least one item is required")
        @Valid
        List<MenuItemRequest> items,

        Boolean active

) {
}
