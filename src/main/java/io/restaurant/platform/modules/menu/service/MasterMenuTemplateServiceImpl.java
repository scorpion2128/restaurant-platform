package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.auth.security.SecurityContextHelper;
import io.restaurant.platform.modules.menu.dto.request.AddMasterTemplateItemsRequest;
import io.restaurant.platform.modules.menu.dto.request.CreateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMasterTemplateRequest;
import io.restaurant.platform.modules.menu.dto.response.MasterTemplateResponse;
import io.restaurant.platform.modules.menu.entity.MasterMenuSection;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplateItem;
import io.restaurant.platform.modules.menu.mapper.MasterMenuTemplateMapper;
import io.restaurant.platform.modules.menu.repository.MasterMenuSectionRepository;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateItemRepository;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateRepository;
import io.restaurant.platform.modules.organization.entity.Organization;
import io.restaurant.platform.modules.organization.repository.OrganizationRepository;
import io.restaurant.platform.modules.product.entity.MasterProduct;
import io.restaurant.platform.modules.product.repository.MasterProductRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for master menu templates
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MasterMenuTemplateServiceImpl implements MasterMenuTemplateService {

    private static final String TEMPLATE_NOT_FOUND = "Master template with id %d not found.";
    private static final String ITEM_NOT_FOUND = "Template item with id %d not found.";
    private static final String SECTION_NOT_FOUND = "Menu section with id %d not found.";
    private static final String ORGANIZATION_NOT_FOUND = "Organization with id %d not found.";
    private static final String PRODUCT_NOT_FOUND = "Master product with id %d not found.";
    private static final String TEMPLATE_NAME_EXISTS = "Master template with name '%s' already exists.";
    private static final String ITEM_TEMPLATE_MISMATCH = "Item does not belong to this template.";

    private final SecurityContextHelper securityContextHelper;
    private final MasterMenuTemplateRepository templateRepository;
    private final MasterMenuTemplateItemRepository itemRepository;
    private final MasterMenuSectionRepository sectionRepository;
    private final MasterProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final MasterMenuTemplateMapper templateMapper;

    @Override
    public MasterTemplateResponse create(CreateMasterTemplateRequest request) {
        Long organizationId = getCurrentOrganizationId();
        Organization organization = getOrganization(organizationId);

        // Check if template with same name already exists
        if (templateRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
            throw new BusinessException(TEMPLATE_NAME_EXISTS.formatted(request.name()));
        }

        // Create template
        MasterMenuTemplate template = templateMapper.toEntity(request);
        template.setOrganization(organization);
        if (request.active() == null) {
            template.setActive(true);
        }
        template = templateRepository.save(template);

        return templateMapper.toResponse(template);
    }

    @Override
    public MasterTemplateResponse update(Long id, UpdateMasterTemplateRequest request) {
        MasterMenuTemplate template = getTemplate(id);
        Long organizationId = getCurrentOrganizationId();

        // Check if template with same name already exists (excluding current)
        templateRepository.findByOrganizationIdAndName(organizationId, request.name())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BusinessException(TEMPLATE_NAME_EXISTS.formatted(request.name()));
                    }
                });

        // Update template
        templateMapper.updateEntity(request, template);
        if (request.active() != null) {
            template.setActive(request.active());
        }

        return templateMapper.toResponse(template);
    }

    @Override
    public void delete(Long id) {
        MasterMenuTemplate template = getTemplate(id);
        templateRepository.delete(template);
    }

    @Override
    @Transactional(readOnly = true)
    public MasterTemplateResponse findById(Long id) {
        MasterMenuTemplate template = getTemplateWithItems(id);
        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MasterTemplateResponse> findAllByOrganization(Pageable pageable) {
        Long organizationId = getCurrentOrganizationId();
        Page<MasterMenuTemplate> templates = templateRepository.findAll(pageable);
        
        // Load items for all templates
        if (!templates.isEmpty()) {
            List<Long> templateIds = templates.getContent().stream()
                    .map(MasterMenuTemplate::getId)
                    .toList();
            List<MasterMenuTemplate> templatesWithItems = templateRepository.findAllByIdInWithItems(templateIds);
            
            // Replace templates with their items-loaded versions
            return templates.map(template -> {
                MasterMenuTemplate withItems = templatesWithItems.stream()
                        .filter(t -> t.getId().equals(template.getId()))
                        .findFirst()
                        .orElse(template);
                return templateMapper.toResponse(withItems);
            });
        }
        
        return templates.map(templateMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MasterTemplateResponse> findAllActive() {
        Long organizationId = getCurrentOrganizationId();
        List<MasterMenuTemplate> templates = templateRepository
                .findByOrganizationIdAndActiveTrue(organizationId);
        
        return templates.stream()
                .map(templateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MasterTemplateResponse addItems(Long templateId, AddMasterTemplateItemsRequest request) {
        MasterMenuTemplate template = getTemplate(templateId);

        // Get existing items
        List<MasterMenuTemplateItem> existingItems = itemRepository.findByMasterTemplateId(templateId);
        
        // Create a map of existing items: key = (productId, sectionId)
        var existingItemsMap = existingItems.stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> {
                            Long sectionId = item.getSection() != null ? item.getSection().getId() : null;
                            return new java.util.AbstractMap.SimpleEntry<>(item.getMasterProduct().getId(), sectionId);
                        },
                        item -> item
                ));
        
        // Process new items
        var newItemsKeys = new java.util.HashSet<java.util.Map.Entry<Long, Long>>();
        var itemsToSave = new ArrayList<MasterMenuTemplateItem>();
        
        for (AddMasterTemplateItemsRequest.MasterTemplateItemRequest itemRequest : request.items()) {
            MasterProduct product = getProduct(itemRequest.masterProductId());
            Long sectionId = itemRequest.sectionId();
            var key = new java.util.AbstractMap.SimpleEntry<>(itemRequest.masterProductId(), sectionId);
            newItemsKeys.add(key);
            
            // Check if item already exists
            MasterMenuTemplateItem existingItem = existingItemsMap.get(key);
            
            if (existingItem != null) {
                // Update existing item (only displayOrder might change)
                existingItem.setDisplayOrder(itemRequest.displayOrder() != null ? itemRequest.displayOrder() : 0);
                itemsToSave.add(existingItem);
            } else {
                // Create new item
                MasterMenuTemplateItem newItem = new MasterMenuTemplateItem();
                newItem.setMasterTemplate(template);
                newItem.setMasterProduct(product);
                newItem.setDisplayOrder(itemRequest.displayOrder() != null ? itemRequest.displayOrder() : 0);
                
                // Set section if provided
                if (sectionId != null) {
                    MasterMenuSection section = getSection(sectionId);
                    // Verify section belongs to the same template
                    if (!section.getMasterTemplate().getId().equals(templateId)) {
                        throw new BusinessException("Section does not belong to this template");
                    }
                    newItem.setSection(section);
                }
                
                itemsToSave.add(newItem);
            }
        }
        
        // Find items to delete (exist in DB but not in new request)
        var itemsToDelete = existingItemsMap.entrySet().stream()
                .filter(entry -> !newItemsKeys.contains(entry.getKey()))
                .map(java.util.Map.Entry::getValue)
                .toList();
        
        // Delete items that are no longer needed
        if (!itemsToDelete.isEmpty()) {
            itemRepository.deleteAll(itemsToDelete);
        }
        
        // Save new and updated items
        if (!itemsToSave.isEmpty()) {
            itemRepository.saveAll(itemsToSave);
        }

        // Refresh template to get updated items
        template = getTemplateWithItems(templateId);
        return templateMapper.toResponse(template);
    }

    @Override
    public void removeItem(Long templateId, Long itemId) {
        MasterMenuTemplateItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND.formatted(itemId)));
        
        // Verify item belongs to template
        if (!item.getMasterTemplate().getId().equals(templateId)) {
            throw new BusinessException(ITEM_TEMPLATE_MISMATCH);
        }

        itemRepository.delete(item);
    }

    private MasterMenuTemplate getTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(id)));
    }

    private MasterMenuTemplate getTemplateWithItems(Long id) {
        return templateRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(id)));
    }

    private MasterProduct getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND.formatted(id)));
    }

    private MasterMenuSection getSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SECTION_NOT_FOUND.formatted(id)));
    }

    private Organization getOrganization(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ORGANIZATION_NOT_FOUND.formatted(id)));
    }

    private Long getCurrentOrganizationId() {
        return securityContextHelper.getOrganizationId();
    }
}
