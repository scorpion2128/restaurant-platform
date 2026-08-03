package io.restaurant.platform.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.modules.user.entity.UserRestaurantAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate token with organization and restaurant access information.
     * activeRestaurantId and activeRestaurantRole are null until user selects a restaurant.
     */
    public String generateToken(Authentication authentication) {
        return generateToken(authentication, null, null);
    }

    /**
     * Generate token with specific active restaurant and role.
     * Used when user selects or switches restaurant.
     */
    public String generateToken(Authentication authentication, Long activeRestaurantId, String activeRestaurantRole) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();

        if (userPrincipal instanceof User user) {
            claims.put("username", user.getUsername());
            claims.put("firstName", user.getFirstName());
            claims.put("lastName", user.getLastName());
            claims.put("userId", user.getId());
            claims.put("organizationId", user.getOrganization().getId());
            
            // Active restaurant (selected by user)
            claims.put("activeRestaurantId", activeRestaurantId);
            claims.put("activeRestaurantRole", activeRestaurantRole);
            
            // All restaurant access (for validation)
            List<Map<String, Object>> restaurantAccess = user.getRestaurantAccess().stream()
                .map(access -> {
                    Map<String, Object> accessMap = new HashMap<>();
                    accessMap.put("restaurantId", access.getRestaurant().getId());
                    accessMap.put("role", access.getRole().name());
                    return accessMap;
                })
                .collect(Collectors.toList());
            claims.put("restaurantAccess", restaurantAccess);
        }

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getActiveRestaurantId(String token) {
        Claims claims = getClaimsFromToken(token);
        Object restaurantId = claims.get("activeRestaurantId");
        return restaurantId != null ? ((Number) restaurantId).longValue() : null;
    }

    public String getActiveRestaurantRole(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("activeRestaurantRole", String.class);
    }

    public Long getOrganizationId(String token) {
        Claims claims = getClaimsFromToken(token);
        Object orgId = claims.get("organizationId");
        return orgId != null ? ((Number) orgId).longValue() : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}