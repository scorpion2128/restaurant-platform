package io.restaurant.platform.modules.restaurant.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRestaurantRequest(

        @NotBlank
        @Size(max = 250)
        String name,

        @Size(max = 20)
        String ruc,

        @Size(max = 30)
        String phone,

        @Email
        @Size(max = 120)
        String email,

        String address

) {
}