package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDailyMenuOverrideRequest(

        @NotNull(message = "Menu date is required")
        LocalDate menuDate,

        @NotNull(message = "Template ID is required")
        Long templateId

) {
}
