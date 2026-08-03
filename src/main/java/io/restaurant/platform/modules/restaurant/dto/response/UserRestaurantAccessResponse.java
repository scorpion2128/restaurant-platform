package io.restaurant.platform.modules.restaurant.dto.response;

import io.restaurant.platform.shared.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestaurantAccessResponse {
    private Long id;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;
    private Long restaurantId;
    private String restaurantName;
}
