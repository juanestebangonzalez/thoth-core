package com.thoth.application.port.output;

import com.thoth.domain.model.MaintenanceHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceHistoryRepositoryPort {
    MaintenanceHistory save(MaintenanceHistory maintenance);
    Optional<MaintenanceHistory> findById(UUID id);
    List<MaintenanceHistory> findByEquipmentId(UUID equipmentId);
    long countByEquipmentId(UUID equipmentId);
}