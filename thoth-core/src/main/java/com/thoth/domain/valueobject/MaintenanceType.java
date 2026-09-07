package com.thoth.domain.valueobject;

public enum MaintenanceType {
    PREVENTIVE("Preventivo"),
    CORRECTIVE("Correctivo"),
    EMERGENCY("Emergencia");
    
    private final String displayName;
    
    MaintenanceType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
