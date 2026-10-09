package com.thoth.adapter.out.ai;

import com.thoth.application.port.output.AIAgentPort;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.port.output.MaintenanceHistoryRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.model.MaintenanceHistory;
import com.thoth.domain.valueobject.Criticality;
import com.thoth.domain.valueobject.DiskType;
import com.thoth.domain.valueobject.Hardware;
import com.thoth.domain.valueobject.MaintenanceType;
import com.thoth.domain.valueobject.UsefulLife;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Motor de reglas de analisis de equipos ("IA").
 *
 * Considera antiguedad (compra, inicio del alquiler o fecha de registro), hardware (salud y
 * temperatura del disco con los umbrales de {@link Hardware}, RAM, HDD vs SSD), sistema operativo,
 * historial de mantenimientos (total, correctivos de los ultimos 12 meses, dias desde el ultimo,
 * mantenimiento vencido), partes reemplazadas y si el equipo es alquilado.
 *
 * El controlador entrega un {@link Equipment} parcial; aqui se recarga el equipo completo y su
 * historial a traves de los puertos de salida (sin depender de adapter.in).
 */
@Component
public class AIAgentAdapter implements AIAgentPort {

    private static final Logger log = LoggerFactory.getLogger(AIAgentAdapter.class);

    static final String URGENTE = "URGENTE";
    static final String PROGRAMAR = "PROGRAMAR";
    static final String MONITOREAR = "MONITOREAR";

    private static final int RAM_MINIMA_GB = 8;
    /** Windows 7, 8, 8.1 o 10 (sin soporte del fabricante). */
    private static final Pattern WINDOWS_SIN_SOPORTE = Pattern.compile("(?<!\\d)(7|8(\\.1)?|10)(?!\\d)");

    private final EquipmentRepositoryPort equipmentRepository;
    private final MaintenanceHistoryRepositoryPort maintenanceRepository;

