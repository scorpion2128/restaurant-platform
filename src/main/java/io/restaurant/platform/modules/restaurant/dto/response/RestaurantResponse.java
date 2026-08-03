package io.restaurant.platform.modules.restaurant.dto.response;

public record RestaurantResponse(

        Long id,

        String name,

        String companyName,

        String ruc,

        String phone,

        String email,

        String address,

        String receiptFooter,

        Boolean active

) {
}