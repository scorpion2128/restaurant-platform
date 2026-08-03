package io.restaurant.platform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    
    // User info
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    
    // Organization info
    private Long organizationId;
    private String organizationName;
    
    // Restaurant access
    private List<RestaurantAccessResponse> availableRestaurants;
    private RestaurantAccessResponse activeRestaurant; // null if user needs to select one
}
