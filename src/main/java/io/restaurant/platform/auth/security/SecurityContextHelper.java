package io.restaurant.platform.auth.security;

import io.restaurant.platform.auth.jwt.JwtTokenProvider;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper component to get security context information.
 * Provides access to the current user and active restaurant from JWT token.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Get the current authenticated user.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new ResourceNotFoundException("No authenticated user found");
    }

    /**
     * Get the active restaurant ID from JWT token.
     * This is the restaurant the user has selected to work with.
     */
    public Long getActiveRestaurantId() {
        String token = extractTokenFromRequest();
        if (token != null) {
            Long restaurantId = jwtTokenProvider.getActiveRestaurantId(token);
            if (restaurantId != null) {
                return restaurantId;
            }
        }
        throw new ResourceNotFoundException("No active restaurant selected. Please select a restaurant first.");
    }

    /**
     * Get the active restaurant role from JWT token.
     */
    public String getActiveRestaurantRole() {
        String token = extractTokenFromRequest();
        if (token != null) {
            String role = jwtTokenProvider.getActiveRestaurantRole(token);
            if (role != null) {
                return role;
            }
        }
        throw new ResourceNotFoundException("No active restaurant role found");
    }

    /**
     * Get the organization ID from JWT token.
     */
    public Long getOrganizationId() {
        String token = extractTokenFromRequest();
        if (token != null) {
            Long orgId = jwtTokenProvider.getOrganizationId(token);
            if (orgId != null) {
                return orgId;
            }
        }
        throw new ResourceNotFoundException("No organization found");
    }

    /**
     * Get the user ID from current authentication.
     */
    public Long getUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if user has access to a specific restaurant.
     */
    public boolean hasAccessToRestaurant(Long restaurantId) {
        User user = getCurrentUser();
        return user.getRestaurantAccess().stream()
                .anyMatch(access -> access.getRestaurant().getId().equals(restaurantId));
    }

    /**
     * Verify user has access to the active restaurant.
     */
    public void verifyActiveRestaurantAccess() {
        Long activeRestaurantId = getActiveRestaurantId();
        if (!hasAccessToRestaurant(activeRestaurantId)) {
            throw new ResourceNotFoundException("User does not have access to active restaurant");
        }
    }

    /**
     * Extract JWT token from current HTTP request.
     */
    private String extractTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String bearerToken = attributes.getRequest().getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }
}
