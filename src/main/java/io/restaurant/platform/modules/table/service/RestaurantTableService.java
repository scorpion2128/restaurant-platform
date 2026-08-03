package io.restaurant.platform.modules.table.service;

import io.restaurant.platform.modules.table.dto.request.CreateTableRequest;
import io.restaurant.platform.modules.table.dto.request.UpdateTableRequest;
import io.restaurant.platform.modules.table.dto.response.RestaurantTableResponse;
import io.restaurant.platform.modules.table.enums.TableStatus;

import java.util.List;

public interface RestaurantTableService {

    RestaurantTableResponse create(CreateTableRequest request);

    RestaurantTableResponse update(Long id, UpdateTableRequest request);

    void delete(Long id);

    RestaurantTableResponse findById(Long id);

    List<RestaurantTableResponse> findAllByRestaurant();

    List<RestaurantTableResponse> findByStatus(TableStatus status);

    RestaurantTableResponse updateStatus(Long id, TableStatus status);
}
