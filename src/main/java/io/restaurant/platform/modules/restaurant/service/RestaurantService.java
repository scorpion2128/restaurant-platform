package io.restaurant.platform.modules.restaurant.service;

import io.restaurant.platform.modules.restaurant.dto.request.AssignUserToRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.CreateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantSettingsRequest;
import io.restaurant.platform.modules.restaurant.dto.response.RestaurantResponse;
import io.restaurant.platform.modules.restaurant.dto.response.UserRestaurantAccessResponse;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse create(CreateRestaurantRequest request);

    RestaurantResponse update(Long id, UpdateRestaurantRequest request);

    RestaurantResponse updateSettings(Long id, UpdateRestaurantSettingsRequest request);

    RestaurantResponse findById(Long id);

    List<RestaurantResponse> findAll();
    
    List<RestaurantResponse> findByOrganization(Long organizationId);

    void delete(Long id);
    
    // User management in restaurants
    UserRestaurantAccessResponse assignUser(Long restaurantId, AssignUserToRestaurantRequest request);
    
    void removeUser(Long restaurantId, Long userId);
    
    List<UserRestaurantAccessResponse> findUsersInRestaurant(Long restaurantId);

}