    public AIAgentAdapter(EquipmentRepositoryPort equipmentRepository,
                          MaintenanceHistoryRepositoryPort maintenanceRepository) {
        this.equipmentRepository = equipmentRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    // =====================================================================================
    // Contexto del analisis
    // =====================================================================================

    /** Recomendacion priorizada. */
    record Recomendacion(String prioridad, String texto) {
        int orden() {
            return switch (prioridad) {
                case URGENTE -> 0;
                case PROGRAMAR -> 1;
                default -> 2;
            };
        }
    }

    /** Datos derivados del equipo y su historial. */
    static final class Contexto {
        Equipment equipo;
        Hardware hw;
        double anos;
        boolean antiguedadEstimada;
        boolean antiguedadDesconocida;
        UsefulLife vidaUtil;
        int totalMantenimientos;
        int correctivosUltimoAno;
        Long diasDesdeUltimo;
        boolean mantenimientoVencido;
        long diasVencido;
        int partesReemplazadas;
        boolean ramInsuficiente;
        boolean discoHdd;
        boolean soSinSoporte;
        boolean alquilado;
    }

    Contexto construirContexto(Equipment recibido) {
        Equipment equipo = recargar(recibido);
        List<MaintenanceHistory> historial = cargarHistorial(equipo);
        LocalDate hoy = LocalDate.now();

        Contexto c = new Contexto();
        c.equipo = equipo;
        c.hw = equipo.getHardware();
        c.alquilado = equipo.isRented();

        // Antiguedad: compra -> inicio del alquiler -> fecha de registro (misma regla que la vida util del dominio)
        LocalDate inicio = UsefulLife.startDate(equipo.getPurchaseDate(),
            equipo.getRentalInfo() != null ? equipo.getRentalInfo().getStartDate() : null,
            equipo.getCreatedAt());
        c.antiguedadEstimada = equipo.getPurchaseDate() == null;
        c.vidaUtil = UsefulLife.calculate(equipo.getCategory(), equipo.getPurchaseDate(),
            equipo.getRentalInfo() != null ? equipo.getRentalInfo().getStartDate() : null,
            equipo.getCreatedAt(), hoy);
        if (inicio != null && !inicio.isAfter(hoy)) {
            c.anos = UsefulLife.ageInYears(inicio, hoy);
        } else {
            c.anos = 0;
            c.antiguedadEstimada = true;
            c.antiguedadDesconocida = inicio == null;
        }

        // Historial de mantenimientos
        c.totalMantenimientos = historial.size();
        LocalDateTime haceUnAno = LocalDateTime.now().minusMonths(12);
        LocalDateTime ultimo = null;
        for (MaintenanceHistory m : historial) {
            LocalDateTime fecha = m.getPerformedDate();
            if (fecha != null && (ultimo == null || fecha.isAfter(ultimo))) ultimo = fecha;
            if (MaintenanceType.isCorrective(m.getMaintenanceType()) && fecha != null && fecha.isAfter(haceUnAno)) {
                c.correctivosUltimoAno++;
            }
            c.partesReemplazadas += m.getPartsCount();
        }
        c.diasDesdeUltimo = ultimo != null ? ChronoUnit.DAYS.between(ultimo.toLocalDate(), hoy) : null;
        if (equipo.getNextMaintenanceDate() != null && equipo.getNextMaintenanceDate().isBefore(hoy)) {
            c.mantenimientoVencido = true;
            c.diasVencido = ChronoUnit.DAYS.between(equipo.getNextMaintenanceDate(), hoy);
        }

        // Hardware y sistema operativo
        Hardware hw = c.hw;
        c.ramInsuficiente = hw != null && hw.getRamSizeGb() != null && hw.getRamSizeGb() < RAM_MINIMA_GB;
        c.discoHdd = hw != null && hw.getDiskType() == DiskType.HDD;
        c.soSinSoporte = esSistemaOperativoSinSoporte(equipo.getOperatingSystem(), equipo.getOsVersion(), equipo.getOsEdition());
        return c;
    }

    /** WINDOWS con version 7, 8, 8.1 o 10. */
    static boolean esSistemaOperativoSinSoporte(String sistemaOperativo, String version) {
        if (sistemaOperativo == null || version == null) return false;
        if (!"WINDOWS".equals(sistemaOperativo.trim().toUpperCase(Locale.ROOT))) return false;
        return WINDOWS_SIN_SOPORTE.matcher(version).find();
    }

    /**
     * Regla con licenciamiento: si hay edicion, WINDOWS 10 = sin soporte (WINDOWS 11 = con soporte);
     * sin edicion (datos antiguos) aplica la regla por texto de la version.
     */
    static boolean esSistemaOperativoSinSoporte(String sistemaOperativo, String version, String edicion) {
        if (sistemaOperativo == null || !"WINDOWS".equals(sistemaOperativo.trim().toUpperCase(Locale.ROOT))) return false;
        if (edicion != null && !edicion.isBlank()) {
            return "WINDOWS 10".equals(edicion.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " "));
        }
        return esSistemaOperativoSinSoporte(sistemaOperativo, version);
    }

    /** Descripcion del SO para los mensajes: edicion + version, o 'WINDOWS version' en datos antiguos. */
    private static String describirSo(Equipment eq) {
        if (eq.getOsEdition() != null && !eq.getOsEdition().isBlank()) {
            return eq.getOsEdition() + (eq.getOsVersion() != null ? " " + eq.getOsVersion() : "");
        }
        return "WINDOWS" + (eq.getOsVersion() != null ? " " + eq.getOsVersion() : "");
    }

    private Equipment recargar(Equipment recibido) {
        if (recibido == null) throw new IllegalArgumentException("El equipo es obligatorio para el analisis");
        if (recibido.getEquipmentId() == null || equipmentRepository == null) return recibido;
        try {
            Optional<Equipment> completo = equipmentRepository.findById(recibido.getEquipmentId());
            return completo.orElse(recibido);
        } catch (RuntimeException e) {
            log.warn("Analisis IA: no se pudo recargar el equipo {}: {}", recibido.getEquipmentId(), e.getMessage());
            return recibido;
        }
    }

    private List<MaintenanceHistory> cargarHistorial(Equipment equipo) {
        if (equipo.getEquipmentId() == null || maintenanceRepository == null) return List.of();
        try {
            List<MaintenanceHistory> historial = maintenanceRepository.findByEquipmentId(equipo.getEquipmentId());
            return historial != null ? historial.stream().filter(Objects::nonNull).toList() : List.of();
        } catch (RuntimeException e) {
            log.warn("Analisis IA: no se pudo cargar el historial del equipo {}: {}", equipo.getEquipmentId(), e.getMessage());
            return List.of();
        }
    }

