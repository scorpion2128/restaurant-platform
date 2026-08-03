package io.restaurant.platform.auth.service;

import io.restaurant.platform.auth.dto.request.LoginRequest;
import io.restaurant.platform.auth.dto.request.SelectRestaurantRequest;
import io.restaurant.platform.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    
    LoginResponse selectRestaurant(SelectRestaurantRequest request);
    
    LoginResponse switchRestaurant(SelectRestaurantRequest request);
}