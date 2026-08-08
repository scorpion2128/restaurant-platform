package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.menu.dto.request.CreateRecurringMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.RecurringMenuResponse;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.entity.RecurringMenuConfig;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateRepository;
import io.restaurant.platform.modules.menu.repository.RecurringMenuConfigRepository;
import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.organization.repository.OrganizationRepository;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecurringMenuServiceImpl implements RecurringMenuService {

    private final SecurityContextHelper securityContextHelper;
    private final RecurringMenuConfigRepository recurringMenuConfigRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrganizationRepository organizationRepository;
    private final MasterMenuTemplateRepository masterMenuTemplateRepository;

    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String TEMPLATE_NOT_FOUND = "Menu template with id %d not found.";
    private static final String CONFIG_NOT_FOUND = "Recurring menu configuration for day %d not found.";

    private static final Map<Integer, String> DAY_NAMES = Map.of(
            1, "Lunes",
            2, "Martes",
            3, "Miércoles",
            4, "Jueves",
            5, "Viernes",
            6, "Sábado",
            7, "Domingo"
    );

    @Override
    @Transactional
    public RecurringMenuResponse createOrUpdate(CreateRecurringMenuRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        Long organizationId = getCurrentOrganizationId();

        MasterMenuTemplate template = masterMenuTemplateRepository
                .findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(request.templateId())));

        RecurringMenuConfig config = recurringMenuConfigRepository
                .findByRestaurantIdAndDayOfWeek(restaurantId, request.dayOfWeek())
                .orElseGet(() -> {
                    RecurringMenuConfig newConfig = new RecurringMenuConfig();
                    newConfig.setOrganization(organizationRepository.getReferenceById(organizationId));
                    newConfig.setRestaurant(restaurantRepository.getReferenceById(restaurantId));
                    newConfig.setDayOfWeek(request.dayOfWeek());
                    return newConfig;
                });

        boolean isNew = config.getId() == null;

        config.setMasterTemplate(template);
        config = recurringMenuConfigRepository.save(config);

        log.info("{} recurring menu config {} for organization {}, restaurant {}, day {}",
                isNew ? "Created" : "Updated",
                config.getId(),
                organizationId,
                restaurantId,
                request.dayOfWeek());

        return toResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringMenuResponse> findAllByRestaurant() {
        Long restaurantId = getCurrentRestaurantId();
        List<RecurringMenuConfig> configs = recurringMenuConfigRepository
                .findAllByRestaurantIdWithTemplate(restaurantId);
        
        log.info("Found {} recurring menu configurations for restaurant {}", configs.size(), restaurantId);
        return configs.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecurringMenuResponse findByDayOfWeek(Integer dayOfWeek) {
        Long restaurantId = getCurrentRestaurantId();
        RecurringMenuConfig config = recurringMenuConfigRepository
                .findByRestaurantIdAndDayOfWeekWithTemplate(restaurantId, dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException(CONFIG_NOT_FOUND.formatted(dayOfWeek)));
        
        return toResponse(config);
    }

    @Override
    public void deleteByDayOfWeek(Integer dayOfWeek) {
        Long restaurantId = getCurrentRestaurantId();
        
        if (!recurringMenuConfigRepository.existsByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek)) {
            throw new ResourceNotFoundException(CONFIG_NOT_FOUND.formatted(dayOfWeek));
        }

        recurringMenuConfigRepository.deleteByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek);
        log.info("Deleted recurring menu config for restaurant {} day {}", restaurantId, dayOfWeek);
    }

    private Long getCurrentRestaurantId() {
        return securityContextHelper.getActiveRestaurantId();
    }

    private Long getCurrentOrganizationId() {
        return securityContextHelper.getOrganizationId();
    }

    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(restaurantId)));
    }

    private Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization with id %d not found.".formatted(organizationId)));
    }

    private MasterMenuTemplate getMasterTemplate(Long templateId) {
        Long organizationId = securityContextHelper.getOrganizationId();
        return masterMenuTemplateRepository.findByIdAndOrganizationId(templateId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(templateId)));
    }

    private RecurringMenuResponse toResponse(RecurringMenuConfig config) {
        return new RecurringMenuResponse(
                config.getId(),
                config.getDayOfWeek(),
                DAY_NAMES.get(config.getDayOfWeek()),
                config.getMasterTemplate().getId(),
                config.getMasterTemplate().getName()
        );
    }
}
