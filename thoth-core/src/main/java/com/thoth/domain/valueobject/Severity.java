package com.thoth.domain.valueobject;

public enum Severity {
    LOW("Bajo", 1),
    MEDIUM("Medio", 2),
    HIGH("Alto", 3),
    CRITICAL("Critico", 4);
    
    private final String displayName;
    private final int level;
    
    Severity(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isMoreSevereThan(Severity other) {
        return this.level > other.level;
    }
}
