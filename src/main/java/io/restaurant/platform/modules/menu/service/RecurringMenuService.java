package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.CreateRecurringMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.RecurringMenuResponse;

import java.util.List;

public interface RecurringMenuService {

    /**
     * Create or update recurring menu configuration for a day of week
     */
    RecurringMenuResponse createOrUpdate(CreateRecurringMenuRequest request);

    /**
     * Get all recurring menu configurations for current restaurant
     */
    List<RecurringMenuResponse> findAllByRestaurant();

    /**
     * Get recurring menu configuration for specific day of week
     */
    RecurringMenuResponse findByDayOfWeek(Integer dayOfWeek);

    /**
     * Delete recurring menu configuration for a day of week
     */
    void deleteByDayOfWeek(Integer dayOfWeek);
}
