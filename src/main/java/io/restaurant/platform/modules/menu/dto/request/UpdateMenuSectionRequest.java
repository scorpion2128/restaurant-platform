package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to update a menu section
 */
public record UpdateMenuSectionRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        Integer displayOrder,

        Boolean visible

) {
}
