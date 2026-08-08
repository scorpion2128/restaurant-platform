package io.restaurant.platform.modules.menu.service;

import io.restaurant.platform.modules.menu.dto.request.CreateMenuSectionRequest;
import io.restaurant.platform.modules.menu.dto.request.UpdateMenuSectionRequest;
import io.restaurant.platform.modules.menu.dto.response.MenuSectionResponse;
import io.restaurant.platform.modules.menu.entity.MasterMenuSection;
import io.restaurant.platform.modules.menu.entity.MasterMenuTemplate;
import io.restaurant.platform.modules.menu.repository.MasterMenuSectionRepository;
import io.restaurant.platform.modules.menu.repository.MasterMenuTemplateRepository;
import io.restaurant.platform.shared.exception.BusinessException;
import io.restaurant.platform.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for menu sections
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MenuSectionServiceImpl implements MenuSectionService {

    private static final String TEMPLATE_NOT_FOUND = "Master template with id %d not found.";
    private static final String SECTION_NOT_FOUND = "Section with id %d not found.";
    private static final String SECTION_NAME_EXISTS = "Section with name '%s' already exists in this template.";
    private static final String SECTION_TEMPLATE_MISMATCH = "Section does not belong to this template.";

    private final MasterMenuSectionRepository sectionRepository;
    private final MasterMenuTemplateRepository templateRepository;

    @Override
    public MenuSectionResponse create(Long templateId, CreateMenuSectionRequest request) {
        MasterMenuTemplate template = getTemplate(templateId);

        // Check if section with same name already exists in this template
        if (sectionRepository.existsByMasterTemplateIdAndName(templateId, request.name())) {
            throw new BusinessException(SECTION_NAME_EXISTS.formatted(request.name()));
        }

        // Create section
        MasterMenuSection section = new MasterMenuSection();
        section.setMasterTemplate(template);
        section.setName(request.name());
        section.setDescription(request.description());
        section.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        section.setVisible(request.visible() != null ? request.visible() : true);

        section = sectionRepository.save(section);

        return toResponse(section);
    }

    @Override
    public MenuSectionResponse update(Long templateId, Long id, UpdateMenuSectionRequest request) {
        MasterMenuSection section = getSection(id);

        // Verify section belongs to template
        if (!section.getMasterTemplate().getId().equals(templateId)) {
            throw new BusinessException(SECTION_TEMPLATE_MISMATCH);
        }

        // Check if section with same name already exists (excluding current)
        sectionRepository.findByMasterTemplateIdOrderByDisplayOrderAsc(templateId).stream()
                .filter(existing -> existing.getName().equals(request.name()) && !existing.getId().equals(id))
                .findFirst()
                .ifPresent(existing -> {
                    throw new BusinessException(SECTION_NAME_EXISTS.formatted(request.name()));
                });

        // Update section
        section.setName(request.name());
        section.setDescription(request.description());
        if (request.displayOrder() != null) {
            section.setDisplayOrder(request.displayOrder());
        }
        if (request.visible() != null) {
            section.setVisible(request.visible());
        }

        return toResponse(section);
    }

    @Override
    public void delete(Long templateId, Long id) {
        MasterMenuSection section = getSection(id);

        // Verify section belongs to template
        if (!section.getMasterTemplate().getId().equals(templateId)) {
            throw new BusinessException(SECTION_TEMPLATE_MISMATCH);
        }

        sectionRepository.delete(section);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuSectionResponse findById(Long templateId, Long id) {
        MasterMenuSection section = getSection(id);

        // Verify section belongs to template
        if (!section.getMasterTemplate().getId().equals(templateId)) {
            throw new BusinessException(SECTION_TEMPLATE_MISMATCH);
        }

        return toResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuSectionResponse> findAllByTemplate(Long templateId) {
        // Verify template exists
        getTemplate(templateId);

        List<MasterMenuSection> sections = sectionRepository
                .findByMasterTemplateIdOrderByDisplayOrderAsc(templateId);

        return sections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MasterMenuSection getSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SECTION_NOT_FOUND.formatted(id)));
    }

    private MasterMenuTemplate getTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEMPLATE_NOT_FOUND.formatted(id)));
    }

    private MenuSectionResponse toResponse(MasterMenuSection section) {
        return new MenuSectionResponse(
                section.getId(),
                section.getMasterTemplate().getId(),
                section.getName(),
                section.getDescription(),
                section.getDisplayOrder(),
                section.getVisible(),
                section.getItems() != null ? section.getItems().size() : 0
        );
    }
}
