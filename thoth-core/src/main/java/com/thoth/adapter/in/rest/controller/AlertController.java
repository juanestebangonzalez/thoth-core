package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Alertas del sistema")
@RequiredArgsConstructor
public class AlertController {

    private final EquipmentJpaRepository equipmentRepository;

    @GetMapping("/upcoming-maintenance")
    @Operation(summary = "Equipos con mantenimiento proximo (7 dias o menos)")
    public ResponseEntity<List<Map<String, Object>>> upcomingMaintenance() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(7);

        List<Map<String, Object>> alerts = new ArrayList<>();

        for (EquipmentEntity eq : equipmentRepository.findAll()) {
            if (eq.getNextMaintenanceDate() == null) continue;
            if (eq.getStatus() != null && eq.getStatus().name().equals("RETIRED")) continue;
            if (eq.getStatus() != null && eq.getStatus().name().equals("MAINTENANCE")) continue;
            if (eq.getNextMaintenanceDate().isAfter(limit)) continue;

            long daysUntil = ChronoUnit.DAYS.between(today, eq.getNextMaintenanceDate());

            Map<String, Object> alert = new HashMap<>();
            alert.put("equipmentId", eq.getEquipmentId());
            alert.put("name", eq.getName());
            alert.put("category", eq.getCategory() != null ? eq.getCategory().name() : null);
            alert.put("serialNumber", eq.getSerialNumber());
            alert.put("nextMaintenanceDate", eq.getNextMaintenanceDate());
            alert.put("daysUntil", daysUntil);
            alert.put("severity", daysUntil < 0 ? "CRITICAL" : (daysUntil <= 3 ? "HIGH" : "MEDIUM"));
            alert.put("message", daysUntil < 0
                ? "Mantenimiento vencido hace " + Math.abs(daysUntil) + " dias"
                : (daysUntil == 0 ? "Mantenimiento programado para HOY" : "Mantenimiento en " + daysUntil + " dias"));
            alerts.add(alert);
        }

        alerts.sort((a, b) -> Long.compare((long) a.get("daysUntil"), (long) b.get("daysUntil")));
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/hardware-critical")
    @Operation(summary = "Equipos con hardware en estado critico")
    public ResponseEntity<List<Map<String, Object>>> hardwareCritical() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        for (EquipmentEntity eq : equipmentRepository.findAll()) {
            if (eq.getStatus() != null && eq.getStatus().name().equals("RETIRED")) continue;

            List<String> issues = new ArrayList<>();
            if (eq.getHardwareDiskHealthPercent() != null && eq.getHardwareDiskHealthPercent() < 30) {
                issues.add("Salud del disco: " + eq.getHardwareDiskHealthPercent() + "% (CRITICO)");
            }
            if (eq.getHardwareDiskTemperatureCelsius() != null && eq.getHardwareDiskTemperatureCelsius() >= 70) {
                issues.add("Temperatura: " + eq.getHardwareDiskTemperatureCelsius() + "C (CRITICO)");
            }
            if (eq.getHardwareRamSizeGb() != null && eq.getHardwareRamSizeGb() < 4) {
                issues.add("RAM insuficiente: " + eq.getHardwareRamSizeGb() + " GB");
            }

            if (!issues.isEmpty()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("equipmentId", eq.getEquipmentId());
                alert.put("name", eq.getName());
                alert.put("category", eq.getCategory() != null ? eq.getCategory().name() : null);
                alert.put("serialNumber", eq.getSerialNumber());
                alert.put("issues", issues);
                alert.put("severity", "CRITICAL");
                alerts.add(alert);
            }
        }

        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/rental-expiring")
    @Operation(summary = "Alquileres proximos a vencer (30 dias)")
    public ResponseEntity<List<Map<String, Object>>> rentalExpiring() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(30);
        List<Map<String, Object>> alerts = new ArrayList<>();

        for (EquipmentEntity eq : equipmentRepository.findAll()) {
            if (eq.getRentalEndDate() == null) continue;
            if (eq.getStatus() != null && eq.getStatus().name().equals("RETIRED")) continue;
            if (eq.getRentalEndDate().isAfter(limit)) continue;

            long daysUntil = ChronoUnit.DAYS.between(today, eq.getRentalEndDate());

            Map<String, Object> alert = new HashMap<>();
            alert.put("equipmentId", eq.getEquipmentId());
            alert.put("name", eq.getName());
            alert.put("rentalCompany", eq.getRentalCompany());
            alert.put("rentalEndDate", eq.getRentalEndDate());
            alert.put("daysUntil", daysUntil);
            alert.put("severity", daysUntil < 0 ? "CRITICAL" : (daysUntil <= 7 ? "HIGH" : "MEDIUM"));
            alert.put("message", daysUntil < 0
                ? "Contrato vencido hace " + Math.abs(daysUntil) + " dias"
                : "Contrato vence en " + daysUntil + " dias");
            alerts.add(alert);
        }

        alerts.sort((a, b) -> Long.compare((long) a.get("daysUntil"), (long) b.get("daysUntil")));
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen consolidado de todas las alertas")
    public ResponseEntity<Map<String, Object>> summary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("upcomingMaintenance", upcomingMaintenance().getBody().size());
        summary.put("hardwareCritical", hardwareCritical().getBody().size());
        summary.put("rentalExpiring", rentalExpiring().getBody().size());

        int total = (int) summary.get("upcomingMaintenance")
            + (int) summary.get("hardwareCritical")
            + (int) summary.get("rentalExpiring");
        summary.put("total", total);

        return ResponseEntity.ok(summary);
    }
}