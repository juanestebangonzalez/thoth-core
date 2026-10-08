package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AreaEntity;
import com.thoth.adapter.out.persistence.entity.CostCenterEntity;
import com.thoth.adapter.out.persistence.entity.DeviceTypeEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceCategoryEntity;
import com.thoth.adapter.out.persistence.entity.SedeEntity;
import com.thoth.adapter.out.persistence.repository.AreaRepository;
import com.thoth.adapter.out.persistence.repository.CostCenterRepository;
import com.thoth.adapter.out.persistence.repository.DeviceTypeJpaRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceCategoryJpaRepository;
import com.thoth.adapter.out.persistence.repository.SedeRepository;
import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentImportResultDTO;
import com.thoth.application.dto.EquipmentImportRowDTO;
import com.thoth.application.dto.EquipmentImportRowResultDTO;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.DiskType;
import com.thoth.domain.valueobject.EquipmentFieldRules;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.OperatingSystemCatalog;
import com.thoth.domain.valueobject.RamType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Importacion masiva de equipos desde la plantilla (JSON ya parseado por el frontend).
 * dryRun=true: solo valida y calcula la proxima fecha de mantenimiento.
 * dryRun=false: importa solo las filas validas, cada una en su propia transaccion.
 *
 * Este metodo NO es transaccional a proposito: cada fila se confirma por separado
 * a traves de {@link EquipmentImportRowProcessor}.
 */
