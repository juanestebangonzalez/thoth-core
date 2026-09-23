package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceHistoryJpaRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceRecordJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Reportes y estadisticas para gerencia")
@RequiredArgsConstructor
public class ReportsController {

    private final EquipmentJpaRepository equipmentRepository;
    private final MaintenanceHistoryJpaRepository maintenanceRepository;
    private final MaintenanceRecordJpaRepository maintenanceRecordRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard completo de reportes para gerencia")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<EquipmentEntity> allEquipments = equipmentRepository.findAll();
        List<MaintenanceHistoryEntity> allMaintenance = maintenanceRepository.findAll();

        Map<String, Object> report = new LinkedHashMap<>();

        // ===== KPIs =====
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalEquipos", allEquipments.size());

        BigDecimal totalValue = allEquipments.stream()
            .filter(e -> e.getPurchaseValue() != null)
            .map(EquipmentEntity::getPurchaseValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        kpis.put("valorTotalInventario", totalValue);

        double avgAge = allEquipments.stream()
            .filter(e -> e.getPurchaseDate() != null)
            .mapToDouble(e -> ChronoUnit.DAYS.between(e.getPurchaseDate(), LocalDate.now()) / 365.0)
            .average().orElse(0);
        kpis.put("edadPromedioAnos", Math.round(avgAge * 10.0) / 10.0);

        kpis.put("totalMantenimientos", allMaintenance.size());

        long equiposConHW = allEquipments.stream()
            .filter(e -> e.getHardwareProcessor() != null || e.getHardwareRamSizeGb() != null)
            .count();
        kpis.put("equiposConHardwareRegistrado", equiposConHW);

        report.put("kpis", kpis);

        // ===== Por Categoria =====
        Map<String, Long> byCategory = allEquipments.stream()
            .filter(e -> e.getCategory() != null)
            .collect(Collectors.groupingBy(
                e -> traducirCategoria(e.getCategory().name()),
                Collectors.counting()
            ));
        report.put("porCategoria", sortByValue(byCategory));

        // ===== Por Estado =====
        Map<String, Long> byStatus = allEquipments.stream()
            .filter(e -> e.getStatus() != null)
            .collect(Collectors.groupingBy(
                e -> traducirEstado(e.getStatus().name()),
                Collectors.counting()
            ));
        report.put("porEstado", byStatus);

        // ===== Por Propiedad =====
        Map<String, Long> byOwnership = new LinkedHashMap<>();
        long propios = allEquipments.stream().filter(e -> e.getOwnershipType() == null || e.getOwnershipType().name().equals("OWNED")).count();
        long alquilados = allEquipments.stream().filter(e -> e.getOwnershipType() != null && e.getOwnershipType().name().equals("RENTED")).count();
        byOwnership.put("Propios", propios);
        byOwnership.put("Alquilados", alquilados);
        report.put("porPropiedad", byOwnership);

        // ===== Por Ubicacion =====
        Map<String, Long> byLocation = allEquipments.stream()
            .filter(e -> e.getLocationBuilding() != null && !e.getLocationBuilding().isBlank())
            .collect(Collectors.groupingBy(EquipmentEntity::getLocationBuilding, Collectors.counting()));
        report.put("porUbicacion", sortByValue(byLocation));

        // ===== Hardware - Tipo de Disco =====
        Map<String, Long> byDiskType = allEquipments.stream()
            .filter(e -> e.getHardwareDiskType() != null)
            .collect(Collectors.groupingBy(e -> e.getHardwareDiskType().name(), Collectors.counting()));
        report.put("porTipoDisco", byDiskType);

        // ===== Hardware - Tipo de RAM =====
        Map<String, Long> byRamType = allEquipments.stream()
            .filter(e -> e.getHardwareRamType() != null)
            .collect(Collectors.groupingBy(e -> e.getHardwareRamType().name(), Collectors.counting()));
        report.put("porTipoRam", byRamType);

        // ===== Hardware - Salud promedio de discos =====
        OptionalDouble avgHealth = allEquipments.stream()
            .filter(e -> e.getHardwareDiskHealthPercent() != null)
            .mapToInt(EquipmentEntity::getHardwareDiskHealthPercent)
            .average();
        report.put("saludPromedioDisco", avgHealth.isPresent() ? Math.round(avgHealth.getAsDouble()) : null);

        // ===== Hardware - Temperatura promedio =====
        OptionalDouble avgTemp = allEquipments.stream()
            .filter(e -> e.getHardwareDiskTemperatureCelsius() != null)
            .mapToInt(EquipmentEntity::getHardwareDiskTemperatureCelsius)
            .average();
        report.put("temperaturaPromedio", avgTemp.isPresent() ? Math.round(avgTemp.getAsDouble()) : null);

        // ===== Equipos criticos =====
        List<Map<String, Object>> critical = new ArrayList<>();
        for (EquipmentEntity eq : allEquipments) {
            List<String> issues = new ArrayList<>();
            if (eq.getHardwareDiskHealthPercent() != null && eq.getHardwareDiskHealthPercent() < 30)
                issues.add("Disco: " + eq.getHardwareDiskHealthPercent() + "%");
            if (eq.getHardwareDiskTemperatureCelsius() != null && eq.getHardwareDiskTemperatureCelsius() >= 70)
                issues.add("Temp: " + eq.getHardwareDiskTemperatureCelsius() + "C");
            if (eq.getPurchaseDate() != null && ChronoUnit.YEARS.between(eq.getPurchaseDate(), LocalDate.now()) >= 5)
                issues.add("Edad: " + ChronoUnit.YEARS.between(eq.getPurchaseDate(), LocalDate.now()) + " anos");

            if (!issues.isEmpty()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", eq.getName());
                item.put("serial", eq.getSerialNumber());
                item.put("category", eq.getCategory() != null ? traducirCategoria(eq.getCategory().name()) : "");
                item.put("issues", issues);
                critical.add(item);
            }
        }
        report.put("equiposCriticos", critical);

        // ===== Mantenimientos por mes (ultimos 6 meses) =====
        List<Map<String, Object>> maintByMonth = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthKey = monthStart.format(fmt);
            long preventive = allMaintenance.stream()
                .filter(m -> m.getPerformedDate() != null && m.getPerformedDate().toLocalDate().format(fmt).equals(monthKey) && m.getMaintenanceType().name().equals("PREVENTIVE"))
                .count();
            long corrective = allMaintenance.stream()
                .filter(m -> m.getPerformedDate() != null && m.getPerformedDate().toLocalDate().format(fmt).equals(monthKey) && m.getMaintenanceType().name().equals("CORRECTIVE"))
                .count();
            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", monthKey);
            month.put("preventive", preventive);
            month.put("corrective", corrective);
            month.put("total", preventive + corrective);
            maintByMonth.add(month);
        }
        report.put("mantenimientosPorMes", maintByMonth);

