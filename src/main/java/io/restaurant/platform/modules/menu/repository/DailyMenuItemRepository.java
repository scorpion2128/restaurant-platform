package io.restaurant.platform.modules.menu.repository;

import io.restaurant.platform.modules.menu.entity.DailyMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DailyMenuItemRepository extends JpaRepository<DailyMenuItem, Long> {

    List<DailyMenuItem> findByDailyMenuId(Long dailyMenuId);

    @Modifying
    @Query("DELETE FROM DailyMenuItem dmi WHERE dmi.dailyMenu.id = :dailyMenuId")
    void deleteByDailyMenuId(@Param("dailyMenuId") Long dailyMenuId);
}
