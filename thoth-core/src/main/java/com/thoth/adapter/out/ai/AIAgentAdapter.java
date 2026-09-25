package com.thoth.adapter.out.ai;

import com.thoth.application.port.output.AIAgentPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Hardware;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class AIAgentAdapter implements AIAgentPort {

    @Override
    public String analyzeMaintenance(Equipment equipment) {
        long daysOwned = equipment.getPurchaseDate() != null
            ? ChronoUnit.DAYS.between(equipment.getPurchaseDate(), LocalDate.now())
            : 0;
        double yearsOwned = daysOwned / 365.0;
        String category = equipment.getCategory() != null ? traducirCategoria(equipment.getCategory()) : "DESCONOCIDO";
        String status = equipment.getStatus() != null ? traducirEstado(equipment.getStatus().name()) : "DESCONOCIDO";
        Hardware hw = equipment.getHardware();

        StringBuilder analisis = new StringBuilder();
        analisis.append("Analisis de Mantenimiento IA para: ").append(equipment.getName()).append("\n");
        analisis.append("Categoria: ").append(category).append("\n");
        analisis.append("Antiguedad: ").append(String.format("%.1f", yearsOwned)).append(" anos\n");
        analisis.append("Estado: ").append(status).append("\n");

        if (hw != null) {
            analisis.append("\n=== HARDWARE ===\n");
            if (hw.getProcessor() != null) analisis.append("Procesador: ").append(hw.getProcessor()).append("\n");
            if (hw.getRamSizeGb() != null) {
                analisis.append("RAM: ").append(hw.getRamSizeGb()).append(" GB");
                if (hw.getRamType() != null) analisis.append(" ").append(hw.getRamType().name());
                analisis.append("\n");
            }
            if (hw.getDiskType() != null) {
                analisis.append("Disco: ").append(hw.getDiskType().name());
                if (hw.getDiskSizeGb() != null) analisis.append(" ").append(hw.getDiskSizeGb()).append(" GB");
                analisis.append("\n");
            }
            if (hw.getDiskHealthPercent() != null) analisis.append("Salud del Disco: ").append(hw.getDiskHealthPercent()).append("%\n");
            if (hw.getDiskTemperatureCelsius() != null) analisis.append("Temperatura del Disco: ").append(hw.getDiskTemperatureCelsius()).append(" C\n");
        }
        analisis.append("\n");

        boolean urgente = false;
        StringBuilder alertas = new StringBuilder();

        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) {
                alertas.append("- CRITICO: Salud del disco en ").append(hw.getDiskHealthPercent())
                       .append("%. Reemplazar disco INMEDIATAMENTE.\n");
                urgente = true;
            } else if (hw.hasWarningDiskHealth()) {
                alertas.append("- ADVERTENCIA: Salud del disco en ").append(hw.getDiskHealthPercent())
                       .append("%. Planificar reemplazo del disco pronto.\n");
            }

            if (hw.hasCriticalTemperature()) {
                alertas.append("- CRITICO: Temperatura del disco en ").append(hw.getDiskTemperatureCelsius())
                       .append("C. Revisar sistema de enfriamiento URGENTE.\n");
                urgente = true;
            } else if (hw.hasWarningTemperature()) {
                alertas.append("- ADVERTENCIA: Temperatura del disco en ").append(hw.getDiskTemperatureCelsius())
                       .append("C. Limpiar ventiladores y revisar pasta termica.\n");
            }

            if (hw.getRamSizeGb() != null && hw.getRamSizeGb() < 8) {
                alertas.append("- ADVERTENCIA: RAM insuficiente (").append(hw.getRamSizeGb())
                       .append(" GB). Considerar ampliar a 16 GB minimo.\n");
            }

            if (hw.getDiskType() != null && hw.getDiskType().name().equals("HDD")) {
                alertas.append("- INFO: Disco HDD detectado. Considerar migrar a SSD/NVMe para mejor rendimiento.\n");
            }
        }

        if (alertas.length() > 0) {
            analisis.append("=== ALERTAS DE HARDWARE ===\n").append(alertas).append("\n");
        }

        if (urgente) {
            analisis.append("RECOMENDACION: URGENTE - Intervencion inmediata requerida.\n");
            analisis.append("NIVEL DE RIESGO: CRITICO\n");
        } else if (yearsOwned < 1) {
            analisis.append("RECOMENDACION: No requiere mantenimiento. El equipo esta en garantia.\n");
            analisis.append("NIVEL DE RIESGO: BAJO\n");
            analisis.append("Proxima revision: En 6 meses.");
        } else if (yearsOwned < 3) {
            analisis.append("RECOMENDACION: Programar mantenimiento preventivo.\n");
            analisis.append("NIVEL DE RIESGO: BAJO-MEDIO\n");
            analisis.append("Acciones sugeridas: Limpiar componentes, actualizar firmware, revisar conexiones.\n");
            analisis.append("Proxima revision: En 3 meses.");
        } else if (yearsOwned < 5) {
            analisis.append("RECOMENDACION: Mantenimiento inmediato requerido.\n");
            analisis.append("NIVEL DE RIESGO: MEDIO-ALTO\n");
            analisis.append("Acciones sugeridas: Cambiar pasta termica, limpieza profunda, revisar bateria.\n");
            analisis.append("Considerar planificacion de presupuesto para reemplazo.\n");
            analisis.append("Proxima revision: Mensual.");
        } else {
            analisis.append("RECOMENDACION: URGENTE - Planificar reemplazo inmediatamente.\n");
            analisis.append("NIVEL DE RIESGO: ALTO\n");
            analisis.append("El equipo excede el ciclo de vida recomendado.\n");
            analisis.append("Proxima revision: Monitoreo semanal hasta el reemplazo.");
        }

        return analisis.toString();
    }

    @Override
    public String predictFailure(Equipment equipment) {
        long daysOwned = equipment.getPurchaseDate() != null
            ? ChronoUnit.DAYS.between(equipment.getPurchaseDate(), LocalDate.now())
            : 0;
        double yearsOwned = daysOwned / 365.0;
        String category = equipment.getCategory() != null ? equipment.getCategory() : "UNKNOWN";
        Hardware hw = equipment.getHardware();

        double failureProbability;
        String timeframe;
        String components;

        if (yearsOwned < 1) {
            failureProbability = 2.0;
            timeframe = "12+ meses";
            components = "Ninguno - equipo nuevo";
        } else if (yearsOwned < 2) {
            failureProbability = 8.0;
            timeframe = "9-12 meses";
            components = "Bateria, perifericos menores";
        } else if (yearsOwned < 3) {
            failureProbability = 18.0;
            timeframe = "6-9 meses";
            components = "Disco duro, bateria, teclado";
        } else if (yearsOwned < 5) {
            failureProbability = 35.0;
            timeframe = "3-6 meses";
            components = "Unidad de almacenamiento, pantalla, capacitores de la placa madre";
        } else {
            failureProbability = 65.0;
            timeframe = "0-3 meses";
            components = "Criticos: placa madre, fuente de poder, todas las partes mecanicas";
        }

        if ("LAPTOP".equals(category) || "DESKTOP".equals(category)) {
            failureProbability *= 1.15;
        }

        StringBuilder alertHw = new StringBuilder();
        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) {
                failureProbability = Math.max(failureProbability, 85.0);
                alertHw.append("- Disco con salud critica (").append(hw.getDiskHealthPercent()).append("%)\n");
                components = "Disco duro - fallo inminente. " + components;
            } else if (hw.hasWarningDiskHealth()) {
                failureProbability += 15.0;
                alertHw.append("- Disco con salud degradada (").append(hw.getDiskHealthPercent()).append("%)\n");
            }

            if (hw.hasCriticalTemperature()) {
                failureProbability = Math.max(failureProbability, 75.0);
                alertHw.append("- Temperatura critica del disco (").append(hw.getDiskTemperatureCelsius()).append("C)\n");
            } else if (hw.hasWarningTemperature()) {
                failureProbability += 10.0;
                alertHw.append("- Temperatura elevada del disco (").append(hw.getDiskTemperatureCelsius()).append("C)\n");
            }

            if (hw.getDiskType() != null && hw.getDiskType().name().equals("HDD") && yearsOwned > 3) {
                failureProbability += 8.0;
                alertHw.append("- HDD con mas de 3 anos de uso (mayor tasa de fallo mecanico)\n");
            }
        }

        failureProbability = Math.min(failureProbability, 99.0);

        StringBuilder prediccion = new StringBuilder();
        prediccion.append("Prediccion de Fallos IA para: ").append(equipment.getName()).append("\n\n");
        prediccion.append("Probabilidad de Fallo: ").append(String.format("%.1f%%", failureProbability)).append("\n");
        prediccion.append("Marco de Tiempo Esperado: ").append(timeframe).append("\n");
        prediccion.append("Componentes en Riesgo: ").append(components).append("\n\n");

        if (alertHw.length() > 0) {
            prediccion.append("Factores de Hardware Detectados:\n").append(alertHw).append("\n");
        }

        if (failureProbability > 70) {
            prediccion.append("ALERTA CRITICA: Alto riesgo de fallo inminente. Se recomienda accion inmediata.");
        } else if (failureProbability > 40) {
            prediccion.append("ADVERTENCIA: Riesgo moderado-alto de fallo. Programar mantenimiento urgente.");
        } else if (failureProbability > 20) {
            prediccion.append("PRECAUCION: Riesgo moderado. Programar mantenimiento preventivo.");
        } else {
            prediccion.append("ESTADO: Bajo riesgo de fallo. Continuar monitoreo normal.");
        }

        return prediccion.toString();
    }

    @Override
    public String recommendReplacement(Equipment equipment) {
        long daysOwned = equipment.getPurchaseDate() != null
            ? ChronoUnit.DAYS.between(equipment.getPurchaseDate(), LocalDate.now())
            : 0;
        double yearsOwned = daysOwned / 365.0;
        double originalValue = equipment.getPurchaseValue() != null ? equipment.getPurchaseValue().doubleValue() : 0;
        double currentValue = originalValue * Math.max(0.05, 1.0 - (yearsOwned * 0.20));
        double replacementCost = originalValue * 1.10;
        Hardware hw = equipment.getHardware();

        StringBuilder recomendacion = new StringBuilder();
        recomendacion.append("Recomendacion de Reemplazo IA para: ").append(equipment.getName()).append("\n\n");
        recomendacion.append("Valor Original: $").append(String.format("%.2f", originalValue)).append("\n");
        recomendacion.append("Valor Actual Estimado: $").append(String.format("%.2f", currentValue)).append("\n");
        recomendacion.append("Costo Estimado de Reemplazo: $").append(String.format("%.2f", replacementCost)).append("\n");
        recomendacion.append("Depreciacion: ").append(String.format("%.1f%%", ((originalValue - currentValue) / originalValue) * 100)).append("\n\n");

        boolean urgente = false;
        StringBuilder factoresHw = new StringBuilder();

        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) {
                factoresHw.append("- Salud del disco critica: ").append(hw.getDiskHealthPercent()).append("%\n");
                urgente = true;
            }
            if (hw.hasCriticalTemperature()) {
                factoresHw.append("- Temperatura del disco critica: ").append(hw.getDiskTemperatureCelsius()).append("C\n");
                urgente = true;
            }
            if (hw.getRamSizeGb() != null && hw.getRamSizeGb() < 4) {
                factoresHw.append("- RAM muy baja: ").append(hw.getRamSizeGb()).append(" GB (obsoleta)\n");
                urgente = true;
            }
        }

        if (factoresHw.length() > 0) {
            recomendacion.append("Factores de Hardware Criticos:\n").append(factoresHw).append("\n");
        }

        if (urgente) {
            recomendacion.append("DECISION: REEMPLAZAR INMEDIATAMENTE\n");
            recomendacion.append("Razon: Hardware con fallos criticos detectados.\n");
            recomendacion.append("ROI del reemplazo: Muy alto - previene perdida de datos y tiempo inactivo.");
        } else if (yearsOwned > 5) {
            recomendacion.append("DECISION: REEMPLAZAR INMEDIATAMENTE\n");
            recomendacion.append("Razon: El equipo ha excedido su vida util.\n");
            recomendacion.append("ROI del reemplazo: Alto - reduce costos de mantenimiento y tiempo inactivo.");
        } else if (yearsOwned > 3) {
            recomendacion.append("DECISION: PLANIFICAR REEMPLAZO (6-12 meses)\n");
            recomendacion.append("Razon: El equipo se acerca al fin de su vida util.\n");
            recomendacion.append("Comenzar a presupuestar el reemplazo en el proximo ciclo fiscal.");
        } else if (yearsOwned > 1) {
            recomendacion.append("DECISION: MANTENER EQUIPO ACTUAL\n");
            recomendacion.append("Razon: El equipo aun esta dentro de su vida util.\n");
            recomendacion.append("Continuar con el programa de mantenimiento preventivo.");
        } else {
            recomendacion.append("DECISION: NO SE REQUIERE ACCION\n");
            recomendacion.append("Razon: El equipo es relativamente nuevo.\n");
            recomendacion.append("Proxima revision: En 12 meses.");
        }

        return recomendacion.toString();
    }

    private String traducirCategoria(String cat) {
        return switch (cat) {
            case "LAPTOP" -> "PORTATIL";
            case "DESKTOP" -> "PC ESCRITORIO";
            case "MONITOR" -> "MONITOR";
            case "PRINTER" -> "IMPRESORA";
            case "NETWORK" -> "RED";
            case "SERVER" -> "SERVIDOR";
            case "PERIPHERAL" -> "PERIFERICO";
            default -> cat;
        };
    }

    private String traducirEstado(String est) {
        return switch (est) {
            case "ACTIVE" -> "ACTIVO";
            case "MAINTENANCE" -> "EN MANTENIMIENTO";
            case "INACTIVE" -> "INACTIVO";
            case "RETIRED" -> "RETIRADO";
            default -> est;
        };
    }
}