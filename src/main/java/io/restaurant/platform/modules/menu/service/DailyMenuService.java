package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface DailyMenuService {

    DailyMenuResponse create(CreateDailyMenuRequest request);

    DailyMenuResponse update(Long id, UpdateDailyMenuRequest request);

    void delete(Long id);

    DailyMenuResponse findById(Long id);

    DailyMenuResponse findByDate(LocalDate date);

    Page<DailyMenuResponse> findAllByRestaurant(Pageable pageable);

    DailyMenuResponse toggleActive(Long id);

    DailyMenuResponse getActiveMenu();
}
