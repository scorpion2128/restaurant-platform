package io.restaurant.platform.modules.menu.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Master Menu Template response with items
 */
public record MasterTemplateResponse(

        Long id,

        Long organizationId,

        String name,

        String description,

        Boolean active,

        List<MasterTemplateItemResponse> items

) {
    public record MasterTemplateItemResponse(
            Long id,
            Long masterProductId,
            String productName,
            BigDecimal productPrice,
            String categoryName,
            Long sectionId,
            String sectionName,
            Integer displayOrder
    ) {
    }
}
