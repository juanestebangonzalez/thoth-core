package com.thoth.application.exception;

import java.util.UUID;

public class EquipmentNotFoundException extends BusinessException {
    public EquipmentNotFoundException(UUID equipmentId) {
        super("Equipo no encontrado: " + equipmentId);
    }
    
    public EquipmentNotFoundException(String serialNumber) {
        super("Equipo no encontrado: " + serialNumber);
    }
}
