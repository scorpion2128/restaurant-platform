package io.restaurant.platform.modules.user.service;

import io.restaurant.platform.modules.user.dto.request.CreateUserRequest;
import io.restaurant.platform.modules.user.dto.request.UpdateUserRequest;
import io.restaurant.platform.modules.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse toggleStatus(Long id);

    Page<UserResponse> findAllByRestaurantAndRole(Pageable pageable, String role);

    Optional<UserResponse> findByUsername(String username);

}