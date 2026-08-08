package io.restaurant.platform.modules.menu.dto.response;

import java.math.BigDecimal;

public record MenuItemResponse(

        Long id,

        Long productId,

        String productName,

        BigDecimal productPrice,

        Long sectionId,

        String sectionName

) {
}
