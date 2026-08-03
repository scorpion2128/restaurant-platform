package io.restaurant.platform.modules.restaurant.controller;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.restaurant.dto.request.AssignUserToRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.CreateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantSettingsRequest;
import io.restaurant.platform.modules.restaurant.dto.response.RestaurantResponse;
import io.restaurant.platform.modules.restaurant.dto.response.UserRestaurantAccessResponse;
import io.restaurant.platform.modules.restaurant.service.RestaurantService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService service;
    private final SecurityContextHelper securityContextHelper;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @Valid @RequestBody CreateRestaurantRequest request) {

        RestaurantResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Restaurant created successfully.",
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {

        RestaurantResponse response = service.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant updated successfully.",
                        response));
    }

    @PatchMapping("/{id}/settings")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateSettings(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateRestaurantSettingsRequest request) {

        RestaurantResponse response = service.updateSettings(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant settings updated successfully.",
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> findById(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant retrieved successfully.",
                        service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> findAll() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurants retrieved successfully.",
                        service.findAll()));
    }
    
    @GetMapping("/my-organization")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> findByMyOrganization() {
        Long organizationId = securityContextHelper.getOrganizationId();
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurants retrieved successfully.",
                        service.findByOrganization(organizationId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long id) {

        service.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Restaurant deleted successfully."));
    }
    
    // User management endpoints
    
    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<UserRestaurantAccessResponse>> assignUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignUserToRestaurantRequest request) {
        
        UserRestaurantAccessResponse response = service.assignUser(id, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User assigned to restaurant successfully.",
                        response));
    }
    
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeUser(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId) {
        
        service.removeUser(id, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("User removed from restaurant successfully."));
    }
    
    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<UserRestaurantAccessResponse>>> findUsersInRestaurant(
            @PathVariable("id") Long id) {
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users retrieved successfully.",
                        service.findUsersInRestaurant(id)));
    }

}