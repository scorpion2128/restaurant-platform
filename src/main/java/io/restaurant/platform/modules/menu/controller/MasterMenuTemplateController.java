package io.restaurant.platform.modules.menu.controller;

import io.restaurant.platform.modules.menu.dto.request.AddMasterTemplateItemsRequest;
import io.restaurant.platform.modules.menu.dto.request.CreateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.response.MasterTemplateResponse;
import io.restaurant.platform.modules.menu.service.MasterMenuTemplateService;
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
@RequestMapping("/master-menu-templates")
@RequiredArgsConstructor
public class MasterMenuTemplateController {

    private final MasterMenuTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterTemplateResponse>> create(
            @Valid @RequestBody CreateMasterTemplateRequest request) {
        MasterTemplateResponse response = templateService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Master template created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMasterTemplateRequest request) {
        MasterTemplateResponse response = templateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Master template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Master template deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterTemplateResponse>> findById(@PathVariable Long id) {
        MasterTemplateResponse response = templateService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Master template retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterTemplateResponse>>> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<MasterTemplateResponse> response = templateService.findAllByOrganization(pageable);
        return ResponseEntity.ok(ApiResponse.success("Master templates retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<MasterTemplateResponse>>> findAllActive() {
        List<MasterTemplateResponse> response = templateService.findAllActive();
        return ResponseEntity.ok(ApiResponse.success("Active master templates retrieved successfully", response));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MasterTemplateResponse>> addItems(
            @PathVariable Long id,
            @Valid @RequestBody AddMasterTemplateItemsRequest request) {
        MasterTemplateResponse response = templateService.addItems(id, request);
        return ResponseEntity.ok(ApiResponse.success("Items added successfully", response));
    }

    @DeleteMapping("/{templateId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long templateId,
            @PathVariable Long itemId) {
        templateService.removeItem(templateId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed successfully", null));
    }
}
