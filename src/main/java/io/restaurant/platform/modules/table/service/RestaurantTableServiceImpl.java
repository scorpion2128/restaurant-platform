package io.restaurant.platform.modules.table.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.modules.table.dto.request.CreateTableRequest;
import io.restaurant.platform.modules.table.dto.request.UpdateTableRequest;
import io.restaurant.platform.modules.table.dto.response.RestaurantTableResponse;
import io.restaurant.platform.modules.table.entity.RestaurantTable;
import io.restaurant.platform.modules.table.enums.TableStatus;
import io.restaurant.platform.modules.table.mapper.RestaurantTableMapper;
import io.restaurant.platform.modules.table.repository.RestaurantTableRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final SecurityContextHelper securityContextHelper;
    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableMapper tableMapper;

    private static final String TABLE_NOT_FOUND = "Table not found";
    private static final String TABLE_NUMBER_EXISTS = "Table number already exists in this restaurant";
    private static final String RESTAURANT_NOT_FOUND = "Restaurant not found";

    @Override
    @Transactional
    public RestaurantTableResponse create(CreateTableRequest request) {
        Long restaurantId = getCurrentRestaurantId();

        if (tableRepository.existsByRestaurantIdAndNumber(restaurantId, request.getNumber())) {
            throw new BusinessException(TABLE_NUMBER_EXISTS);
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND));

        RestaurantTable table = new RestaurantTable();
        table.setRestaurant(restaurant);
        table.setNumber(request.getNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());

        RestaurantTable savedTable = tableRepository.save(table);
        return tableMapper.toResponse(savedTable);
    }

    @Override
    @Transactional
    public RestaurantTableResponse update(Long id, UpdateTableRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TABLE_NOT_FOUND));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException(TABLE_NOT_FOUND);
        }

        if (request.getCapacity() != null) {
            table.setCapacity(request.getCapacity());
        }
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }

        RestaurantTable updatedTable = tableRepository.save(table);
        return tableMapper.toResponse(updatedTable);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long restaurantId = getCurrentRestaurantId();
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TABLE_NOT_FOUND));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException(TABLE_NOT_FOUND);
        }

        tableRepository.delete(table);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTableResponse findById(Long id) {
        Long restaurantId = getCurrentRestaurantId();
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TABLE_NOT_FOUND));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException(TABLE_NOT_FOUND);
        }

        return tableMapper.toResponse(table);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> findAllByRestaurant() {
        Long restaurantId = getCurrentRestaurantId();
        List<RestaurantTable> tables = tableRepository.findByRestaurantIdOrderByNumberAsc(restaurantId);
        return tables.stream()
                .map(tableMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> findByStatus(TableStatus status) {
        Long restaurantId = getCurrentRestaurantId();
        List<RestaurantTable> tables = tableRepository.findByRestaurantIdAndStatus(restaurantId, status);
        return tables.stream()
                .map(tableMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantTableResponse updateStatus(Long id, TableStatus status) {
        Long restaurantId = getCurrentRestaurantId();
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TABLE_NOT_FOUND));

        if (!table.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException(TABLE_NOT_FOUND);
        }

        table.setStatus(status);
        RestaurantTable updatedTable = tableRepository.save(table);
        return tableMapper.toResponse(updatedTable);
    }

    private Long getCurrentRestaurantId() {
        return securityContextHelper.getActiveRestaurantId();
    }
}
