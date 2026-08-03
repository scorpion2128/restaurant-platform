package io.restaurant.platform.modules.product.dto.response;

/**
 * Master Product Category response
 */
public record MasterCategoryResponse(

        Long id,

        Long organizationId,

        String name,

        String description,

        Integer displayOrder,

        Boolean active

) {
}
