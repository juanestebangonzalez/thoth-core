package com.thoth.application.exception;

import java.util.UUID;

public class EquipmentNotFoundException extends BusinessException {
    public EquipmentNotFoundException(UUID equipmentId) {
        super("Equipment not found: " + equipmentId);
    }
    
    public EquipmentNotFoundException(String serialNumber) {
        super("Equipment not found: " + serialNumber);
    }
}
