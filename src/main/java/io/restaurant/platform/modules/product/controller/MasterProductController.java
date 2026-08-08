package io.restaurant.platform.modules.product.controller;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.product.dto.response.MasterProductResponse;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import io.restaurant.platform.modules.product.repository.MasterProductRepository;
import io.restaurant.platform.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Master Product Controller
 * Provides read-only access to the master product catalog
 */
@RestController
@RequestMapping("/master-products")
@RequiredArgsConstructor
public class MasterProductController {

    private final MasterProductRepository masterProductRepository;
    private final SecurityContextHelper securityContextHelper;

    /**
     * Lists all master products for the current user's organization.
     * Supports filtering by category and active status.
     *
     * @param pageable   pagination information
     * @param categoryId optional category filter
     * @param active     optional active status filter (defaults to true)
     * @return a paginated response containing the list of master products
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<MasterProductResponse>>> listMasterProducts(
            @PageableDefault(size = 100, sort = "name") Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "active", required = false, defaultValue = "true") Boolean active) {

        Long organizationId = securityContextHelper.getOrganizationId();
        
        List<MasterProduct> masterProducts;
        
        if (categoryId != null) {
            masterProducts = masterProductRepository.findActiveByCategoryAndOrganizationWithCategory(organizationId, categoryId);
        } else if (active) {
            masterProducts = masterProductRepository.findByOrganizationIdAndActiveTrueWithCategory(organizationId);
        } else {
            masterProducts = masterProductRepository.findByOrganizationId(organizationId);
        }

        // Convert to responses
        List<MasterProductResponse> responses = masterProducts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        // Create page (simple pagination for now)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<MasterProductResponse> pageContent = responses.subList(start, end);
        
        Page<MasterProductResponse> page = new PageImpl<>(pageContent, pageable, responses.size());

        return ResponseEntity.ok(ApiResponse.success(page));
    }

    private MasterProductResponse toResponse(MasterProduct masterProduct) {
        return new MasterProductResponse(
                masterProduct.getId(),
                masterProduct.getOrganization().getId(),
                masterProduct.getMasterCategory() != null ? masterProduct.getMasterCategory().getId() : null,
                masterProduct.getMasterCategory() != null ? masterProduct.getMasterCategory().getName() : null,
                masterProduct.getCode(),
                masterProduct.getName(),
                masterProduct.getDescription(),
                masterProduct.getBasePrice(),
                masterProduct.getActive()
        );
    }
}
