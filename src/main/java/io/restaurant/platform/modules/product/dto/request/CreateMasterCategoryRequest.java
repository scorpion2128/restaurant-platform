package io.restaurant.platform.modules.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to create a master product category
 */
public record CreateMasterCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 80, message = "Category name must not exceed 80 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        Integer displayOrder,

        Boolean active

) {
}
