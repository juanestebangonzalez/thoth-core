package com.thoth.application.port.output;

import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepositoryPort {
    Equipment save(Equipment equipment);
    Optional<Equipment> findById(UUID equipmentId);
    Optional<Equipment> findBySerialNumber(String serialNumber);
    List<Equipment> findAll();
    List<Equipment> findByStatus(EquipmentStatus status);
    Optional<Equipment> findByInventoryNumber(String inventoryNumber);
    void deleteById(UUID equipmentId);
}
