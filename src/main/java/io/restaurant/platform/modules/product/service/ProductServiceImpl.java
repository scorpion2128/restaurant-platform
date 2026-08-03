package io.restaurant.platform.modules.product.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import io.restaurant.platform.modules.product.entity.Product;
import io.restaurant.platform.modules.product.mapper.ProductMapper;
import io.restaurant.platform.modules.product.repository.MasterProductRepository;
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

/**
 * Product Service Implementation - Simplified for master catalog
 * Products only store price and availability locally
 * All other data comes from master_product
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT_NOT_FOUND = "Product with id %d not found.";
    private static final String RESTAURANT_NOT_FOUND = "Restaurant with id %d not found.";
    private static final String MASTER_PRODUCT_NOT_FOUND = "Master product with id %d not found.";
    private static final String PRODUCT_ALREADY_EXISTS = "This master product is already added to your restaurant.";

    private final SecurityContextHelper securityContextHelper;
    private final ProductRepository productRepository;
    private final MasterProductRepository masterProductRepository;
    private final RestaurantRepository restaurantRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(CreateProductRequest request) {
        Long restaurantId = getCurrentRestaurantId();
        Restaurant restaurant = getRestaurant(restaurantId);

        // Validate master product exists
        MasterProduct masterProduct = getMasterProduct(request.masterProductId());

        // Check if product already exists for this restaurant
        if (productRepository.existsByRestaurantIdAndMasterProductId(restaurantId, request.masterProductId())) {
            throw new BusinessException(PRODUCT_ALREADY_EXISTS);
        }

        // Create product
        Product product = productMapper.toEntity(request);
        product.setRestaurant(restaurant);
        product.setMasterProduct(masterProduct);
        if (request.available() == null) {
            product.setAvailable(true);
        }
        product = productRepository.save(product);

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = getProduct(id);

        // Update product (only price and availability)
        productMapper.updateEntity(request, product);

        return productMapper.toResponse(product);
    }

    @Override
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = getProduct(id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllByRestaurant(Pageable pageable, Long categoryId, Boolean available) {
        Long restaurantId = getCurrentRestaurantId();
        
        Page<Product> products;
        if (categoryId != null) {
            // Filter by master category
            products = productRepository.findByRestaurantIdAndMasterProductCategoryId(restaurantId, categoryId, pageable);
        } else if (available != null) {
            products = productRepository.findByRestaurantIdAndAvailable(restaurantId, available, pageable);
        } else {
            products = productRepository.findByRestaurantId(restaurantId, pageable);
        }

        return products.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse toggleAvailability(Long id) {
        Product product = getProduct(id);
        product.setAvailable(!product.getAvailable());
        return productMapper.toResponse(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(id)));
    }

    private MasterProduct getMasterProduct(Long id) {
        return masterProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MASTER_PRODUCT_NOT_FOUND.formatted(id)));
    }

    private Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESTAURANT_NOT_FOUND.formatted(id)));
    }

    private Long getCurrentRestaurantId() {
        return securityContextHelper.getActiveRestaurantId();
    }
}
