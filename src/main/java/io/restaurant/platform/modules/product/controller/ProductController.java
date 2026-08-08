package io.restaurant.platform.modules.product.controller;

import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import io.restaurant.platform.modules.product.service.ProductService;
import io.restaurant.platform.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param request the product creation data
     * @return a response containing the created product details with HTTP 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully.", created));
    }

    /**
     * Updates an existing product.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id      the ID of the product to update
     * @param request the updated product data
     * @return a response containing the updated product details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse updated = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", updated));
    }

    /**
     * Deletes a product.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id the ID of the product to delete
     * @return a response confirming deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully.", null));
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id the ID of the product
     * @return a response containing the product details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("id") Long id) {
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Lists all products for the current user's restaurant.
     * Supports filtering by category and availability.
     *
     * @param pageable   pagination information
     * @param categoryId optional category filter
     * @param available  optional availability filter
     * @return a paginated response containing the list of products
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> listProducts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "available", required = false) Boolean available) {
        Page<ProductResponse> products = productService.findAllByRestaurant(pageable, categoryId, available);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Toggles the availability status of a product.
     * Only users with the ADMIN role can perform this operation.
     *
     * @param id the ID of the product to toggle
     * @return a response containing the updated product details
     */
    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleAvailability(@PathVariable("id") Long id) {
        ProductResponse updated = productService.toggleAvailability(id);
        return ResponseEntity.ok(ApiResponse.success("Product availability toggled successfully.", updated));
    }
}
