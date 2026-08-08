package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuOverrideRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import io.restaurant.platform.modules.menu.dto.response.MenuItemResponse;
import io.restaurant.platform.modules.menu.entity.DailyMenu;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplateItem;
import io.restaurant.platform.modules.menu.entity.RecurringMenuConfig;
import io.restaurant.platform.modules.menu.repository.DailyMenuRepository;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateRepository;
import io.restaurant.platform.modules.menu.repository.RecurringMenuConfigRepository;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import io.restaurant.platform.modules.product.entity.Product;
import io.restaurant.platform.modules.product.repository.ProductRepository;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Daily Menu Service Implementation - New approach with recurring menus and overrides
 * - Recurring menus: Automatic configuration for each day of week
 * - Overrides: Specific date configurations that take priority
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DailyMenuServiceImpl implements DailyMenuService {

    private final SecurityContextHelper securityContextHelper;
    private final DailyMenuRepository dailyMenuRepository;
    private final RecurringMenuConfigRepository recurringMenuConfigRepository;
    private final MasterMenuTemplateRepository masterTemplateRepository;
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;

    private static final String DAILY_MENU_NOT_FOUND = "Daily menu with id %d not found.";
    private static final String DAILY_MENU_DATE_NOT_FOUND = "No menu configured for date %s.";
    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String TEMPLATE_NOT_FOUND = "Menu template with id %d not found.";
    private static final String MENU_DATE_EXISTS = "Daily menu override for date %s already exists.";
    @Override
    public DailyMenuResponse createOverride(CreateDailyMenuOverrideRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        Restaurant restaurant = getRestaurant(restaurantId);

        // Check if override already exists for this date
        if (dailyMenuRepository.existsByRestaurantIdAndMenuDate(restaurantId, request.menuDate())) {
            throw new BusinessException(MENU_DATE_EXISTS.formatted(request.menuDate()));
        }

        MasterMenuTemplate template = getMasterTemplate(request.templateId());

        // Create daily menu as override
        DailyMenu dailyMenu = new DailyMenu();
        dailyMenu.setRestaurant(restaurant);
        dailyMenu.setMenuDate(request.menuDate());
        dailyMenu.setMasterTemplate(template);
        dailyMenu.setIsOverride(true);
        dailyMenu = dailyMenuRepository.save(dailyMenu);

        log.info("Created override menu for date {} with template {}", request.menuDate(), template.getId());
        return buildDailyMenuResponse(dailyMenu);
    }

    @Override
    public DailyMenuResponse updateOverride(Long id, CreateDailyMenuOverrideRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        DailyMenu dailyMenu = getDailyMenu(id, restaurantId);

        if (!dailyMenu.getIsOverride()) {
            throw new BusinessException("Cannot update non-override menu. ID: " + id);
        }

        dailyMenuRepository.findByRestaurantIdAndMenuDate(restaurantId, request.menuDate())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException(MENU_DATE_EXISTS.formatted(request.menuDate()));
                });

        dailyMenu.setMenuDate(request.menuDate());
        dailyMenu.setMasterTemplate(getMasterTemplate(request.templateId()));

        log.info("Updated override menu {} for date {} with template {}",
                id, request.menuDate(), request.templateId());
        return buildDailyMenuResponse(dailyMenu);
    }

    @Override
    public void deleteOverride(Long id) {
        DailyMenu dailyMenu = getDailyMenu(id, getCurrentRestaurantId());
        
        if (!dailyMenu.getIsOverride()) {
            throw new BusinessException("Cannot delete non-override menu. ID: " + id);
        }

        dailyMenuRepository.delete(dailyMenu);
        log.info("Deleted override menu for date {}", dailyMenu.getMenuDate());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMenuResponse findByDate(LocalDate date) {
        Long restaurantId = getCurrentRestaurantId();

        // 1. Check for specific override first
        Optional<DailyMenu> override = dailyMenuRepository.findByRestaurantIdAndMenuDate(restaurantId, date);
        if (override.isPresent() && override.get().getIsOverride()) {
            DailyMenu dailyMenu = override.get();
            log.debug("Found override menu for date {}", date);
            return buildDailyMenuResponse(dailyMenu);
        }

        // 2. Check recurring configuration for day of week
        int dayOfWeekValue = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        Optional<RecurringMenuConfig> recurringConfig = recurringMenuConfigRepository
                .findByRestaurantIdAndDayOfWeekWithTemplate(restaurantId, dayOfWeekValue);

        if (recurringConfig.isEmpty()) {
            throw new ResourceNotFoundException(DAILY_MENU_DATE_NOT_FOUND.formatted(date));
        }

        // Build response from recurring configuration (virtual menu)
        log.debug("Using recurring configuration for date {}", date);
        return buildVirtualMenuFromRecurring(date, recurringConfig.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyMenuResponse> findAllOverrides(Pageable pageable) {
        Long restaurantId = getCurrentRestaurantId();
        Page<DailyMenu> overrides = dailyMenuRepository.findOverridesOrderedByNearestDate(restaurantId, pageable);

        return overrides.map(this::buildDailyMenuResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMenuResponse> getMonthlyView(Integer year, Integer month) {
        Long restaurantId = getCurrentRestaurantId();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<DailyMenuResponse> monthlyMenus = new ArrayList<>();

        // Get all overrides for this month
        List<DailyMenu> overrides = dailyMenuRepository
                .findByRestaurantIdAndMenuDateBetween(restaurantId, startDate, endDate);

        // Get all recurring configurations
        List<RecurringMenuConfig> recurringConfigs = recurringMenuConfigRepository
                .findAllByRestaurantIdWithTemplate(restaurantId);

        // Process each day of the month
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date; // For lambda

            // Check if there's an override
            Optional<DailyMenu> override = overrides.stream()
                    .filter(m -> m.getMenuDate().equals(currentDate))
                    .findFirst();

            if (override.isPresent()) {
                monthlyMenus.add(buildDailyMenuResponse(override.get()));
            } else {
                // Check recurring configuration
                int dayOfWeekValue = date.getDayOfWeek().getValue();
                Optional<RecurringMenuConfig> recurring = recurringConfigs.stream()
                        .filter(c -> c.getDayOfWeek().equals(dayOfWeekValue))
                        .findFirst();

                recurring.ifPresent(config -> 
                        monthlyMenus.add(buildVirtualMenuFromRecurring(currentDate, config)));
            }
        }

        log.info("Generated monthly view for {}-{}: {} days with menus", year, month, monthlyMenus.size());
        return monthlyMenus;
    }

    private List<MenuItemResponse> resolveMenuItems(Long restaurantId, MasterMenuTemplate template) {
        List<MasterMenuTemplateItem> templateItems = template.getItems();
        if (templateItems == null || templateItems.isEmpty()) {
            return List.of();
        }

        List<MenuItemResponse> items = new ArrayList<>();
        for (MasterMenuTemplateItem templateItem : templateItems) {
            MasterProduct masterProduct = templateItem.getMasterProduct();
            Optional<Product> product = productRepository.findByRestaurantIdAndMasterProductId(
                    restaurantId, masterProduct.getId());

            if (product.isPresent()) {
                Product restaurantProduct = product.get();
                items.add(new MenuItemResponse(
                        templateItem.getId(),
                        restaurantProduct.getId(),
                        masterProduct.getName(),
                        restaurantProduct.getPrice(),
                        templateItem.getSection() != null ? templateItem.getSection().getId() : null,
                        templateItem.getSection() != null ? templateItem.getSection().getName() : null
                ));
            } else {
                log.warn("Product not found for master product {} in restaurant {}",
                        masterProduct.getId(), restaurantId);
            }
        }

        return items;
    }

    private DailyMenuResponse buildVirtualMenuFromRecurring(LocalDate date, RecurringMenuConfig config) {
        MasterMenuTemplate template = config.getMasterTemplate();
        Long restaurantId = config.getRestaurant().getId();

        return new DailyMenuResponse(
                null, // No ID (virtual)
                restaurantId,
                date,
                template.getId(),
                template.getName(),
                false, // isOverride
                resolveMenuItems(restaurantId, template)
        );
    }

    private DailyMenuResponse buildDailyMenuResponse(DailyMenu dailyMenu) {
        MasterMenuTemplate template = dailyMenu.getMasterTemplate();
        Long restaurantId = dailyMenu.getRestaurant().getId();

        return new DailyMenuResponse(
                dailyMenu.getId(),
                restaurantId,
                dailyMenu.getMenuDate(),
                template != null ? template.getId() : null,
                template != null ? template.getName() : null,
                dailyMenu.getIsOverride(),
                template != null ? resolveMenuItems(restaurantId, template) : List.of()
        );
    }

    private DailyMenu getDailyMenu(Long id, Long restaurantId) {
        return dailyMenuRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_MENU_NOT_FOUND.formatted(id)));
    }

    private MasterMenuTemplate getMasterTemplate(Long id) {
        Long organizationId = securityContextHelper.getOrganizationId();
        return masterTemplateRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(id)));
    }

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(id)));
    }

    private Long getCurrentRestaurantId() {
        return securityContextHelper.getActiveRestaurantId();
    }
}