    // =====================================================================================
    // Analisis de mantenimiento
    // =====================================================================================

    @Override
    public String analyzeMaintenance(Equipment equipment) {
        Contexto c = construirContexto(equipment);
        Equipment eq = c.equipo;
        Hardware hw = c.hw;
        List<Recomendacion> recs = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("Analisis de Mantenimiento IA para: ").append(eq.getName()).append("\n");
        appendResumen(sb, c);

        if (hw != null) {
            sb.append("\n=== HARDWARE ===\n");
            appendHardware(sb, hw);
        }

        sb.append("\n=== HISTORIAL DE MANTENIMIENTO ===\n");
        appendHistorial(sb, c);

        // --- Reglas de hardware ---
        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) {
                recs.add(new Recomendacion(URGENTE, "Salud del disco en " + hw.getDiskHealthPercent()
                    + "% (CRITICO). Respaldar la informacion y reemplazar el disco de inmediato."));
            } else if (hw.hasWarningDiskHealth()) {
                recs.add(new Recomendacion(PROGRAMAR, "Salud del disco en " + hw.getDiskHealthPercent()
                    + "% (ADVERTENCIA). Planificar el reemplazo del disco y verificar respaldos."));
            }
            if (hw.hasCriticalTemperature()) {
                recs.add(new Recomendacion(URGENTE, "Temperatura del disco en " + hw.getDiskTemperatureCelsius()
                    + "C (CRITICO). Revisar ventilacion y sistema de enfriamiento."));
            } else if (hw.hasWarningTemperature()) {
                recs.add(new Recomendacion(PROGRAMAR, "Temperatura del disco en " + hw.getDiskTemperatureCelsius()
                    + "C (ADVERTENCIA). Limpiar ventiladores y revisar pasta termica."));
            }
        }
        if (c.ramInsuficiente) {
            recs.add(new Recomendacion(c.alquilado ? MONITOREAR : PROGRAMAR, "RAM de " + hw.getRamSizeGb()
                + " GB (minimo recomendado " + RAM_MINIMA_GB + " GB). "
                + (c.alquilado ? "Solicitar al arrendador la ampliacion de memoria." : "Ampliar a 8-16 GB.")));
        }
        if (c.discoHdd) {
            recs.add(new Recomendacion(MONITOREAR, "Disco mecanico (HDD). "
                + (c.alquilado ? "Solicitar al arrendador el cambio a SSD." : "Migrar a SSD/NVMe mejora el rendimiento y reduce fallos.")));
        }
        if (c.soSinSoporte) {
            recs.add(new Recomendacion(PROGRAMAR, "Sistema operativo sin soporte (" + describirSo(eq)
                + "). Actualizar a una version con soporte de seguridad."));
        }

        // --- Reglas de historial ---
        if (c.mantenimientoVencido) {
            recs.add(new Recomendacion(URGENTE, "Mantenimiento preventivo vencido hace " + c.diasVencido
                + " dias. Ejecutarlo lo antes posible."));
        } else if (c.totalMantenimientos == 0 && c.anos >= 1) {
            recs.add(new Recomendacion(PROGRAMAR, "El equipo no tiene mantenimientos registrados. Programar un preventivo."));
        } else if (c.diasDesdeUltimo != null && c.diasDesdeUltimo > 365) {
            recs.add(new Recomendacion(PROGRAMAR, "Han pasado " + c.diasDesdeUltimo
                + " dias desde el ultimo mantenimiento. Programar un preventivo."));
        }
        if (c.correctivosUltimoAno >= 3) {
            recs.add(new Recomendacion(URGENTE, c.correctivosUltimoAno + " mantenimientos correctivos en los ultimos 12 meses. "
                + (c.alquilado ? "Reportar la falla recurrente al arrendador y solicitar cambio del equipo."
                               : "Diagnosticar la causa raiz y evaluar el reemplazo.")));
        } else if (c.correctivosUltimoAno == 2) {
            recs.add(new Recomendacion(PROGRAMAR, "2 mantenimientos correctivos en los ultimos 12 meses. Revisar la causa de las fallas."));
        }
        if (c.partesReemplazadas >= 3) {
            recs.add(new Recomendacion(MONITOREAR, c.partesReemplazadas + " partes reemplazadas en su historial. "
                + "Vigilar el costo acumulado de reparaciones."));
        }

        // --- Reglas por antiguedad ---
        String riesgo;
        int vidaUtilAnos = c.vidaUtil != null ? c.vidaUtil.getYears() : UsefulLife.DEFAULT_YEARS;
        if (c.anos >= vidaUtilAnos) {
            recs.add(new Recomendacion(c.alquilado ? PROGRAMAR : URGENTE, c.alquilado
                ? "El equipo supera su vida util (" + vidaUtilAnos + " anos de uso). Solicitar al arrendador la renovacion del equipo."
                : "El equipo excede el ciclo de vida recomendado (" + vidaUtilAnos + " anos). Planificar su reemplazo."));
            riesgo = "ALTO";
        } else if (c.anos >= 3) {
            recs.add(new Recomendacion(PROGRAMAR, "Equipo con mas de 3 anos: limpieza profunda, cambio de pasta termica y revision de bateria."));
            riesgo = "MEDIO-ALTO";
        } else if (c.anos >= 1) {
            recs.add(new Recomendacion(MONITOREAR, "Mantenimiento preventivo de rutina: limpieza, actualizaciones y revision de conexiones."));
            riesgo = "BAJO-MEDIO";
        } else {
            recs.add(new Recomendacion(MONITOREAR, "Equipo reciente: verificar garantia y continuar con el plan preventivo."));
            riesgo = "BAJO";
        }
        if (c.alquilado) {
            recs.add(new Recomendacion(MONITOREAR, "Equipo alquilado: las reparaciones y repuestos deben gestionarse con el arrendador"
                + (eq.getRentalInfo() != null && eq.getRentalInfo().getRentalCompany() != null
                    ? " (" + eq.getRentalInfo().getRentalCompany() + ")" : "") + ", no comprarlos."));
        }

        boolean hayUrgente = recs.stream().anyMatch(r -> URGENTE.equals(r.prioridad()));
        if (hayUrgente) riesgo = "CRITICO";

        appendRecomendaciones(sb, recs);
        sb.append("\nRECOMENDACION: ").append(hayUrgente
            ? "URGENTE - Intervencion inmediata requerida."
            : (recs.stream().anyMatch(r -> PROGRAMAR.equals(r.prioridad()))
                ? "Programar mantenimiento preventivo." : "Continuar con el monitoreo normal.")).append("\n");
        sb.append("NIVEL DE RIESGO: ").append(riesgo).append("\n");
        sb.append("Proxima revision: ").append(hayUrgente ? "Inmediata." : (riesgo.startsWith("MEDIO") || "ALTO".equals(riesgo)
            ? "Mensual." : "En 3 a 6 meses."));
        return sb.toString();
    }

    // =====================================================================================
    // Prediccion de fallos
    // =====================================================================================

    @Override
    public String predictFailure(Equipment equipment) {
        Contexto c = construirContexto(equipment);
        Equipment eq = c.equipo;
        Hardware hw = c.hw;
        double anos = c.anos;

        double probabilidad;
        String plazo;
        String componentes;
        if (anos < 1) {
            probabilidad = 2.0; plazo = "12+ meses"; componentes = "Ninguno - equipo nuevo";
        } else if (anos < 2) {
            probabilidad = 8.0; plazo = "9-12 meses"; componentes = "Bateria, perifericos menores";
        } else if (anos < 3) {
            probabilidad = 18.0; plazo = "6-9 meses"; componentes = "Disco duro, bateria, teclado";
        } else if (anos < 5) {
            probabilidad = 35.0; plazo = "3-6 meses"; componentes = "Unidad de almacenamiento, pantalla, capacitores de la placa madre";
        } else {
            probabilidad = 65.0; plazo = "0-3 meses"; componentes = "Criticos: placa madre, fuente de poder, partes mecanicas";
        }
        String categoria = eq.getCategory() != null ? eq.getCategory().toUpperCase(Locale.ROOT) : "";
        if (categoria.equals("LAPTOP") || categoria.equals("DESKTOP") || categoria.contains("PORTATIL")) {
            probabilidad *= 1.15;
        }

        List<String> factores = new ArrayList<>();
        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) {
                probabilidad = Math.max(probabilidad, 85.0);
                factores.add("Disco con salud CRITICA (" + hw.getDiskHealthPercent() + "%)");
                componentes = "Disco duro - fallo inminente. " + componentes;
            } else if (hw.hasWarningDiskHealth()) {
                probabilidad += 15.0;
                factores.add("Disco con salud en ADVERTENCIA (" + hw.getDiskHealthPercent() + "%)");
            }
            if (hw.hasCriticalTemperature()) {
                probabilidad = Math.max(probabilidad, 75.0);
                factores.add("Temperatura del disco CRITICA (" + hw.getDiskTemperatureCelsius() + "C)");
            } else if (hw.hasWarningTemperature()) {
                probabilidad += 10.0;
                factores.add("Temperatura del disco en ADVERTENCIA (" + hw.getDiskTemperatureCelsius() + "C)");
            }
        }
        if (c.discoHdd && anos > 3) {
            probabilidad += 8.0;
            factores.add("HDD con mas de 3 anos de uso (mayor tasa de fallo mecanico)");
        }
        if (c.correctivosUltimoAno > 0) {
            probabilidad += Math.min(25.0, c.correctivosUltimoAno * 8.0);
            factores.add(c.correctivosUltimoAno + " mantenimiento(s) correctivo(s) en los ultimos 12 meses");
        }
        if (c.mantenimientoVencido) {
            probabilidad += 10.0;
            factores.add("Mantenimiento preventivo vencido hace " + c.diasVencido + " dias");
        } else if (c.diasDesdeUltimo != null && c.diasDesdeUltimo > 365) {
            probabilidad += 5.0;
            factores.add(c.diasDesdeUltimo + " dias sin mantenimiento");
        }
        if (c.partesReemplazadas >= 3) {
            probabilidad += 5.0;
            factores.add(c.partesReemplazadas + " partes reemplazadas en su historial");
        }
        if (c.ramInsuficiente) {
            factores.add("RAM de " + hw.getRamSizeGb() + " GB (bajo rendimiento, no aumenta el riesgo de fallo fisico)");
        }
        if (c.soSinSoporte) {
            factores.add("Sistema operativo sin soporte (" + describirSo(eq) + "): riesgo de seguridad");
        }
        probabilidad = Math.min(probabilidad, 99.0);

        StringBuilder sb = new StringBuilder();
        sb.append("Prediccion de Fallos IA para: ").append(eq.getName()).append("\n");
        appendResumen(sb, c);
        sb.append("\nProbabilidad de Fallo: ").append(String.format(Locale.ROOT, "%.1f%%", probabilidad)).append("\n");
        sb.append("Marco de Tiempo Esperado: ").append(plazo).append("\n");
        sb.append("Componentes en Riesgo: ").append(componentes).append("\n");
        if (!factores.isEmpty()) {
            sb.append("\nFactores Detectados:\n");
            factores.forEach(f -> sb.append("- ").append(f).append("\n"));
        }

        List<Recomendacion> recs = new ArrayList<>();
        String accionAlquiler = c.alquilado ? " Coordinar la revision con el arrendador." : "";
        if (probabilidad > 70) {
            recs.add(new Recomendacion(URGENTE, "Alto riesgo de fallo inminente. Respaldar la informacion y actuar de inmediato." + accionAlquiler));
        } else if (probabilidad > 40) {
            recs.add(new Recomendacion(PROGRAMAR, "Riesgo moderado-alto. Programar mantenimiento en las proximas semanas." + accionAlquiler));
        } else if (probabilidad > 20) {
            recs.add(new Recomendacion(PROGRAMAR, "Riesgo moderado. Programar mantenimiento preventivo." + accionAlquiler));
        } else {
            recs.add(new Recomendacion(MONITOREAR, "Bajo riesgo de fallo. Continuar con el monitoreo normal."));
        }
        if (c.soSinSoporte) {
            recs.add(new Recomendacion(PROGRAMAR, "Actualizar el sistema operativo a una version con soporte."));
        }
        appendRecomendaciones(sb, recs);

        sb.append("\n");
        if (probabilidad > 70) {
            sb.append("ALERTA CRITICA: Alto riesgo de fallo inminente. Se recomienda accion inmediata.");
        } else if (probabilidad > 40) {
            sb.append("ADVERTENCIA: Riesgo moderado-alto de fallo. Programar mantenimiento urgente.");
        } else if (probabilidad > 20) {
            sb.append("PRECAUCION: Riesgo moderado. Programar mantenimiento preventivo.");
        } else {
            sb.append("ESTADO: Bajo riesgo de fallo. Continuar monitoreo normal.");
        }
        return sb.toString();
    }

    // =====================================================================================
    // Recomendacion de reemplazo
    // =====================================================================================

    @Override
    public String recommendReplacement(Equipment equipment) {
        Contexto c = construirContexto(equipment);
        Equipment eq = c.equipo;
        Hardware hw = c.hw;
        double anos = c.anos;

        StringBuilder sb = new StringBuilder();
        sb.append("Recomendacion de Reemplazo IA para: ").append(eq.getName()).append("\n");
        appendResumen(sb, c);
        sb.append("\n");

        if (!c.alquilado) {
            double valorOriginal = eq.getPurchaseValue() != null ? eq.getPurchaseValue().doubleValue() : 0;
            if (valorOriginal > 0) {
                double valorActual = valorOriginal * Math.max(0.05, 1.0 - (anos * 0.20));
                sb.append("Valor Original: $").append(String.format(Locale.ROOT, "%,.2f", valorOriginal)).append("\n");
                sb.append("Valor Actual Estimado: $").append(String.format(Locale.ROOT, "%,.2f", valorActual)).append("\n");
                sb.append("Costo Estimado de Reemplazo: $").append(String.format(Locale.ROOT, "%,.2f", valorOriginal * 1.10)).append("\n");
                sb.append("Depreciacion: ").append(String.format(Locale.ROOT, "%.1f%%",
                    ((valorOriginal - valorActual) / valorOriginal) * 100)).append("\n\n");
            } else {
                sb.append("Valor de compra: no registrado (no se calcula depreciacion).\n\n");
            }
        } else if (eq.getRentalInfo() != null && eq.getRentalInfo().getMonthlyValue() != null) {
            sb.append("Valor mensual del alquiler: $")
              .append(String.format(Locale.ROOT, "%,.2f", eq.getRentalInfo().getMonthlyValue().doubleValue())).append("\n\n");
        }

        List<String> criticos = new ArrayList<>();
        if (hw != null) {
            if (hw.hasCriticalDiskHealth()) criticos.add("Salud del disco CRITICA: " + hw.getDiskHealthPercent() + "%");
            if (hw.hasCriticalTemperature()) criticos.add("Temperatura del disco CRITICA: " + hw.getDiskTemperatureCelsius() + "C");
            if (hw.getRamSizeGb() != null && hw.getRamSizeGb() < 4) criticos.add("RAM muy baja: " + hw.getRamSizeGb() + " GB (obsoleta)");
        }
        if (c.correctivosUltimoAno >= 3) criticos.add(c.correctivosUltimoAno + " correctivos en los ultimos 12 meses (falla recurrente)");

        List<String> menores = new ArrayList<>();
        if (c.ramInsuficiente && (hw.getRamSizeGb() >= 4)) menores.add("RAM de " + hw.getRamSizeGb() + " GB (menos de 8 GB)");
        if (c.discoHdd) menores.add("Disco HDD (lento y con mayor tasa de fallo)");
        if (c.soSinSoporte) menores.add("Sistema operativo sin soporte (" + describirSo(eq) + ")");
        if (c.partesReemplazadas >= 3) menores.add(c.partesReemplazadas + " partes reemplazadas en su historial");
        if (c.correctivosUltimoAno == 2) menores.add("2 correctivos en los ultimos 12 meses");

        if (!criticos.isEmpty()) {
            sb.append("Factores Criticos:\n");
            criticos.forEach(f -> sb.append("- ").append(f).append("\n"));
            sb.append("\n");
        }
        if (!menores.isEmpty()) {
            sb.append("Factores a Considerar:\n");
            menores.forEach(f -> sb.append("- ").append(f).append("\n"));
            sb.append("\n");
        }

        List<Recomendacion> recs = new ArrayList<>();
        if (c.alquilado) {
            String arrendador = eq.getRentalInfo() != null && eq.getRentalInfo().getRentalCompany() != null
                ? eq.getRentalInfo().getRentalCompany() : "el arrendador";
            if (!criticos.isEmpty() || anos >= 5) {
                sb.append("DECISION: GESTIONAR CAMBIO CON EL ARRENDADOR\n");
                sb.append("Razon: Equipo alquilado con ").append(!criticos.isEmpty() ? "fallas criticas" : "mas de 5 anos de uso").append(".\n");
                recs.add(new Recomendacion(URGENTE, "Solicitar a " + arrendador + " la reparacion o el cambio del equipo segun el contrato. "
                    + "No comprar repuestos ni reemplazarlo con recursos propios."));
            } else if (!menores.isEmpty()) {
                sb.append("DECISION: SOLICITAR MEJORAS AL ARRENDADOR\n");
                sb.append("Razon: El equipo alquilado tiene limitaciones que el arrendador debe atender.\n");
                recs.add(new Recomendacion(PROGRAMAR, "Solicitar a " + arrendador + " las mejoras necesarias (memoria, disco o sistema operativo)."));
            } else {
                sb.append("DECISION: MANTENER EQUIPO ALQUILADO\n");
                sb.append("Razon: No se detectan problemas relevantes.\n");
                recs.add(new Recomendacion(MONITOREAR, "Continuar con el contrato vigente y revisar su vencimiento."));
            }
            if (eq.getRentalInfo() != null && eq.getRentalInfo().isContractExpiringSoon()) {
                recs.add(new Recomendacion(PROGRAMAR, "El contrato de alquiler vence pronto: definir con " + arrendador + " su renovacion o el cambio del equipo."));
            }
        } else if (!criticos.isEmpty()) {
            sb.append("DECISION: REEMPLAZAR INMEDIATAMENTE\n");
            sb.append("Razon: Hardware o historial con fallas criticas.\n");
            sb.append("ROI del reemplazo: Muy alto - previene perdida de datos y tiempo inactivo.\n");
            recs.add(new Recomendacion(URGENTE, "Iniciar el proceso de reemplazo y respaldar la informacion del usuario."));
        } else if (anos > 5) {
            sb.append("DECISION: REEMPLAZAR INMEDIATAMENTE\n");
            sb.append("Razon: El equipo ha excedido su vida util.\n");
            sb.append("ROI del reemplazo: Alto - reduce costos de mantenimiento y tiempo inactivo.\n");
            recs.add(new Recomendacion(URGENTE, "Incluir el equipo en el plan de renovacion inmediato."));
        } else if (anos > 3 || menores.size() >= 2) {
            sb.append("DECISION: PLANIFICAR REEMPLAZO (6-12 meses)\n");
            sb.append("Razon: El equipo se acerca al fin de su vida util o acumula limitaciones.\n");
            recs.add(new Recomendacion(PROGRAMAR, "Presupuestar el reemplazo en el proximo ciclo fiscal."));
        } else if (!menores.isEmpty()) {
            sb.append("DECISION: MANTENER Y MEJORAR\n");
            sb.append("Razon: El equipo esta dentro de su vida util; conviene corregir las limitaciones detectadas.\n");
            recs.add(new Recomendacion(PROGRAMAR, "Aplicar mejoras puntuales (RAM, SSD o actualizacion del sistema operativo)."));
        } else if (anos > 1) {
            sb.append("DECISION: MANTENER EQUIPO ACTUAL\n");
            sb.append("Razon: El equipo aun esta dentro de su vida util.\n");
            recs.add(new Recomendacion(MONITOREAR, "Continuar con el programa de mantenimiento preventivo."));
        } else {
            sb.append("DECISION: NO SE REQUIERE ACCION\n");
            sb.append("Razon: El equipo es relativamente nuevo.\n");
            recs.add(new Recomendacion(MONITOREAR, "Proxima revision en 12 meses."));
        }
        appendRecomendaciones(sb, recs);
        return sb.toString().trim();
    }

    // =====================================================================================
    // Utilidades de formato
    // =====================================================================================

    private void appendResumen(StringBuilder sb, Contexto c) {
        Equipment eq = c.equipo;
        sb.append("Categoria: ").append(eq.getCategory() != null ? traducirCategoria(eq.getCategory()) : "DESCONOCIDA").append("\n");
        sb.append("Antiguedad: ");
        if (c.antiguedadDesconocida) {
            sb.append("desconocida (sin fecha de compra, alquiler ni registro)\n");
        } else {
            sb.append(String.format(Locale.ROOT, "%.1f", c.anos)).append(" anos");
            if (c.antiguedadEstimada) sb.append(" (antiguedad estimada)");
            sb.append("\n");
        }
        if (c.vidaUtil != null) {
            sb.append("Vida util: ").append(c.vidaUtil.getYears()).append(" anos (")
              .append(c.vidaUtil.getConsumedPercent()).append("% consumido, ")
              .append(String.format(Locale.ROOT, "%.1f", c.vidaUtil.getRemainingYears())).append(" anos restantes)\n");
        }
        sb.append("Criticidad: ").append(Criticality.fromCostCenter(eq.getCostCenter())).append("\n");
        sb.append("Estado: ").append(eq.getStatus() != null ? traducirEstado(eq.getStatus().name()) : "DESCONOCIDO").append("\n");
        sb.append("Propiedad: ").append(c.alquilado ? "ALQUILADO" : "PROPIO");
        if (c.alquilado && eq.getRentalInfo() != null && eq.getRentalInfo().getRentalCompany() != null) {
            sb.append(" (").append(eq.getRentalInfo().getRentalCompany()).append(")");
        }
        sb.append("\n");
        if (eq.getOperatingSystem() != null) {
            sb.append("Sistema operativo: ").append(eq.getOperatingSystem());
            if (eq.getOsEdition() != null) sb.append(" / ").append(eq.getOsEdition());
            if (eq.getOsVersion() != null) sb.append(" ").append(eq.getOsVersion());
            if (eq.getOsLicenseType() != null) sb.append(" (licencia ").append(eq.getOsLicenseType()).append(")");
            if (c.soSinSoporte) sb.append(" (SIN SOPORTE)");
            sb.append("\n");
        }
    }

    private void appendHardware(StringBuilder sb, Hardware hw) {
        if (hw.getProcessor() != null) sb.append("Procesador: ").append(hw.getProcessor()).append("\n");
        if (hw.getRamSizeGb() != null) {
            sb.append("RAM: ").append(hw.getRamSizeGb()).append(" GB");
            if (hw.getRamType() != null) sb.append(" ").append(hw.getRamType().name());
            sb.append("\n");
        }
        if (hw.getDiskType() != null) {
            sb.append("Disco: ").append(hw.getDiskType().name());
            if (hw.getDiskSizeGb() != null) sb.append(" ").append(hw.getDiskSizeGb()).append(" GB");
            sb.append("\n");
        }
        if (hw.getDiskHealthPercent() != null) {
            sb.append("Salud del Disco: ").append(hw.getDiskHealthPercent()).append("% (")
              .append(Hardware.diskHealthLevel(hw.getDiskHealthPercent())).append(")\n");
        }
        if (hw.getDiskTemperatureCelsius() != null) {
            sb.append("Temperatura del Disco: ").append(hw.getDiskTemperatureCelsius()).append(" C (")
              .append(Hardware.diskTemperatureLevel(hw.getDiskTemperatureCelsius())).append(")\n");
        }
    }

    private void appendHistorial(StringBuilder sb, Contexto c) {
        sb.append("Mantenimientos registrados: ").append(c.totalMantenimientos).append("\n");
        sb.append("Correctivos en los ultimos 12 meses: ").append(c.correctivosUltimoAno).append("\n");
        sb.append("Dias desde el ultimo mantenimiento: ")
          .append(c.diasDesdeUltimo != null ? String.valueOf(c.diasDesdeUltimo) : "sin registros").append("\n");
        sb.append("Partes reemplazadas: ").append(c.partesReemplazadas).append("\n");
        if (c.equipo.getNextMaintenanceDate() != null) {
            sb.append("Proximo mantenimiento: ").append(c.equipo.getNextMaintenanceDate())
              .append(c.mantenimientoVencido ? " (VENCIDO)" : "").append("\n");
        }
    }

    private void appendRecomendaciones(StringBuilder sb, List<Recomendacion> recs) {
        if (recs.isEmpty()) return;
        sb.append("\n=== RECOMENDACIONES (priorizadas) ===\n");
        recs.stream()
            .sorted(Comparator.comparingInt(Recomendacion::orden))
            .forEach(r -> sb.append("[").append(r.prioridad()).append("] ").append(r.texto()).append("\n"));
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
