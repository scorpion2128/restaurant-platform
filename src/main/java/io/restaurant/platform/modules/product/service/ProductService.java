package io.restaurant.platform.modules.product.service;

import io.restaurant.platform.modules.product.dto.request.CreateProductRequest;
import io.restaurant.platform.modules.product.dto.request.UpdateProductRequest;
import io.restaurant.platform.modules.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);

    ProductResponse findById(Long id);

    Page<ProductResponse> findAllByRestaurant(Pageable pageable, Long categoryId, Boolean available);

    ProductResponse toggleAvailability(Long id);
}
