package io.restaurant.platform.modules.menu.controller;

import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuOverrideRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import io.restaurant.platform.modules.menu.service.DailyMenuService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/daily-menus")
@RequiredArgsConstructor
public class DailyMenuController {

    private final DailyMenuService dailyMenuService;

    /**
     * Creates a daily menu override for a specific date.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param request the daily menu override data
     * @return a response containing the created override details with HTTP 201 (Created)
     */
    @PostMapping("/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> createOverride(
            @Valid @RequestBody CreateDailyMenuOverrideRequest request) {
        DailyMenuResponse created = dailyMenuService.createOverride(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Daily menu override created successfully.", created));
    }

    @PutMapping("/{id}/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> updateOverride(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateDailyMenuOverrideRequest request) {
        DailyMenuResponse updated = dailyMenuService.updateOverride(id, request);
        return ResponseEntity.ok(ApiResponse.success("Daily menu override updated successfully.", updated));
    }

    /**
     * Deletes a daily menu override.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id the ID of the override to delete
     * @return a response confirming deletion
     */
    @DeleteMapping("/{id}/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOverride(@PathVariable("id") Long id) {
        dailyMenuService.deleteOverride(id);
        return ResponseEntity.ok(ApiResponse.success("Daily menu override deleted successfully.", null));
    }

    /**
     * Retrieves the menu for a specific date.
     * Resolves override first, then recurring configuration.
     *
     * @param date the date to query
     * @return a response containing the menu details
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<DailyMenuResponse>> getMenuByDate(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyMenuResponse menu = dailyMenuService.findByDate(date);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * Retrieves all overrides for current restaurant.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param pageable pagination parameters
     * @return a response containing paginated overrides
     */
    @GetMapping("/overrides")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<DailyMenuResponse>>> getAllOverrides(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DailyMenuResponse> overrides = dailyMenuService.findAllOverrides(pageable);
        return ResponseEntity.ok(ApiResponse.success(overrides));
    }

    /**
     * Retrieves monthly view with all configured menus.
     * Includes both overrides and recurring configurations.
     *
     * @param year  the year
     * @param month the month (1-12)
     * @return a response containing all menus for the month
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<ApiResponse<List<DailyMenuResponse>>> getMonthlyView(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month) {
        List<DailyMenuResponse> monthlyMenus = dailyMenuService.getMonthlyView(year, month);
        return ResponseEntity.ok(ApiResponse.success(monthlyMenus));
    }
}
