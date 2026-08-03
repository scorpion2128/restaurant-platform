package io.restaurant.platform.auth.controller;

import io.restaurant.platform.auth.dto.request.LoginRequest;
import io.restaurant.platform.auth.dto.request.SelectRestaurantRequest;
import io.restaurant.platform.auth.dto.response.LoginResponse;
import io.restaurant.platform.auth.service.AuthService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/select-restaurant")
    public ResponseEntity<ApiResponse<LoginResponse>> selectRestaurant(@Valid @RequestBody SelectRestaurantRequest request) {
        LoginResponse response = authService.selectRestaurant(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/switch-restaurant")
    public ResponseEntity<ApiResponse<LoginResponse>> switchRestaurant(@Valid @RequestBody SelectRestaurantRequest request) {
        LoginResponse response = authService.switchRestaurant(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}