package com.thoth.domain.valueobject;

public enum EquipmentCategory {
    DESKTOP("PC de Escritorio"),
    LAPTOP("Portatil"),
    SERVER("Servidor"),
    PRINTER("Impresora"),
    NETWORK("Dispositivo de Red"),
    PERIPHERAL("Periferico"),
    STORAGE("Almacenamiento"),
    MONITOR("Monitor"),
    UPS("SAI/UPS"),
    OTHER("Otro");
    
    private final String displayName;
    
    EquipmentCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
