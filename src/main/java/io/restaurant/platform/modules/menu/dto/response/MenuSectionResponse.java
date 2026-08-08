package io.restaurant.platform.modules.menu.dto.response;

/**
 * Menu Section Response
 */
public record MenuSectionResponse(

        Long id,

        Long templateId,

        String name,

        String description,

        Integer displayOrder,

        Boolean visible,

        Integer itemCount

) {
}
