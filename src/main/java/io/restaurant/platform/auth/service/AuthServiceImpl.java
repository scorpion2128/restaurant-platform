package io.restaurant.platform.auth.service;

import io.restaurant.platform.auth.dto.request.LoginRequest;
import io.restaurant.platform.auth.dto.request.SelectRestaurantRequest;
import io.restaurant.platform.auth.dto.response.LoginResponse;
import io.restaurant.platform.auth.dto.response.RestaurantAccessResponse;
import io.restaurant.platform.auth.jwt.JwtTokenProvider;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.modules.user.entity.UserRestaurantAccess;
import io.restaurant.platform.modules.user.repository.UserRestaurantAccessRepository;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRestaurantAccessRepository userRestaurantAccessRepository;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        
        // Get user's restaurant access
        List<UserRestaurantAccess> access = userRestaurantAccessRepository.findByUserId(user.getId());
        
        if (access.isEmpty()) {
            throw new ResourceNotFoundException("User has no restaurant access assigned");
        }
        
        // Convert to DTOs
        List<RestaurantAccessResponse> availableRestaurants = access.stream()
            .map(a -> new RestaurantAccessResponse(
                a.getRestaurant().getId(),
                a.getRestaurant().getName(),
                a.getRole()))
            .collect(Collectors.toList());
        
        // If user has only one restaurant, auto-select it
        RestaurantAccessResponse activeRestaurant = null;
        String token;
        
        if (availableRestaurants.size() == 1) {
            activeRestaurant = availableRestaurants.get(0);
            token = tokenProvider.generateToken(authentication, activeRestaurant.restaurantId(), activeRestaurant.role().name());
        } else {
            // User needs to select a restaurant
            token = tokenProvider.generateToken(authentication);
        }
        
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .availableRestaurants(availableRestaurants)
                .activeRestaurant(activeRestaurant)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse selectRestaurant(SelectRestaurantRequest request) {
        return switchRestaurant(request);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse switchRestaurant(SelectRestaurantRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        // Verify user has access to this restaurant
        UserRestaurantAccess access = userRestaurantAccessRepository
                .findByUserIdAndRestaurantId(user.getId(), request.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("User does not have access to restaurant ID: " + request.restaurantId()));
        
        // Get all restaurant access
        List<UserRestaurantAccess> allAccess = userRestaurantAccessRepository.findByUserId(user.getId());
        List<RestaurantAccessResponse> availableRestaurants = allAccess.stream()
            .map(a -> new RestaurantAccessResponse(
                a.getRestaurant().getId(),
                a.getRestaurant().getName(),
                a.getRole()))
            .collect(Collectors.toList());
        
        RestaurantAccessResponse activeRestaurant = new RestaurantAccessResponse(
                access.getRestaurant().getId(),
                access.getRestaurant().getName(),
                access.getRole());
        
        // Generate new token with selected restaurant
        String token = tokenProvider.generateToken(authentication, activeRestaurant.restaurantId(), activeRestaurant.role().name());
        
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .availableRestaurants(availableRestaurants)
                .activeRestaurant(activeRestaurant)
                .build();
    }
}