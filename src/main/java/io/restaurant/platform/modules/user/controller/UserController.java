package io.restaurant.platform.modules.user.controller;

import io.restaurant.platform.modules.user.dto.request.CreateUserRequest;
import io.restaurant.platform.modules.user.dto.request.UpdateUserRequest;
import io.restaurant.platform.modules.user.dto.response.UserResponse;
import io.restaurant.platform.modules.user.service.UserService;
import io.restaurant.platform.shared.api.ApiResponse;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param request the user creation data
     * @return a response containing the created user details with HTTP 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully.", created));
    }

    /**
     * Updates an existing user.
     * Accessible to ADMIN users or the user themselves.
     *
     * @param id      the ID of the user to update
     * @param request the updated user data
     * @return a response containing the updated user details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully.", updated));
    }

    /**
     * Lists all users, optionally filtered by role.
     * Only users with the ADMIN role can access this endpoint.
     *
     * @param pageable pagination information (size, sort, page)
     * @param role     optional role filter (e.g., ADMIN, WAITER, CASHIER, KITCHEN)
     * @return a paginated response containing the list of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @RequestParam(name = "role", required = false) String role) {
        Page<UserResponse> users = userService.findAllByRestaurantAndRole(pageable, role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Toggles the enabled status of a user (active/inactive).
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id the ID of the user to toggle
     * @return a response containing the updated user details
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable("id") Long id) {
        UserResponse updated = userService.toggleStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully.", updated));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return the user's profile data
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserResponse user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return ResponseEntity.ok(ApiResponse.success(user));
    }

}