package com.thoth.application.usecase.impl;

import com.thoth.application.dto.MaintenanceHistoryDTO;
import com.thoth.application.mapper.MaintenanceHistoryDtoMapper;
import com.thoth.application.port.input.GetMaintenanceHistoryUseCase;
import com.thoth.application.port.output.MaintenanceHistoryRepositoryPort;
import com.thoth.domain.model.MaintenanceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMaintenanceHistoryUseCaseImpl implements GetMaintenanceHistoryUseCase {

    private final MaintenanceHistoryRepositoryPort repository;
    private final MaintenanceHistoryDtoMapper mapper;

    @Override
    public List<MaintenanceHistoryDTO> getByEquipmentId(UUID equipmentId) {
        return repository.findByEquipmentId(equipmentId).stream()
            .map(mapper::toDTO)
            .toList();
    }

    @Override
    public MaintenanceHistoryDTO getById(UUID maintenanceId) {
        MaintenanceHistory maintenance = repository.findById(maintenanceId)
            .orElseThrow(() -> new IllegalArgumentException("Historial de mantenimiento no encontrado: " + maintenanceId));
        return mapper.toDTO(maintenance);
    }

    @Override
    public long countByEquipmentId(UUID equipmentId) {
        return repository.countByEquipmentId(equipmentId);
    }
}