        // ===== Valor por categoria =====
        Map<String, BigDecimal> valueByCategory = allEquipments.stream()
            .filter(e -> e.getCategory() != null && e.getPurchaseValue() != null)
            .collect(Collectors.groupingBy(
                e -> traducirCategoria(e.getCategory().name()),
                Collectors.reducing(BigDecimal.ZERO, EquipmentEntity::getPurchaseValue, BigDecimal::add)
            ));
        report.put("valorPorCategoria", valueByCategory);

        return ResponseEntity.ok(report);
    }

    private String traducirCategoria(String cat) {
        return switch (cat) {
            case "LAPTOP" -> "Portatil";
            case "DESKTOP" -> "PC Escritorio";
            case "MONITOR" -> "Monitor";
            case "PRINTER" -> "Impresora";
            case "NETWORK_DEVICE" -> "Red";
            case "SERVER" -> "Servidor";
            case "PERIPHERAL" -> "Periferico";
            case "STORAGE" -> "Almacenamiento";
            case "UPS" -> "UPS";
            case "OTHER" -> "Otro";
            default -> cat;
        };
    }

    private String traducirEstado(String est) {
        return switch (est) {
            case "ACTIVE" -> "Activo";
            case "MAINTENANCE" -> "Mantenimiento";
            case "INACTIVE" -> "Inactivo";
            case "RETIRED" -> "Retirado";
            default -> est;
        };
    }

    private <K> Map<K, Long> sortByValue(Map<K, Long> map) {
        return map.entrySet().stream()
            .sorted(Map.Entry.<K, Long>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    @GetMapping("/maintenance-report")
    @Operation(summary = "Reporte detallado de mantenimientos por semana y mes")
    public ResponseEntity<Map<String, Object>> maintenanceReport() {
        List<MaintenanceHistoryEntity> all = maintenanceRepository.findAll();
        Map<String, Object> report = new LinkedHashMap<>();

        java.time.LocalDate today = java.time.LocalDate.now();

        // Mantenimientos por semana (ultimas 8 semanas)
        List<Map<String, Object>> byWeek = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            java.time.LocalDate weekStart = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate weekEnd = weekStart.plusDays(6);
            long prev = all.stream().filter(m -> m.getPerformedDate() != null && !m.getPerformedDate().toLocalDate().isBefore(weekStart) && !m.getPerformedDate().toLocalDate().isAfter(weekEnd) && m.getMaintenanceType().name().equals("PREVENTIVE")).count();
            long corr = all.stream().filter(m -> m.getPerformedDate() != null && !m.getPerformedDate().toLocalDate().isBefore(weekStart) && !m.getPerformedDate().toLocalDate().isAfter(weekEnd) && m.getMaintenanceType().name().equals("CORRECTIVE")).count();
            Map<String, Object> week = new LinkedHashMap<>();
            week.put("weekStart", weekStart.toString());
            week.put("weekEnd", weekEnd.toString());
            week.put("label", weekStart.getDayOfMonth() + "-" + weekEnd.getDayOfMonth() + " " + weekStart.getMonth().toString().substring(0, 3));
            week.put("preventive", prev);
            week.put("corrective", corr);
            week.put("total", prev + corr);
            byWeek.add(week);
        }
        report.put("byWeek", byWeek);

        // Mantenimientos por mes (ultimos 12 meses)
        List<Map<String, Object>> byMonth = new ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        for (int i = 11; i >= 0; i--) {
            java.time.LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            String monthKey = monthDate.format(fmt);
            long prev = all.stream().filter(m -> m.getPerformedDate() != null && m.getPerformedDate().toLocalDate().format(fmt).equals(monthKey) && m.getMaintenanceType().name().equals("PREVENTIVE")).count();
            long corr = all.stream().filter(m -> m.getPerformedDate() != null && m.getPerformedDate().toLocalDate().format(fmt).equals(monthKey) && m.getMaintenanceType().name().equals("CORRECTIVE")).count();
            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", monthKey);
            month.put("label", meses[monthDate.getMonthValue() - 1] + " " + monthDate.getYear());
            month.put("preventive", prev);
            month.put("corrective", corr);
            month.put("total", prev + corr);
            byMonth.add(month);
        }
        report.put("byMonth", byMonth);

        // Totales
        long totalPrev = all.stream().filter(m -> m.getMaintenanceType().name().equals("PREVENTIVE")).count();
        long totalCorr = all.stream().filter(m -> m.getMaintenanceType().name().equals("CORRECTIVE")).count();
        report.put("totalPreventive", totalPrev);
        report.put("totalCorrective", totalCorr);
        report.put("total", totalPrev + totalCorr);

        // Por tecnico
        Map<String, Long> byTech = all.stream()
            .filter(m -> m.getTechnicianName() != null)
            .collect(Collectors.groupingBy(MaintenanceHistoryEntity::getTechnicianName, Collectors.counting()));
        report.put("byTechnician", byTech);

        // ===== Por Sede =====
        // Join maintenance history with equipment to get sede (locationBuilding)
        List<EquipmentEntity> allEquipments = equipmentRepository.findAll();
        Map<UUID, String> equipmentSedeMap = allEquipments.stream()
            .collect(Collectors.toMap(EquipmentEntity::getEquipmentId,
                e -> e.getLocationBuilding() != null && !e.getLocationBuilding().isBlank() ? e.getLocationBuilding() : "Sin Sede",
                (a, b) -> a));

        Map<String, Map<String, Long>> bySede = new LinkedHashMap<>();
        for (MaintenanceHistoryEntity m : all) {
            String sede = equipmentSedeMap.getOrDefault(m.getEquipmentId(), "Sin Sede");
            bySede.computeIfAbsent(sede, k -> new LinkedHashMap<>(Map.of("preventive", 0L, "corrective", 0L, "total", 0L)));
            Map<String, Long> counts = bySede.get(sede);
            counts.put("total", counts.get("total") + 1);
            if (m.getMaintenanceType().name().equals("PREVENTIVE")) {
                counts.put("preventive", counts.get("preventive") + 1);
            } else {
                counts.put("corrective", counts.get("corrective") + 1);
            }
        }
        report.put("bySede", bySede);

        // ===== Planeados vs Cumplidos =====
        List<MaintenanceRecordEntity> allRecords = maintenanceRecordRepository.findAll();
        long totalPlanned = allRecords.stream().filter(r -> r.getScheduledDate() != null).count();
        long totalCompleted = allRecords.stream().filter(r -> r.getCompletedDate() != null).count();
        long totalPending = totalPlanned - totalCompleted;
        long totalOverdue = allRecords.stream()
            .filter(r -> r.getScheduledDate() != null && r.getCompletedDate() == null && r.getScheduledDate().isBefore(today))
            .count();
        double complianceRate = totalPlanned > 0 ? (totalCompleted * 100.0 / totalPlanned) : 0;

        Map<String, Object> plannedVsCompleted = new LinkedHashMap<>();
        plannedVsCompleted.put("totalPlanned", totalPlanned);
        plannedVsCompleted.put("totalCompleted", totalCompleted);
        plannedVsCompleted.put("totalPending", totalPending);
        plannedVsCompleted.put("totalOverdue", totalOverdue);
        plannedVsCompleted.put("complianceRate", Math.round(complianceRate * 10.0) / 10.0);

        // Planned vs completed by month (last 6 months)
        List<Map<String, Object>> pvcByMonth = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            java.time.LocalDate monthEnd = monthDate.plusMonths(1).minusDays(1);
            String monthKey = monthDate.format(fmt);
            long planned = allRecords.stream()
                .filter(r -> r.getScheduledDate() != null && !r.getScheduledDate().isBefore(monthDate) && !r.getScheduledDate().isAfter(monthEnd))
                .count();
            long completed = allRecords.stream()
                .filter(r -> r.getCompletedDate() != null && !r.getCompletedDate().isBefore(monthDate) && !r.getCompletedDate().isAfter(monthEnd))
                .count();
            Map<String, Object> monthData = new LinkedHashMap<>();
            monthData.put("month", monthKey);
            monthData.put("label", meses[monthDate.getMonthValue() - 1] + " " + monthDate.getYear());
            monthData.put("planned", planned);
            monthData.put("completed", completed);
            pvcByMonth.add(monthData);
        }
        plannedVsCompleted.put("byMonth", pvcByMonth);

        // Planned vs completed by sede
        Map<String, Map<String, Long>> pvcBySede = new LinkedHashMap<>();
        for (MaintenanceRecordEntity r : allRecords) {
            String sede = equipmentSedeMap.getOrDefault(r.getEquipmentId(), "Sin Sede");
            pvcBySede.computeIfAbsent(sede, k -> new LinkedHashMap<>(Map.of("planned", 0L, "completed", 0L, "overdue", 0L)));
            Map<String, Long> counts = pvcBySede.get(sede);
            if (r.getScheduledDate() != null) counts.put("planned", counts.get("planned") + 1);
            if (r.getCompletedDate() != null) counts.put("completed", counts.get("completed") + 1);
            if (r.getScheduledDate() != null && r.getCompletedDate() == null && r.getScheduledDate().isBefore(today))
                counts.put("overdue", counts.get("overdue") + 1);
        }
        plannedVsCompleted.put("bySede", pvcBySede);

        report.put("plannedVsCompleted", plannedVsCompleted);

        // Ultimos 10 mantenimientos
        List<Map<String, Object>> recent = all.stream()
            .sorted((a, b) -> b.getPerformedDate().compareTo(a.getPerformedDate()))
            .limit(10)
            .map(m -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", m.getMaintenanceId());
                item.put("equipmentId", m.getEquipmentId());
                item.put("type", m.getMaintenanceType().name());
                item.put("date", m.getPerformedDate());
                item.put("technician", m.getTechnicianName());
                item.put("reason", m.getReason());
                item.put("hasSig", m.getSignatureBase64() != null && !m.getSignatureBase64().isBlank());
                return item;
            }).collect(Collectors.toList());
        report.put("recent", recent);

        return ResponseEntity.ok(report);
    }
}