package io.restaurant.platform.modules.product.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import io.restaurant.platform.modules.product.entity.MasterProductCategory;
import io.restaurant.platform.modules.product.repository.MasterProductCategoryRepository;
import io.restaurant.platform.modules.product.repository.MasterProductRepository;
import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.organization.repository.OrganizationRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service Implementation - Working directly with master_product
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String PRODUCT_NOT_FOUND = "Product with id %d not found.";
    private static final String ORGANIZATION_NOT_FOUND = "Organization with id %d not found.";
    private static final String CATEGORY_NOT_FOUND = "Category with id %d not found.";
    private static final String PRODUCT_NAME_EXISTS = "A product with this name already exists.";

    private final SecurityContextHelper securityContextHelper;
    private final MasterProductRepository masterProductRepository;
    private final MasterProductCategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public ProductResponse create(CreateProductRequest request) {
        Long organizationId = securityContextHelper.getOrganizationId();
        Organization organization = getOrganization(organizationId);

        // Validate category exists
        MasterProductCategory category = getCategory(request.categoryId());

        // Check if product name already exists
        if (masterProductRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
            throw new BusinessException(PRODUCT_NAME_EXISTS);
        }

        // Create product
        MasterProduct product = new MasterProduct();
        product.setOrganization(organization);
        product.setMasterCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setBasePrice(request.price());
        product.setCode(request.code());
        product.setActive(request.active() != null ? request.active() : true);

        product = masterProductRepository.save(product);

        return toResponse(product);
    }

    @Override
    public ProductResponse update(Long id, UpdateProductRequest request) {
        MasterProduct product = getProduct(id);

        // Validate category exists
        MasterProductCategory category = getCategory(request.categoryId());

        // Check if name is changing and if new name already exists
        if (!product.getName().equals(request.name())) {
            if (masterProductRepository.existsByOrganizationIdAndName(
                    product.getOrganization().getId(), request.name())) {
                throw new BusinessException(PRODUCT_NAME_EXISTS);
            }
        }

        // Update product
        product.setName(request.name());
        product.setDescription(request.description());
        product.setMasterCategory(category);
        product.setBasePrice(request.price());
        product.setCode(request.code());
        if (request.active() != null) {
            product.setActive(request.active());
        }

        return toResponse(product);
    }

    @Override
    public void delete(Long id) {
        MasterProduct product = getProduct(id);
        masterProductRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        MasterProduct product = getProduct(id);
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllByRestaurant(Pageable pageable, Long categoryId, Boolean available) {
        Long organizationId = securityContextHelper.getOrganizationId();
        
        log.info("Filtering products - organizationId: {}, categoryId: {}, available: {}", 
                organizationId, categoryId, available);
        
        List<MasterProduct> products;
        
        if (categoryId != null && available != null) {
            log.info("Applying BOTH filters: categoryId={} and active={}", categoryId, available);
            products = masterProductRepository.findByOrganizationIdAndMasterCategoryIdAndActive(
                    organizationId, categoryId, available);
        } else if (categoryId != null) {
            log.info("Applying CATEGORY filter: categoryId={}", categoryId);
            products = masterProductRepository.findByOrganizationIdAndMasterCategoryId(
                    organizationId, categoryId);
        } else if (available != null) {
            log.info("Applying ACTIVE filter: active={}", available);
            products = masterProductRepository.findByOrganizationIdAndActive(organizationId, available);
        } else {
            log.info("NO filters applied, returning ALL products");
            products = masterProductRepository.findByOrganizationId(organizationId);
        }

        log.info("Found {} products after filtering", products.size());

        // Convert to responses
        List<ProductResponse> responses = products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<ProductResponse> pageContent = start >= responses.size() ? 
                List.of() : responses.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    public ProductResponse toggleAvailability(Long id) {
        MasterProduct product = getProduct(id);
        product.setActive(!product.getActive());
        return toResponse(product);
    }

    private MasterProduct getProduct(Long id) {
        return masterProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(id)));
    }

    private MasterProductCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND.formatted(id)));
    }

    private Organization getOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZATION_NOT_FOUND.formatted(id)));
    }

    private ProductResponse toResponse(MasterProduct product) {
        return new ProductResponse(
                product.getId(),
                product.getOrganization().getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                product.getActive(),
                product.getMasterCategory() != null ? product.getMasterCategory().getId() : null,
                product.getMasterCategory() != null ? product.getMasterCategory().getName() : null
        );
    }
}
