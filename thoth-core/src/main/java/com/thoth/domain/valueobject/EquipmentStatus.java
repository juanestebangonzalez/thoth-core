package com.thoth.domain.valueobject;

public enum EquipmentStatus {
    ACTIVE("Activo"),
    MAINTENANCE("En Mantenimiento"),
    INACTIVE("Inactivo"),
    RETIRED("Retirado");
    
    private final String displayName;
    
    EquipmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
