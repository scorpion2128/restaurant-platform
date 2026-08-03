package io.restaurant.platform.modules.product.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.organization.repository.OrganizationRepository;
import io.restaurant.platform.modules.product.dto.request.CreateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.response.MasterCategoryResponse;
import io.restaurant.platform.modules.product.entity.MasterProductCategory;
import io.restaurant.platform.modules.product.mapper.MasterProductCategoryMapper;
import io.restaurant.platform.modules.product.repository.MasterProductCategoryRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for master product categories
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MasterProductCategoryServiceImpl implements MasterProductCategoryService {

    private static final String CATEGORY_NOT_FOUND = "Master category with id %d not found.";
    private static final String ORGANIZATION_NOT_FOUND = "Organization with id %d not found.";
    private static final String CATEGORY_NAME_EXISTS = "Master category with name '%s' already exists.";

    private final SecurityContextHelper securityContextHelper;
    private final MasterProductCategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final MasterProductCategoryMapper categoryMapper;

    @Override
    public MasterCategoryResponse create(CreateMasterCategoryRequest request) {
        Long organizationId = getCurrentOrganizationId();
        Organization organization = getOrganization(organizationId);

        // Check if category with same name already exists
        if (categoryRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
            throw new BusinessException(CATEGORY_NAME_EXISTS.formatted(request.name()));
        }

        // Create category
        MasterProductCategory category = categoryMapper.toEntity(request);
        category.setOrganization(organization);
        if (request.active() == null) {
            category.setActive(true);
        }
        if (request.displayOrder() == null) {
            category.setDisplayOrder(0);
        }
        category = categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Override
    public MasterCategoryResponse update(Long id, UpdateMasterCategoryRequest request) {
        MasterProductCategory category = getCategory(id);
        Long organizationId = getCurrentOrganizationId();

        // Check if category with same name already exists (excluding current)
        categoryRepository.findByOrganizationIdAndName(organizationId, request.name())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(CATEGORY_NAME_EXISTS.formatted(request.name()));
                    }
                });

        // Update category
        categoryMapper.updateEntity(request, category);
        if (request.displayOrder() != null) {
            category.setDisplayOrder(request.displayOrder());
        }
        if (request.active() != null) {
            category.setActive(request.active());
        }

        return categoryMapper.toResponse(category);
    }

    @Override
    public void delete(Long id) {
        MasterProductCategory category = getCategory(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterCategoryResponse findById(Long id) {
        MasterProductCategory category = getCategory(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MasterCategoryResponse> findAllByOrganization(Pageable pageable) {
        Long organizationId = getCurrentOrganizationId();
        Page<MasterProductCategory> categories = categoryRepository
                .findAll(pageable);
        
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MasterCategoryResponse> findAllActive() {
        Long organizationId = getCurrentOrganizationId();
        List<MasterProductCategory> categories = categoryRepository
                .findByOrganizationIdAndActiveTrue(organizationId);
        
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    private MasterProductCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND.formatted(id)));
    }

    private Organization getOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZATION_NOT_FOUND.formatted(id)));
    }

    private Long getCurrentOrganizationId() {
        return securityContextHelper.getOrganizationId();
    }
}
