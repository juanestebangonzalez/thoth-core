package com.thoth.application.usecase.impl;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.ListEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListEquipmentUseCaseImpl implements ListEquipmentUseCase {

    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper equipmentMapper;

    /**
     * Lista los equipos aplicando los filtros opcionales:
     * - status: codigo exacto (ACTIVE | MAINTENANCE | INACTIVE | RETIRED).
     * - category: sin distinguir mayusculas/minusculas.
     * Sin filtros devuelve todos los equipos (comportamiento anterior).
     */
    @Override
    public PageResponseDTO<EquipmentDTO> listAll(ListEquipmentCommand command) {
        String status = blankToNull(command.status());
        String category = blankToNull(command.category());

        List<EquipmentDTO> content = equipmentRepository.findAll().stream()
            .filter(e -> status == null || (e.getStatus() != null && e.getStatus().name().equals(status)))
            .filter(e -> category == null || matchesCategory(e, category))
            .map(equipmentMapper::toDTO)
            .collect(Collectors.toList());

        int pageSize = command.pageSize() > 0 ? command.pageSize() : 20;
        return new PageResponseDTO<>(
            content,
            command.pageNumber(),
            command.pageSize(),
            (long) content.size(),
            (content.size() + pageSize - 1) / pageSize
        );
    }

    private static boolean matchesCategory(Equipment equipment, String category) {
        return equipment.getCategory() != null && equipment.getCategory().trim().equalsIgnoreCase(category);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
