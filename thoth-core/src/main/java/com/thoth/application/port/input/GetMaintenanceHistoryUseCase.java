package com.thoth.application.port.input;

import com.thoth.application.dto.MaintenanceHistoryDTO;
import java.util.List;
import java.util.UUID;

public interface GetMaintenanceHistoryUseCase {
    List<MaintenanceHistoryDTO> getByEquipmentId(UUID equipmentId);
    MaintenanceHistoryDTO getById(UUID maintenanceId);
    long countByEquipmentId(UUID equipmentId);
}