package com.thoth.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hardware {

    // ===== Umbrales unicos del sistema para salud y temperatura del disco =====
    /** Salud del disco en ADVERTENCIA si es menor a este valor (%). */
    public static final int DISK_HEALTH_WARNING = 60;
    /** Salud del disco CRITICA si es menor a este valor (%). */
    public static final int DISK_HEALTH_CRITICAL = 30;
    /** Temperatura del disco en ADVERTENCIA si es mayor a este valor (C). */
    public static final int DISK_TEMP_WARNING = 50;
    /** Temperatura del disco CRITICA si es mayor o igual a este valor (C). */
    public static final int DISK_TEMP_CRITICAL = 60;

    public static final String LEVEL_OK = "OK";
    public static final String LEVEL_WARNING = "ADVERTENCIA";
    public static final String LEVEL_CRITICAL = "CRITICO";

    private String processor;
    private Integer ramSizeGb;
    private RamType ramType;
    private DiskType diskType;
    private Integer diskSizeGb;
    private Integer diskHealthPercent;
    private Integer diskTemperatureCelsius;

    public boolean hasCriticalDiskHealth() {
        return isCriticalDiskHealth(diskHealthPercent);
    }

    /** true si la salud esta por debajo del umbral de advertencia (incluye el nivel critico). */
    public boolean hasWarningDiskHealth() {
        return isWarningDiskHealth(diskHealthPercent);
    }

    public boolean hasCriticalTemperature() {
        return isCriticalTemperature(diskTemperatureCelsius);
    }

    /** true si la temperatura supera el umbral de advertencia (incluye el nivel critico). */
    public boolean hasWarningTemperature() {
        return isWarningTemperature(diskTemperatureCelsius);
    }

    public static boolean isCriticalDiskHealth(Integer healthPercent) {
        return healthPercent != null && healthPercent < DISK_HEALTH_CRITICAL;
    }

    public static boolean isWarningDiskHealth(Integer healthPercent) {
        return healthPercent != null && healthPercent < DISK_HEALTH_WARNING;
    }

    public static boolean isCriticalTemperature(Integer temperatureCelsius) {
        return temperatureCelsius != null && temperatureCelsius >= DISK_TEMP_CRITICAL;
    }

    public static boolean isWarningTemperature(Integer temperatureCelsius) {
        return temperatureCelsius != null && temperatureCelsius > DISK_TEMP_WARNING;
    }

    /** Nivel de la salud del disco: "OK", "ADVERTENCIA" o "CRITICO". */
    public static String diskHealthLevel(Integer healthPercent) {
        if (isCriticalDiskHealth(healthPercent)) return LEVEL_CRITICAL;
        if (isWarningDiskHealth(healthPercent)) return LEVEL_WARNING;
        return LEVEL_OK;
    }

    /** Nivel de la temperatura del disco: "OK", "ADVERTENCIA" o "CRITICO". */
    public static String diskTemperatureLevel(Integer temperatureCelsius) {
        if (isCriticalTemperature(temperatureCelsius)) return LEVEL_CRITICAL;
        if (isWarningTemperature(temperatureCelsius)) return LEVEL_WARNING;
        return LEVEL_OK;
    }

    /** true si el bloque no tiene ningun dato registrado. */
    public boolean isEmpty() {
        return (processor == null || processor.isBlank()) && ramSizeGb == null && ramType == null
            && diskType == null && diskSizeGb == null && diskHealthPercent == null
            && diskTemperatureCelsius == null;
    }
}
