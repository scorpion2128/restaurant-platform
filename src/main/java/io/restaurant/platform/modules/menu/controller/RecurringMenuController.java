package io.restaurant.platform.modules.menu.controller;

import io.restaurant.platform.modules.menu.dto.request.CreateRecurringMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.RecurringMenuResponse;
import io.restaurant.platform.modules.menu.service.RecurringMenuService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recurring-menus")
@RequiredArgsConstructor
public class RecurringMenuController {

    private final RecurringMenuService recurringMenuService;

    /**
     * Create or update recurring menu configuration for a day of week.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param request the recurring menu configuration data
     * @return a response containing the created/updated configuration with HTTP 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringMenuResponse>> createOrUpdateRecurringMenu(
            @Valid @RequestBody CreateRecurringMenuRequest request) {
        RecurringMenuResponse created = recurringMenuService.createOrUpdate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring menu configuration saved successfully.", created));
    }

    /**
     * Get all recurring menu configurations for current restaurant.
     * Only users with the ADMIN role can perform this operation.
     *
     * @return a response containing the list of configurations
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RecurringMenuResponse>>> getAllRecurringMenus() {
        List<RecurringMenuResponse> configs = recurringMenuService.findAllByRestaurant();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * Get recurring menu configuration for specific day of week.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param dayOfWeek the day of week (1=Monday, 7=Sunday)
     * @return a response containing the configuration
     */
    @GetMapping("/{dayOfWeek}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RecurringMenuResponse>> getRecurringMenuByDay(
            @PathVariable("dayOfWeek") Integer dayOfWeek) {
        RecurringMenuResponse config = recurringMenuService.findByDayOfWeek(dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * Delete recurring menu configuration for a day of week.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param dayOfWeek the day of week (1=Monday, 7=Sunday)
     * @return a response confirming deletion
     */
    @DeleteMapping("/{dayOfWeek}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringMenu(@PathVariable("dayOfWeek") Integer dayOfWeek) {
        recurringMenuService.deleteByDayOfWeek(dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success("Recurring menu configuration deleted successfully.", null));
    }
}
