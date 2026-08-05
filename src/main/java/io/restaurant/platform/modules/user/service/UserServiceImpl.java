package io.restaurant.platform.modules.user.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.modules.user.dto.request.CreateUserRequest;
import io.restaurant.platform.modules.user.dto.request.UpdateUserRequest;
import io.restaurant.platform.modules.user.dto.response.UserResponse;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.modules.user.entity.UserRestaurantAccess;
import io.restaurant.platform.modules.user.mapper.UserMapper;
import io.restaurant.platform.modules.user.repository.UserRepository;
import io.restaurant.platform.modules.user.repository.UserRestaurantAccessRepository;
import io.restaurant.platform.shared.enums.UserRole;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ForbiddenException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link UserService} interface.
 * Provides business logic for user management operations including creation,
 * updates, and username generation.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String USER_NOT_FOUND = "User with id %d not found.";
    private static final String CANNOT_CHANGE_OWN_ROLE = "You cannot change your own role.";
    private static final String CANNOT_CHANGE_OWN_ENABLED_STATUS = "You cannot change your own enabled status.";
    private static final String INVALID_ROLE = "Invalid role: %s. Allowed values: ADMIN, WAITER, CASHIER, KITCHEN";
    private static final String MAX_USERS_REACHED = "Maximum number of users (9999) reached for organization %d";

    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRestaurantAccessRepository userRestaurantAccessRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with an auto-generated username and a default password.
     * <p>
     * The username is generated in the format: <strong>U{restaurantId}{correlative}</strong>
     * where {@code restaurantId} is the restaurant's ID and {@code correlative}
     * is a zero-padded 4-digit sequential number (e.g., U10001, U10002).
     * <p>
     * The default password is set to {@code "123456"}. It is highly recommended
     * to force the user to change their password on the first login.
     *
     * @param request the user creation data
     * @return a {@link UserResponse} containing the created user details
     * @throws ResourceNotFoundException if the restaurant does not exist
     * @throws BusinessException if the maximum number of users (9999) has been reached
     */
    @Override
    public UserResponse create(CreateUserRequest request) {
        Restaurant restaurant = getRestaurant(request.restaurantId());
        Organization organization = restaurant.getOrganization();
        String username = generateUsername(organization.getId());
        String rawPassword = "123456";

        User user = userMapper.toEntity(request);
        user.setOrganization(organization);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user = userRepository.save(user);

        // Create user-restaurant access
        UserRestaurantAccess access = new UserRestaurantAccess();
        access.setUser(user);
        access.setRestaurant(restaurant);
        access.setRole(request.role());
        userRestaurantAccessRepository.save(access);

        return userMapper.toResponse(user);
    }

    /**
     * Updates an existing user's information.
     * <p>
     * Non-admin users can update their own profile but cannot change their role.
     * Only users with the {@code ROLE_ADMIN} authority can change a user's role.
     *
     * @param id the ID of the user to update
     * @param request the updated user data
     * @return a {@link UserResponse} containing the updated user details
     * @throws ResourceNotFoundException if the user with the given ID does not exist
     * @throws ForbiddenException if a non-admin user attempts to change their own role
     */
    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Non-admin users cannot change their own role
        if (!isAdmin && request.role() != null && request.role() != user.getRole()) {
            throw new ForbiddenException(CANNOT_CHANGE_OWN_ROLE);
        }

        // Only admin users can change the enabled status
        if (!isAdmin && request.enabled() != null && request.enabled() != user.getEnabled()) {
            throw new ForbiddenException(CANNOT_CHANGE_OWN_ENABLED_STATUS);
        }

        userMapper.updateEntity(request, user);
        return userMapper.toResponse(user);
    }

    /**
     * Toggles the enabled status of a user.
     * Only accessible to users with the ADMIN role.
     *
     * @param id the ID of the user whose status will be toggled
     * @return a {@link UserResponse} containing the updated user details
     * @throws ResourceNotFoundException if the user with the given ID does not exist
     */
    @Override
    public UserResponse toggleStatus(Long id) {
        User user = getUser(id);
        user.setEnabled(!user.getEnabled());
        return userMapper.toResponse(user);
    }

    /**
     * Retrieves a paginated list of users belonging to the same restaurant as the currently authenticated user.
     * Optionally filters the results by a specific role.
     *
     * @param pageable pagination information (page number, size, sorting)
     * @param role     optional role to filter users (e.g., ADMIN, WAITER, CASHIER, KITCHEN).
     * @return a {@link Page} of {@link UserResponse} containing the filtered user data
     * @throws BusinessException if the provided role is not a valid {@link UserRole}
     */
    @Override
    public Page<UserResponse> findAllByRestaurantAndRole(Pageable pageable, String role) {
        Long restaurantId = securityContextHelper.getActiveRestaurantId();

        // Get users with access to the active restaurant
        List<UserRestaurantAccess> accessList;
        if (role != null && !role.isBlank()) {
            try {
                UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(INVALID_ROLE.formatted(role));
            }
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            accessList = userRestaurantAccessRepository.findByRestaurantId(restaurantId).stream()
                .filter(access -> access.getRole() == userRole)
                .collect(Collectors.toList());
        } else {
            accessList = userRestaurantAccessRepository.findByRestaurantId(restaurantId);
        }

        // Extract unique users and map to response
        List<User> users = accessList.stream()
            .map(UserRestaurantAccess::getUser)
            .distinct()
            .collect(Collectors.toList());

        // For now, return all results without pagination (simplified)
        List<UserResponse> userResponses = users.stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());

        return org.springframework.data.support.PageableExecutionUtils.getPage(
            userResponses,
            pageable,
            userResponses::size
        );
    }

    @Override
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user ID
     * @return the {@link User} entity
     * @throws ResourceNotFoundException if the user does not exist
     */
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(id)));
    }

    /**
     * Retrieves a restaurant by its ID.
     *
     * @param id the restaurant ID
     * @return the {@link Restaurant} entity
     * @throws ResourceNotFoundException if the restaurant does not exist
     */
    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(id)));
    }

    /**
     * Generates a unique username for a user in a given restaurant.
     * <p>
     * The username format is <strong>U{restaurantId}{correlative}</strong>,
     * where {@code restaurantId} is the restaurant's ID and {@code correlative}
     * is a zero-padded 4-digit sequential number starting from 0001.
     * <p>
     * Examples: U10001, U10002, U20001, etc.
     *
     * @param organizationId the ID of the organization
     * @return a unique username for the new user
     * @throws BusinessException if the maximum number of users (9999) has been reached for the organization
     */
    private String generateUsername(Long organizationId) {
        long count = userRepository.countByOrganizationId(organizationId);
        long nextNumber = count + 1;

        if (nextNumber > 9999) {
            throw new BusinessException(MAX_USERS_REACHED.formatted(organizationId));
        }

        String correlative = String.format("%04d", nextNumber);
        return "U" + organizationId + correlative;
    }

}