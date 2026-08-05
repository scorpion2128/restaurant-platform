package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.menu.dto.request.CreateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.request.MenuItemRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateDailyMenuRequest;
import io.restaurant.platform.modules.menu.dto.response.DailyMenuResponse;
import io.restaurant.platform.modules.menu.dto.response.MenuItemResponse;
import io.restaurant.platform.modules.menu.entity.DailyMenu;
import io.restaurant.platform.modules.menu.entity.DailyMenuItem;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.mapper.DailyMenuMapper;
import io.restaurant.platform.modules.menu.repository.DailyMenuItemRepository;
import io.restaurant.platform.modules.menu.repository.DailyMenuRepository;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateRepository;
import io.restaurant.platform.modules.product.entity.Product;
import io.restaurant.platform.modules.product.repository.ProductRepository;
import io.restaurant.platform.modules.restaurant.entity.Restaurant;
import io.restaurant.platform.modules.restaurant.repository.RestaurantRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Daily Menu Service Implementation
 * Uses master catalog (master_menu_template) for templates
 * Products link to master_product for name/description
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DailyMenuServiceImpl implements DailyMenuService {

    private final SecurityContextHelper securityContextHelper;

    private static final String DAILY_MENU_NOT_FOUND = "Daily menu with id %d not found.";
    private static final String DAILY_MENU_DATE_NOT_FOUND = "Daily menu for date %s not found.";
    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String PRODUCT_NOT_FOUND = "Product with id %d not found.";
    private static final String TEMPLATE_NOT_FOUND = "Menu template with id %d not found.";
    private static final String MENU_DATE_EXISTS = "Daily menu for date %s already exists.";
    private static final String NO_ACTIVE_MENU = "No active menu found.";
    private static final String PRODUCTS_NOT_FOUND = "One or more products not found. Missing product IDs: %s.";
    private static final String UNKNOWN_PRODUCT = "Unknown Product";

    private final DailyMenuRepository dailyMenuRepository;
    private final DailyMenuItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;
    private final MasterMenuTemplateRepository masterTemplateRepository;
    private final DailyMenuMapper dailyMenuMapper;

    @Override
    public DailyMenuResponse create(CreateDailyMenuRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        Restaurant restaurant = getRestaurant(restaurantId);

        // Check if daily menu for this date already exists
        if (dailyMenuRepository.existsByRestaurantIdAndMenuDate(restaurantId, request.menuDate())) {
            throw new BusinessException(MENU_DATE_EXISTS.formatted(request.menuDate()));
        }

        // Validate template if provided
        MasterMenuTemplate template = null;
        if (request.templateId() != null) {
            template = getMasterTemplate(request.templateId());
        }

        // Validate all products exist
        List<Long> productIds = request.items().stream()
                .map(MenuItemRequest::productId)
                .toList();
        List<Product> products = productRepository.findByIdIn(productIds);
        if (products.size() != productIds.size()) {
            List<Long> foundIds = products.stream().map(Product::getId).toList();
            List<Long> missingIds = productIds.stream()
                    .filter(productId -> !foundIds.contains(productId))
                    .toList();
            throw new BusinessException(PRODUCTS_NOT_FOUND.formatted(missingIds));
        }

        // Create daily menu
        DailyMenu dailyMenu = dailyMenuMapper.toEntity(request);
        dailyMenu.setRestaurant(restaurant);
        dailyMenu.setMasterTemplate(template);
        if (request.active() == null) {
            dailyMenu.setActive(false);
        }
        
        // If activating this menu, deactivate any other active menu
        if (dailyMenu.getActive()) {
            deactivateAllMenus(restaurantId);
        }
        
        dailyMenu = dailyMenuRepository.save(dailyMenu);

        // Create items
        DailyMenu finalDailyMenu = dailyMenu;
        List<DailyMenuItem> items = request.items().stream()
                .map(itemReq -> {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(itemReq.productId()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(itemReq.productId())));
                    
                    DailyMenuItem item = new DailyMenuItem();
                    item.setDailyMenu(finalDailyMenu);
                    item.setProduct(product);
                    item.setPriceOverride(itemReq.priceOverride());
                    
                    return item;
                })
                .toList();

        itemRepository.saveAll(items);

        return buildDailyMenuResponse(dailyMenu, items);
    }

    @Override
    public DailyMenuResponse update(Long id, UpdateDailyMenuRequest request) {
        DailyMenu dailyMenu = getDailyMenu(id);
        Long restaurantId = getCurrentRestaurantId();

        // Validate template if provided
        MasterMenuTemplate template = null;
        if (request.templateId() != null) {
            template = getMasterTemplate(request.templateId());
        }

        // Validate all products exist
        List<Long> productIds = request.items().stream()
                .map(MenuItemRequest::productId)
                .toList();
        List<Product> products = productRepository.findByIdIn(productIds);
        if (products.size() != productIds.size()) {
            List<Long> foundIds = products.stream().map(Product::getId).toList();
            List<Long> missingIds = productIds.stream()
                    .filter(productId -> !foundIds.contains(productId))
                    .toList();
            throw new BusinessException(PRODUCTS_NOT_FOUND.formatted(missingIds));
        }

        // Update daily menu
        dailyMenu.setMasterTemplate(template);
        if (request.active() != null) {
            // If activating this menu, deactivate any other active menu
            if (request.active() && !dailyMenu.getActive()) {
                deactivateAllMenus(restaurantId);
            }
            dailyMenu.setActive(request.active());
        }

        // Replace items
        itemRepository.deleteByDailyMenuId(id);
        List<DailyMenuItem> newItems = request.items().stream()
                .map(itemReq -> {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(itemReq.productId()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(itemReq.productId())));
                    
                    DailyMenuItem item = new DailyMenuItem();
                    item.setDailyMenu(dailyMenu);
                    item.setProduct(product);
                    item.setPriceOverride(itemReq.priceOverride());
                    
                    return item;
                })
                .toList();

        itemRepository.saveAll(newItems);

        return buildDailyMenuResponse(dailyMenu, newItems);
    }

    @Override
    public void delete(Long id) {
        DailyMenu dailyMenu = getDailyMenu(id);
        itemRepository.deleteByDailyMenuId(id);
        dailyMenuRepository.delete(dailyMenu);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMenuResponse findById(Long id) {
        DailyMenu dailyMenu = getDailyMenu(id);
        List<DailyMenuItem> items = itemRepository.findByDailyMenuId(id);
        return buildDailyMenuResponse(dailyMenu, items);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMenuResponse findByDate(LocalDate date) {
        Long restaurantId = getCurrentRestaurantId();
        DailyMenu dailyMenu = dailyMenuRepository.findByRestaurantIdAndMenuDate(restaurantId, date)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_MENU_DATE_NOT_FOUND.formatted(date)));
        
        List<DailyMenuItem> items = itemRepository.findByDailyMenuId(dailyMenu.getId());
        return buildDailyMenuResponse(dailyMenu, items);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DailyMenuResponse> findAllByRestaurant(Pageable pageable) {
        Long restaurantId = getCurrentRestaurantId();
        Page<DailyMenu> dailyMenus = dailyMenuRepository.findByRestaurantId(restaurantId, pageable);
        
        return dailyMenus.map(dailyMenu -> {
            List<DailyMenuItem> items = itemRepository.findByDailyMenuId(dailyMenu.getId());
            return buildDailyMenuResponse(dailyMenu, items);
        });
    }

    @Override
    public DailyMenuResponse toggleActive(Long id) {
        DailyMenu dailyMenu = getDailyMenu(id);
        Long restaurantId = getCurrentRestaurantId();

        // If activating, deactivate all other menus
        if (!dailyMenu.getActive()) {
            deactivateAllMenus(restaurantId);
        }

        dailyMenu.setActive(!dailyMenu.getActive());

        List<DailyMenuItem> items = itemRepository.findByDailyMenuId(id);
        return buildDailyMenuResponse(dailyMenu, items);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMenuResponse getActiveMenu() {
        Long restaurantId = getCurrentRestaurantId();
        DailyMenu dailyMenu = dailyMenuRepository.findByRestaurantIdAndActiveTrue(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(NO_ACTIVE_MENU));

        List<DailyMenuItem> items = itemRepository.findByDailyMenuId(dailyMenu.getId());
        return buildDailyMenuResponse(dailyMenu, items);
    }

    private void deactivateAllMenus(Long restaurantId) {
        dailyMenuRepository.findByRestaurantIdAndActiveTrue(restaurantId)
                .ifPresent(activeMenu -> activeMenu.setActive(false));
    }

    private DailyMenuResponse buildDailyMenuResponse(DailyMenu dailyMenu, List<DailyMenuItem> items) {
        List<MenuItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal effectivePrice = item.getPriceOverride() != null ? 
                            item.getPriceOverride() : product.getPrice();
                    
                    // Get product name from master_product
                    String productName = product.getMasterProduct() != null ? 
                            product.getMasterProduct().getName() : UNKNOWN_PRODUCT;
                    
                    return new MenuItemResponse(
                            item.getId(),
                            product.getId(),
                            productName,
                            product.getPrice(),
                            null, // No section ID in simplified schema
                            null, // No section name in simplified schema
                            item.getPriceOverride(),
                            effectivePrice
                    );
                })
                .collect(Collectors.toList());

        DailyMenuResponse response = dailyMenuMapper.toResponse(dailyMenu);
        return new DailyMenuResponse(
                response.id(),
                response.restaurantId(),
                response.menuDate(),
                response.templateId(),
                response.templateName(),
                response.active(),
                itemResponses
        );
    }

    private DailyMenu getDailyMenu(Long id) {
        return dailyMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DAILY_MENU_NOT_FOUND.formatted(id)));
    }

    private MasterMenuTemplate getMasterTemplate(Long id) {
        return masterTemplateRepository.findById(id)
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