@Service
@RequiredArgsConstructor
public class EquipmentImportService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentImportService.class);

    public static final int MAX_ROWS = 2000;
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_IMPORTED = "IMPORTADO";

    private static final int MAX_RAM_GB = 1024;
    private static final int MAX_DISK_GB = 100000;

    private final DeviceTypeJpaRepository deviceTypeRepository;
    private final SedeRepository sedeRepository;
    private final AreaRepository areaRepository;
    private final CostCenterRepository costCenterRepository;
    private final MaintenanceCategoryJpaRepository maintenanceCategoryRepository;
    private final EquipmentRepositoryPort equipmentRepository;
    private final MaintenanceSchedulerService schedulerService;
    private final EquipmentImportRowProcessor rowProcessor;
    private final AuditService auditService;

    /** Fila ya validada y convertida. */
    private static final class ParsedRow {
        int rowNumber;
        String name;
        String category;
        String serialNumber;
        String inventoryNumber;
        String brand;
        String model;
        String macAddress;
        String macAddress2;
        String sede;
        String area;
        String costCenter;
        String assignedTo;
        String ownershipType = "OWNED";
        LocalDate purchaseDate;
        BigDecimal purchaseValue;
        String rentalCompany;
        String rentalContractNumber;
        LocalDate rentalStartDate;
        LocalDate rentalEndDate;
        BigDecimal rentalMonthlyValue;
        String processor;
        Integer ramSizeGb;
        String ramType;
        String diskType;
        Integer diskSizeGb;
        String operatingSystem;
        String osVersion;
        LocalDate lastMaintenanceDate;
        String lastMaintenanceType;
        String lastMaintenanceTechnician;
        String lastMaintenanceDescription;
        LocalDate nextMaintenanceDate;
        String responsiblePosition;
        String responsibleDocument;
        String responsiblePhone;
        String responsibleEmail;
        String ipAddress;
        String ipAssignment;
        String associatedInventoryNumber;
        final List<String> errors = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        String status = STATUS_OK;
    }

    public EquipmentImportResultDTO importRows(List<EquipmentImportRowDTO> rows, boolean dryRun, String user) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene filas para importar");
        }
        if (rows.size() > MAX_ROWS) {
            throw new IllegalArgumentException("El archivo tiene " + rows.size()
                + " filas; el maximo permitido por importacion es " + MAX_ROWS);
        }
        String performedBy = (user == null || user.isBlank()) ? "SYSTEM" : user;

        // Catalogos cargados una sola vez (clave: nombre en mayusculas -> nombre del catalogo)
        Map<String, String> deviceTypes = new HashMap<>();
        for (DeviceTypeEntity d : deviceTypeRepository.findByActiveTrueOrderByNameAsc()) putName(deviceTypes, d.getName());
        Map<String, String> sedes = new HashMap<>();
        for (SedeEntity s : sedeRepository.findAll()) putName(sedes, s.getName());
        Map<String, String> areas = new HashMap<>();
        for (AreaEntity a : areaRepository.findAll()) putName(areas, a.getName());
        Map<String, String> costCenters = new HashMap<>();
        for (CostCenterEntity c : costCenterRepository.findAll()) putName(costCenters, c.getName());
        Map<String, String> maintenanceTypes = new HashMap<>();
        for (MaintenanceCategoryEntity m : maintenanceCategoryRepository.findByActiveTrueOrderByNameAsc()) putName(maintenanceTypes, m.getName());

        Map<String, Integer> seenInventory = new HashMap<>();
        Map<String, Integer> seenSerial = new HashMap<>();
        // Inventario -> categoria de las filas previas validas (para asociar monitores a equipos del mismo archivo)
        Map<String, String> validInventoryCategories = new HashMap<>();
        List<ParsedRow> parsed = new ArrayList<>();
        int index = 0;
        for (EquipmentImportRowDTO raw : rows) {
            index++;
            ParsedRow row = validate(raw != null ? raw : new EquipmentImportRowDTO(), index,
                deviceTypes, sedes, areas, costCenters, maintenanceTypes, seenInventory, seenSerial);
            validateAssociation(row, validInventoryCategories);
            if (row.errors.isEmpty() && row.inventoryNumber != null) {
                validInventoryCategories.put(row.inventoryNumber, row.category);
            }
            parsed.add(row);
        }

        int imported = 0;
        if (!dryRun) {
            for (ParsedRow row : parsed) {
                if (!row.errors.isEmpty()) continue;
                try {
                    UUID associatedId = resolveAssociatedId(row);
                    rowProcessor.importRow(toCommand(row, performedBy, associatedId), row.lastMaintenanceDate,
                        row.lastMaintenanceType, row.lastMaintenanceTechnician,
                        row.lastMaintenanceDescription, performedBy);
                    row.status = STATUS_IMPORTED;
                    imported++;
                } catch (RuntimeException ex) {
                    log.warn("Importacion: fallo la fila {}: {}", row.rowNumber, ex.getMessage());
                    row.status = STATUS_ERROR;
                    row.errors.add("No se pudo importar: "
                        + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
                }
            }
            auditService.log("IMPORT", "EQUIPMENT", "", "IMPORTACION MASIVA",
                "Importados " + imported + " equipos", performedBy);
        }

        List<EquipmentImportRowResultDTO> results = new ArrayList<>();
        int withErrors = 0;
        for (ParsedRow row : parsed) {
            if (STATUS_ERROR.equals(row.status)) withErrors++;
            results.add(new EquipmentImportRowResultDTO(
                row.rowNumber,
                row.name,
                row.status,
                List.copyOf(row.errors),
                List.copyOf(row.warnings),
                row.nextMaintenanceDate != null ? row.nextMaintenanceDate.toString() : null));
        }
        return new EquipmentImportResultDTO(dryRun, parsed.size(), parsed.size() - withErrors,
            withErrors, imported, results);
    }

    // ------------------------------------------------------------------ validacion

    private ParsedRow validate(EquipmentImportRowDTO raw, int index,
                               Map<String, String> deviceTypes, Map<String, String> sedes,
                               Map<String, String> areas, Map<String, String> costCenters,
                               Map<String, String> maintenanceTypes,
                               Map<String, Integer> seenInventory, Map<String, Integer> seenSerial) {
        ParsedRow row = new ParsedRow();
        List<String> errors = row.errors;
        LocalDate today = LocalDate.now();

        Integer rn = parseIntQuiet(str(raw.getRowNumber()));
        row.rowNumber = rn != null ? rn : index;

        // Nombre y categoria (obligatorios)
        row.name = upper(str(raw.getName()));
        if (row.name == null) errors.add("El nombre del equipo es obligatorio");

        String category = upper(str(raw.getCategory()));
        if (category == null) {
            errors.add("La categoria es obligatoria");
        } else if (!deviceTypes.containsKey(category)) {
            errors.add("La categoria '" + category + "' no existe en los tipos de dispositivo de THOTH");
        } else {
            row.category = deviceTypes.get(category).trim().toUpperCase();
        }

        // Identificadores unicos
        row.inventoryNumber = upper(str(raw.getInventoryNumber()));
        if (row.inventoryNumber != null) {
            Integer prev = seenInventory.putIfAbsent(row.inventoryNumber, row.rowNumber);
            if (prev != null) {
                errors.add("El numero de inventario '" + row.inventoryNumber + "' esta repetido en el archivo (fila " + prev + ")");
            } else if (equipmentRepository.findByInventoryNumber(row.inventoryNumber).isPresent()) {
                errors.add("El numero de inventario '" + row.inventoryNumber + "' ya existe en THOTH");
            }
        }
        row.serialNumber = upper(str(raw.getSerialNumber()));
        if (row.serialNumber != null) {
            Integer prev = seenSerial.putIfAbsent(row.serialNumber, row.rowNumber);
            if (prev != null) {
                errors.add("El numero de serie '" + row.serialNumber + "' esta repetido en el archivo (fila " + prev + ")");
            } else if (equipmentRepository.findBySerialNumber(row.serialNumber).isPresent()) {
                errors.add("El numero de serie '" + row.serialNumber + "' ya existe en THOTH");
            }
        }

        row.brand = upper(str(raw.getBrand()));
        row.model = upper(str(raw.getModel()));
        row.assignedTo = upper(str(raw.getAssignedTo()));

        // MACs
        row.macAddress = str(raw.getMacAddress());
        if (row.macAddress != null && !isValidMac(row.macAddress)) {
            errors.add("La MAC '" + row.macAddress + "' no tiene un formato valido (12 caracteres hexadecimales)");
        }
        row.macAddress2 = str(raw.getMacAddress2());
        if (row.macAddress2 != null && !isValidMac(row.macAddress2)) {
            errors.add("La segunda MAC '" + row.macAddress2 + "' no tiene un formato valido (12 caracteres hexadecimales)");
        }

        // Catalogos de ubicacion y centro de costo (no se crean automaticamente)
        String sede = upper(str(raw.getSede()));
        if (sede != null) {
            if (sedes.containsKey(sede)) row.sede = sedes.get(sede);
            else errors.add("La sede '" + sede + "' no existe en THOTH");
        }
        String area = upper(str(raw.getArea()));
        if (area != null) {
            if (areas.containsKey(area)) row.area = areas.get(area);
            else errors.add("El area '" + area + "' no existe en THOTH");
        }
        String costCenter = upper(str(raw.getCostCenter()));
        if (costCenter != null) {
            if (costCenters.containsKey(costCenter)) row.costCenter = costCenters.get(costCenter).trim().toUpperCase();
            else errors.add("El centro de costo '" + costCenter + "' no existe en THOTH");
        }

        // Propiedad
        String ownership = upper(str(raw.getOwnershipType()));
        if (ownership == null || ownership.equals("PROPIO") || ownership.equals("OWNED")) {
            row.ownershipType = "OWNED";
        } else if (ownership.equals("ALQUILADO") || ownership.equals("RENTED")) {
            row.ownershipType = "RENTED";
        } else {
            errors.add("El tipo de propiedad '" + ownership + "' no es valido (use PROPIO o ALQUILADO)");
        }

        // Compra
        row.purchaseDate = parseDate(str(raw.getPurchaseDate()), "fecha de compra", errors);
        if (row.purchaseDate != null && row.purchaseDate.isAfter(today)) {
            errors.add("La fecha de compra no puede ser futura");
        }
        row.purchaseValue = parseMoney(str(raw.getPurchaseValue()), "valor de compra", errors);

        // Alquiler
        row.rentalCompany = upper(str(raw.getRentalCompany()));
        row.rentalContractNumber = upper(str(raw.getRentalContractNumber()));
        row.rentalStartDate = parseDate(str(raw.getRentalStartDate()), "fecha de inicio del alquiler", errors);
        row.rentalEndDate = parseDate(str(raw.getRentalEndDate()), "fecha de fin del alquiler", errors);
        row.rentalMonthlyValue = parseMoney(str(raw.getRentalMonthlyValue()), "valor mensual del alquiler", errors);
        if ("RENTED".equals(row.ownershipType) && row.rentalMonthlyValue == null) {
            row.warnings.add("Equipo alquilado sin valor mensual");
        }

        // Hardware
        row.processor = upper(str(raw.getProcessor()));
        row.ramSizeGb = parseNonNegativeInt(str(raw.getRamSizeGb()), "tamano de RAM (GB)", MAX_RAM_GB, errors);
        row.diskSizeGb = parseNonNegativeInt(str(raw.getDiskSizeGb()), "tamano de disco (GB)", MAX_DISK_GB, errors);
        String ramType = upper(str(raw.getRamType()));
        if (ramType != null) {
            if (isEnumValue(RamType.class, ramType)) row.ramType = ramType;
            else row.warnings.add("El tipo de RAM '" + ramType + "' no es reconocido; se ignorara");
        }
        String diskType = upper(str(raw.getDiskType()));
        if (diskType != null) {
            if (isEnumValue(DiskType.class, diskType)) row.diskType = diskType;
            else row.warnings.add("El tipo de disco '" + diskType + "' no es reconocido; se ignorara");
        }

        // Sistema operativo (debe ser uno de los valores permitidos) y version (texto libre)
        String os = upper(str(raw.getOperatingSystem()));
        if (os != null) {
            if (OperatingSystemCatalog.VALUES.contains(os)) row.operatingSystem = os;
            else errors.add("El sistema operativo '" + os + "' no es valido (use "
                + String.join(", ", OperatingSystemCatalog.VALUES) + ")");
        }
        row.osVersion = upper(str(raw.getOsVersion()));
        if (row.osVersion != null && row.osVersion.length() > OperatingSystemCatalog.OS_VERSION_MAX_LENGTH) {
            errors.add("La version del sistema operativo no puede superar "
                + OperatingSystemCatalog.OS_VERSION_MAX_LENGTH + " caracteres");
            row.osVersion = null;
        }

        // Ultimo mantenimiento
        row.lastMaintenanceDate = parseDate(str(raw.getLastMaintenanceDate()), "fecha del ultimo mantenimiento", errors);
        if (row.lastMaintenanceDate != null && row.lastMaintenanceDate.isAfter(today)) {
            errors.add("La fecha del ultimo mantenimiento no puede ser futura");
        }
        String lastType = upper(str(raw.getLastMaintenanceType()));
        if (lastType != null) {
            if (maintenanceTypes.containsKey(lastType)) {
                row.lastMaintenanceType = maintenanceTypes.get(lastType).trim().toUpperCase();
            } else {
                errors.add("El tipo de mantenimiento '" + lastType + "' no existe en THOTH");
            }
            if (str(raw.getLastMaintenanceDate()) == null) {
                errors.add("Indique la fecha del ultimo mantenimiento (se indico el tipo pero no la fecha)");
            }
        } else if (str(raw.getLastMaintenanceDate()) != null) {
            errors.add("Indique el tipo del ultimo mantenimiento (se indico la fecha pero no el tipo)");
        }
        row.lastMaintenanceTechnician = upper(str(raw.getLastMaintenanceTechnician()));
        row.lastMaintenanceDescription = str(raw.getLastMaintenanceDescription());

        boolean hasLastMaintenance = row.lastMaintenanceDate != null && row.lastMaintenanceType != null;
        if (str(raw.getLastMaintenanceDate()) == null && lastType == null) {
            row.warnings.add("Sin ultimo mantenimiento: la proxima fecha se calculara desde hoy");
        }

        // Proxima fecha de mantenimiento
        if (row.category != null) {
            row.nextMaintenanceDate = schedulerService.calculateNextMaintenanceDate(
                row.category, hasLastMaintenance ? row.lastMaintenanceDate : today);
        }

        // Responsable (mismas reglas que el formulario)
        row.responsiblePosition = upper(str(raw.getResponsiblePosition()));
        addIfError(errors, EquipmentFieldRules.validateResponsiblePosition(row.responsiblePosition));
        row.responsibleDocument = upper(str(raw.getResponsibleDocument()));
        addIfError(errors, EquipmentFieldRules.validateResponsibleDocument(row.responsibleDocument));
        row.responsiblePhone = str(raw.getResponsiblePhone());
        addIfError(errors, EquipmentFieldRules.validatePhone(row.responsiblePhone));
        String email = str(raw.getResponsibleEmail());
        row.responsibleEmail = email != null ? email.toLowerCase(Locale.ROOT) : null;
        addIfError(errors, EquipmentFieldRules.validateEmail(row.responsibleEmail));

        // Red
        row.ipAddress = str(raw.getIpAddress());
        addIfError(errors, EquipmentFieldRules.validateIpAddress(row.ipAddress));
        String ipAssignment = str(raw.getIpAssignment());
        String ipError = EquipmentFieldRules.validateIpAssignment(ipAssignment);
        addIfError(errors, ipError);
        row.ipAssignment = ipError == null ? EquipmentFieldRules.normalizeIpAssignment(ipAssignment) : null;

        // Monitor asociado (se valida contra BD y filas previas en validateAssociation)
        row.associatedInventoryNumber = upper(str(raw.getAssociatedInventoryNumber()));

        row.status = errors.isEmpty() ? STATUS_OK : STATUS_ERROR;
        return row;
    }

    /**
     * Reglas de asociacion de monitores (iguales al formulario): el equipo debe ser monitor y el destino
     * (por inventario) debe existir en THOTH o en una fila previa valida del archivo, no ser monitor,
     * no estar retirado y no ser el mismo equipo.
     */
    private void validateAssociation(ParsedRow row, Map<String, String> validInventoryCategories) {
        String target = row.associatedInventoryNumber;
        if (target == null) return;
        String error;
        if (target.equals(row.inventoryNumber)) {
            error = "Un monitor no puede asociarse a si mismo";
        } else if (validInventoryCategories.containsKey(target)) {
            error = Equipment.validateAssociation(row.category != null ? row.category : "", null,
                UUID.randomUUID(), validInventoryCategories.get(target), EquipmentStatus.ACTIVE, true);
        } else {
            Equipment existing = equipmentRepository.findByInventoryNumber(target).orElse(null);
            error = Equipment.validateAssociation(row.category != null ? row.category : "", null,
                existing != null ? existing.getEquipmentId() : null,
                existing != null ? existing.getCategory() : null,
                existing != null ? existing.getStatus() : null,
                existing != null);
            if (existing == null) {
                error = "El equipo con inventario '" + target + "' al que se asocia el monitor no existe en THOTH ni en filas previas validas del archivo";
            }
        }
        if (error != null && row.category == null) {
            return; // la categoria ya tiene su propio error
        }
        if (error != null) {
            row.errors.add(error);
            row.status = STATUS_ERROR;
        }
    }

    /** Al importar, el equipo destino ya existe (en BD o importado en una fila anterior). */
    private UUID resolveAssociatedId(ParsedRow row) {
        if (row.associatedInventoryNumber == null) return null;
        return equipmentRepository.findByInventoryNumber(row.associatedInventoryNumber)
            .map(Equipment::getEquipmentId)
            .orElseThrow(() -> new IllegalArgumentException("El equipo con inventario '"
                + row.associatedInventoryNumber + "' al que se asocia el monitor no fue importado"));
    }

    private static void addIfError(List<String> errors, String error) {
        if (error != null) errors.add(error);
    }

    private RegisterEquipmentCommand toCommand(ParsedRow r, String user, UUID associatedId) {
        boolean rented = "RENTED".equals(r.ownershipType);
        return new RegisterEquipmentCommand(
            r.name,
            r.category,
            r.serialNumber,
            r.inventoryNumber,
            r.brand,
            r.model,
            r.macAddress,
            r.purchaseDate,
            r.purchaseValue,
            r.sede,
            "",
            r.area,
            r.assignedTo,
            user,
            r.ownershipType,
            rented ? r.rentalCompany : null,
            null,
            null,
            null,
            rented ? r.rentalStartDate : null,
            rented ? r.rentalEndDate : null,
            rented ? r.rentalContractNumber : null,
            null,
            null,
            r.processor,
            r.ramSizeGb,
            r.ramType,
            r.diskType,
            r.diskSizeGb,
            null,
            null,
            r.macAddress2,
            r.costCenter,
            rented ? r.rentalMonthlyValue : null,
            r.operatingSystem,
            r.osVersion,
            r.responsiblePosition,
            r.responsibleDocument,
            r.responsiblePhone,
            r.responsibleEmail,
            r.ipAddress,
            r.ipAssignment,
            associatedId
        );
    }

    // ------------------------------------------------------------------ utilidades

    private static void putName(Map<String, String> map, String name) {
        if (name != null && !name.isBlank()) map.putIfAbsent(name.trim().toUpperCase(), name.trim());
    }

    /** Convierte cualquier valor del JSON a texto recortado; vacio -> null. */
    static String str(Object value) {
        if (value == null) return null;
        String s;
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (!Double.isInfinite(d) && !Double.isNaN(d) && d == Math.rint(d) && Math.abs(d) < 1e15) {
                s = Long.toString((long) d);
            } else {
                s = BigDecimal.valueOf(d).toPlainString();
            }
        } else if (value instanceof BigDecimal bd) {
            s = bd.toPlainString();
        } else {
            s = value.toString();
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static String upper(String s) {
        return s != null ? s.toUpperCase(Locale.ROOT) : null;
    }

    private static Integer parseIntQuiet(String s) {
        if (s == null) return null;
        try {
            return new BigDecimal(s).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String s, String label, List<String> errors) {
        if (s == null) return null;
        String v = s;
        if (v.length() > 10 && (v.charAt(10) == 'T' || v.charAt(10) == ' ')) v = v.substring(0, 10);
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            errors.add("La " + label + " '" + s + "' no es valida (use el formato AAAA-MM-DD)");
            return null;
        }
    }

    private static BigDecimal parseMoney(String s, String label, List<String> errors) {
        if (s == null) return null;
        String v = s.replace("$", "").replace(" ", "");
        BigDecimal value;
        try {
            value = new BigDecimal(v);
        } catch (NumberFormatException e) {
            try {
                value = new BigDecimal(v.replace(",", ""));
            } catch (NumberFormatException e2) {
                errors.add("El " + label + " '" + s + "' no es un numero valido");
                return null;
            }
        }
        if (value.signum() < 0) {
            errors.add("El " + label + " no puede ser negativo");
            return null;
        }
        return value;
    }

    private static Integer parseNonNegativeInt(String s, String label, int max, List<String> errors) {
        if (s == null) return null;
        Integer value = parseIntQuiet(s);
        if (value == null) {
            errors.add("El " + label + " '" + s + "' no es un numero entero valido");
            return null;
        }
        if (value < 0) {
            errors.add("El " + label + " no puede ser negativo");
            return null;
        }
        if (value > max) {
            errors.add("El " + label + " no puede ser mayor que " + max);
            return null;
        }
        return value;
    }

    private static boolean isValidMac(String mac) {
        String clean = mac.replaceAll("[:\\-\\s]", "").toUpperCase(Locale.ROOT);
        return clean.matches("^[0-9A-F]{12}$");
    }

    private static <E extends Enum<E>> boolean isEnumValue(Class<E> type, String value) {
        for (E e : type.getEnumConstants()) {
            if (e.name().equals(value)) return true;
        }
        return false;
    }
}
