package io.restaurant.platform.modules.table.controller;

import io.restaurant.platform.modules.table.dto.request.CreateTableRequest;
import io.restaurant.platform.modules.table.dto.request.UpdateTableRequest;
import io.restaurant.platform.modules.table.dto.response.RestaurantTableResponse;
import io.restaurant.platform.modules.table.enums.TableStatus;
import io.restaurant.platform.modules.table.service.RestaurantTableService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final RestaurantTableService tableService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> createTable(
            @Valid @RequestBody CreateTableRequest request) {
        RestaurantTableResponse response = tableService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Table created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> updateTable(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTableRequest request) {
        RestaurantTableResponse response = tableService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Table updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable("id") Long id) {
        tableService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Table deleted successfully", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> getTableById(@PathVariable("id") Long id) {
        RestaurantTableResponse response = tableService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> getAllTables() {
        List<RestaurantTableResponse> response = tableService.findAllByRestaurant();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> getTablesByStatus(
            @PathVariable("status") TableStatus status) {
        List<RestaurantTableResponse> response = tableService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> updateTableStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") TableStatus status) {
        RestaurantTableResponse response = tableService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Table status updated successfully", response));
    }
}
