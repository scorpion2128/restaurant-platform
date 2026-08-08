package io.restaurant.platform.modules.menu.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request to add items to a master menu template
 */
public record AddMasterTemplateItemsRequest(

        @NotEmpty(message = "At least one item is required")
        List<MasterTemplateItemRequest> items

) {
    public record MasterTemplateItemRequest(
            @NotNull(message = "Master product ID is required")
            Long masterProductId,
            
            Long sectionId,
            
            Integer displayOrder
    ) {
    }
}
