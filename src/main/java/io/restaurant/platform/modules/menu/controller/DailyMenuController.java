package io.restaurant.platform.modules.menu.controller;

import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import io.restaurant.platform.modules.menu.service.DailyMenuService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for managing daily menus.
 * Only ADMIN users can create, update, or delete daily menus.
 */
@RestController
@RequestMapping("/api/daily-menus")
@RequiredArgsConstructor
public class DailyMenuController {

    private final DailyMenuService dailyMenuService;

    /**
     * Creates a new daily menu.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param request the daily menu creation data
     * @return a response containing the created daily menu details with HTTP 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> createDailyMenu(
            @Valid @RequestBody CreateDailyMenuRequest request) {
        DailyMenuResponse created = dailyMenuService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Daily menu created successfully.", created));
    }

    /**
     * Updates an existing daily menu.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id      the ID of the daily menu to update
     * @param request the updated daily menu data
     * @return a response containing the updated daily menu details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> updateDailyMenu(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDailyMenuRequest request) {
        DailyMenuResponse updated = dailyMenuService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Daily menu updated successfully.", updated));
    }

    /**
     * Deletes a daily menu.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id the ID of the daily menu to delete
     * @return a response confirming deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDailyMenu(@PathVariable("id") Long id) {
        dailyMenuService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Daily menu deleted successfully.", null));
    }

    /**
     * Retrieves a daily menu by ID.
     *
     * @param id the ID of the daily menu
     * @return a response containing the daily menu details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> getDailyMenuById(@PathVariable("id") Long id) {
        DailyMenuResponse dailyMenu = dailyMenuService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(dailyMenu));
    }

    /**
     * Retrieves a daily menu by date.
     *
     * @param date the menu date
     * @return a response containing the daily menu details
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> getDailyMenuByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyMenuResponse dailyMenu = dailyMenuService.findByDate(date);
        return ResponseEntity.ok(ApiResponse.success(dailyMenu));
    }

    /**
     * Lists all daily menus for the current user's restaurant.
     *
     * @param pageable pagination information
     * @return a paginated response containing the list of daily menus
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DailyMenuResponse>>> listDailyMenus(
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                @SortDefault(sort = "menuDate", direction = Sort.Direction.DESC)
            })
            Pageable pageable) {
        Page<DailyMenuResponse> dailyMenus = dailyMenuService.findAllByRestaurant(pageable);
        return ResponseEntity.ok(ApiResponse.success(dailyMenus));
    }

    /**
     * Toggles the active status of a daily menu.
     * Only users with the ADMIN role can perform this operation.
     * Activating a menu will deactivate all other menus.
     *
     * @param id the ID of the daily menu to toggle
     * @return a response containing the updated daily menu details
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> toggleActive(@PathVariable("id") Long id) {
        DailyMenuResponse updated = dailyMenuService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Daily menu status toggled successfully.", updated));
    }

    /**
     * Retrieves the currently active daily menu.
     *
     * @return a response containing the active daily menu details
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> getActiveMenu() {
        DailyMenuResponse activeMenu = dailyMenuService.getActiveMenu();
        return ResponseEntity.ok(ApiResponse.success(activeMenu));
    }
}
