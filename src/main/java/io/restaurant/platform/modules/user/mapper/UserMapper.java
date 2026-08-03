package io.restaurant.platform.modules.user.mapper;

import io.restaurant.platform.modules.user.dto.request.CreateUserRequest;
import io.restaurant.platform.modules.user.dto.request.UpdateUserRequest;
import io.restaurant.platform.modules.user.dto.response.UserResponse;
import io.restaurant.platform.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequest request);

    @Mapping(target = "organizationId", source = "user.organization.id")
    @Mapping(target = "organizationName", source = "user.organization.name")
    @Mapping(target = "restaurantAccess", source = "user", qualifiedByName = "mapRestaurantAccess")
    UserResponse toResponse(User user);

    void updateEntity(
            UpdateUserRequest request,
            @MappingTarget User user);

    @Named("mapRestaurantAccess")
    default List<UserResponse.RestaurantAccessInfo> mapRestaurantAccess(User user) {
        return user.getRestaurantAccess().stream()
            .map(access -> new UserResponse.RestaurantAccessInfo(
                access.getRestaurant().getId(),
                access.getRestaurant().getName(),
                access.getRole()
            ))
            .collect(Collectors.toList());
    }

}
