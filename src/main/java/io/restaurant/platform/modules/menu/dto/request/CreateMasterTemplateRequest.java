package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create a master menu template
 */
public record CreateMasterTemplateRequest(

        @NotBlank(message = "Template name is required")
        @Size(max = 120, message = "Template name must not exceed 120 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        Boolean active

) {
}
