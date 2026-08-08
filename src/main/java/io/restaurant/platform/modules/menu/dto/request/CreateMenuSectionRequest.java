package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to create a menu section
 */
public record CreateMenuSectionRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        Integer displayOrder,

        Boolean visible

) {
}
