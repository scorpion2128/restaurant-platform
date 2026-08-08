package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuOverrideRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DailyMenuService {

    /**
     * Create a daily menu override for specific date
     * This has priority over recurring configuration
     */
    DailyMenuResponse createOverride(CreateDailyMenuOverrideRequest request);

    /**
     * Update the date or template of a daily menu override
     */
    DailyMenuResponse updateOverride(Long id, CreateDailyMenuOverrideRequest request);

    /**
     * Delete a daily menu override
     * Returns to using recurring configuration if available
     */
    void deleteOverride(Long id);

    /**
     * Get menu for specific date
     * Resolves override first, then recurring configuration
     */
    DailyMenuResponse findByDate(LocalDate date);

    /**
     * Get all overrides for current restaurant
     */
    Page<DailyMenuResponse> findAllOverrides(Pageable pageable);

    /**
     * Get monthly view - all dates with their configured menus
     * Includes both overrides and recurring configurations
     */
    List<DailyMenuResponse> getMonthlyView(Integer year, Integer month);
}
