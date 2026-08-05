package io.restaurant.platform.modules.product.controller;

import io.restaurant.platform.modules.product.dto.request.CreateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateMasterCategoryRequest;
import io.restaurant.platform.modules.product.dto.response.MasterCategoryResponse;
import io.restaurant.platform.modules.product.service.MasterProductCategoryService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master-product-categories")
@RequiredArgsConstructor
public class MasterProductCategoryController {

    private final MasterProductCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<MasterCategoryResponse>> create(
            @Valid @RequestBody CreateMasterCategoryRequest request) {
        MasterCategoryResponse response = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Master category created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<MasterCategoryResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMasterCategoryRequest request) {
        MasterCategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Master category updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Master category deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterCategoryResponse>> findById(@PathVariable("id") Long id) {
        MasterCategoryResponse response = categoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Master category retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterCategoryResponse>>> findAll(
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable) {
        Page<MasterCategoryResponse> response = categoryService.findAllByOrganization(pageable);
        return ResponseEntity.ok(ApiResponse.success("Master categories retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MasterCategoryResponse>>> findAllActive() {
        List<MasterCategoryResponse> response = categoryService.findAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active master categories retrieved successfully", response));
    }
}
