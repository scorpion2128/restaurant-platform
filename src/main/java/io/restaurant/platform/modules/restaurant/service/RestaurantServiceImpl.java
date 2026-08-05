package io.restaurant.platform.modules.restaurant.service;

import io.restaurant.platform.modules.user.entity.UserRestaurantAccess;
import io.restaurant.platform.modules.user.repository.UserRestaurantAccessRepository;
import io.restaurant.platform.modules.restaurant.dto.request.AssignUserToRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.CreateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantRequest;
import io.restaurant.platform.modules.restaurant.dto.request.UpdateRestaurantSettingsRequest;
import io.restaurant.platform.modules.restaurant.dto.response.RestaurantResponse;
import io.restaurant.platform.modules.restaurant.dto.response.UserRestaurantAccessResponse;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.mapper.RestaurantMapper;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.modules.user.entity.User;
import io.restaurant.platform.modules.user.repository.UserRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String USER_NOT_FOUND = "User with id %d not found.";
    private static final String USER_ORGANIZATION_MISMATCH = "User does not belong to the same organization as the restaurant.";
    private static final String USER_ALREADY_HAS_ACCESS = "User already has access to this restaurant.";
    private static final String USER_ACCESS_NOT_FOUND = "User access not found for this restaurant.";
    private static final String CANNOT_REMOVE_LAST_ACCESS = "Cannot remove user's last restaurant access.";

    private final RestaurantRepository repository;
    private final RestaurantMapper mapper;
    private final UserRepository userRepository;
    private final UserRestaurantAccessRepository userRestaurantAccessRepository;

    @Override
    public RestaurantResponse create(CreateRestaurantRequest request) {
        Restaurant restaurant = mapper.toEntity(request);
        restaurant = repository.save(restaurant);
        return mapper.toResponse(restaurant);
    }

    @Override
    public RestaurantResponse update(Long id, UpdateRestaurantRequest request) {
        Restaurant restaurant = getRestaurant(id);
        mapper.updateEntity(request, restaurant);
        return mapper.toResponse(repository.save(restaurant));
    }

    @Override
    public RestaurantResponse updateSettings(Long id, UpdateRestaurantSettingsRequest request) {
        Restaurant restaurant = getRestaurant(id);
        restaurant.setReceiptFooter(request.receiptFooter());
        return mapper.toResponse(repository.save(restaurant));
    }

    @Transactional(readOnly = true)
    @Override
    public RestaurantResponse findById(Long id) {
        Restaurant restaurant = getRestaurant(id);
        return mapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RestaurantResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<RestaurantResponse> findByOrganization(Long organizationId) {
        return repository.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        Restaurant restaurant = getRestaurant(id);
        repository.delete(restaurant);
    }
    
    @Override
    public UserRestaurantAccessResponse assignUser(Long restaurantId, AssignUserToRestaurantRequest request) {
        Restaurant restaurant = getRestaurant(restaurantId);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND.formatted(request.userId())));
        
        // Verify user belongs to the same organization
        if (!user.getOrganization().getId().equals(restaurant.getOrganization().getId())) {
            throw new BusinessException(USER_ORGANIZATION_MISMATCH);
        }
        
        // Check if user already has access
        boolean hasAccess = userRestaurantAccessRepository
                .existsByUserIdAndRestaurantId(user.getId(), restaurant.getId());
        if (hasAccess) {
            throw new BusinessException(USER_ALREADY_HAS_ACCESS);
        }
        
        // Create access
        UserRestaurantAccess access = new UserRestaurantAccess();
        access.setUser(user);
        access.setRestaurant(restaurant);
        access.setRole(request.role());
        access = userRestaurantAccessRepository.save(access);
        
        return UserRestaurantAccessResponse.builder()
                .id(access.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(access.getRole())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .build();
    }
    
    @Override
    public void removeUser(Long restaurantId, Long userId) {
        UserRestaurantAccess access = userRestaurantAccessRepository
                .findByUserIdAndRestaurantId(userId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_ACCESS_NOT_FOUND));
        
        // Verify user has at least one other restaurant
        long accessCount = userRestaurantAccessRepository.countByUserId(userId);
        if (accessCount <= 1) {
            throw new BusinessException(CANNOT_REMOVE_LAST_ACCESS);
        }
        
        userRestaurantAccessRepository.delete(access);
    }
    
    @Transactional(readOnly = true)
    @Override
    public List<UserRestaurantAccessResponse> findUsersInRestaurant(Long restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        
        return userRestaurantAccessRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(access -> UserRestaurantAccessResponse.builder()
                        .id(access.getId())
                        .userId(access.getUser().getId())
                        .username(access.getUser().getUsername())
                        .firstName(access.getUser().getFirstName())
                        .lastName(access.getUser().getLastName())
                        .role(access.getRole())
                        .restaurantId(restaurant.getId())
                        .restaurantName(restaurant.getName())
                        .build())
                .toList();
    }

    private Restaurant getRestaurant(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(id)));
    }

}