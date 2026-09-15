package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceHistoryJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Reportes y estadisticas para gerencia")
@RequiredArgsConstructor
public class ReportsController {

    private final EquipmentJpaRepository equipmentRepository;
    private final MaintenanceHistoryJpaRepository maintenanceRepository;

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
}