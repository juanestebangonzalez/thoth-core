package com.thoth.application.port.output;

import com.thoth.domain.valueobject.MaintenanceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRepositoryPort {
    Object save(Object maintenance);
    Optional<Object> findById(UUID maintenanceId);
    List<Object> findByEquipmentId(UUID equipmentId);
    List<Object> findByType(MaintenanceType type);
}
