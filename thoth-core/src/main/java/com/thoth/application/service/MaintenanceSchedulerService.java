package com.thoth.application.service;

import com.thoth.domain.valueobject.EquipmentCategory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class MaintenanceSchedulerService {

    /**
     * Calcula la proxima fecha de mantenimiento segun la categoria del equipo.
     *
     * Reglas:
     * - LAPTOP / DESKTOP: cada 6 meses
     * - SERVER: cada 3 meses
     * - NETWORK_DEVICE: cada 6 meses
     * - STORAGE: cada 6 meses
     * - UPS: cada 12 meses
     * - MONITOR / PRINTER / PERIPHERAL / OTHER: bajo demanda (no automatico)
     */
    public LocalDate calculateNextMaintenanceDate(String categoryName, LocalDate fromDate) {
        if (categoryName == null || fromDate == null) return null;

        EquipmentCategory category = parseCategory(categoryName);
        if (category == null) return null;

        return switch (category) {
            case LAPTOP, DESKTOP -> fromDate.plusMonths(6);
            case SERVER -> fromDate.plusMonths(3);
            case NETWORK -> fromDate.plusMonths(6);
            case STORAGE -> fromDate.plusMonths(6);
            case UPS -> fromDate.plusMonths(12);
            case MONITOR, PRINTER, PERIPHERAL, OTHER -> null;
        };
    }

    /**
     * Retorna el intervalo en meses segun la categoria.
     */
    public Integer getMaintenanceIntervalMonths(String categoryName) {
        if (categoryName == null) return null;
        EquipmentCategory category = parseCategory(categoryName);
        if (category == null) return null;
        return switch (category) {
            case LAPTOP, DESKTOP -> 6;
            case SERVER -> 3;
            case NETWORK -> 6;
            case STORAGE -> 6;
            case UPS -> 12;
            case MONITOR, PRINTER, PERIPHERAL, OTHER -> null;
        };
    }

    /**
     * Indica si la categoria requiere mantenimientos programados automaticos.
     */
    public boolean isScheduled(String categoryName) {
        return getMaintenanceIntervalMonths(categoryName) != null;
    }

    private EquipmentCategory parseCategory(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return EquipmentCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
