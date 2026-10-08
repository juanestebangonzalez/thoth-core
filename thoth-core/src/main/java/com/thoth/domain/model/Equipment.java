package com.thoth.domain.model;

import com.thoth.domain.valueobject.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"location"})
public class Equipment {

    private UUID equipmentId;
    private String name;
    private String category;
    private String serialNumber;
    private String inventoryNumber;
    private String macAddress;
    /** Segunda MAC (ej: WiFi en portatiles). Opcional. */
    private String macAddress2;
    private String brand;
    private String model;
    private EquipmentStatus status;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private Location location;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;

    private OwnershipType ownershipType;
    private RentalInfo rentalInfo;
    private Hardware hardware;
    private LocalDate nextMaintenanceDate;
    /** Nombre del centro de costo (catalogo cost_center). Opcional. */
    private String costCenter;
    /** Sistema operativo (WINDOWS, LINUX, MACOS, CHROMEOS, ANDROID, IOS, OTRO, N/A). Opcional. */
    private String operatingSystem;
    /** Version del sistema operativo (texto libre, max. 100). Opcional. */
    private String osVersion;

    /** Cargo del responsable (mayusculas, max. 100). Opcional. */
    private String responsiblePosition;
    /** Documento de identidad del responsable (mayusculas, max. 30). Opcional. */
    private String responsibleDocument;
    /** Celular del responsable (exactamente 10 digitos). Opcional. */
    private String responsiblePhone;
    /** Correo del responsable (minusculas, max. 150). Opcional. */
    private String responsibleEmail;
    /** Direccion IPv4 del equipo. Opcional. */
    private String ipAddress;
    /** Asignacion de la IP: DHCP | FIJA. Opcional. */
    private String ipAssignment;
    /** Equipo (PC) al que esta asociado este monitor. Solo aplica a monitores. */
    private UUID associatedEquipmentId;

    public static Equipment create(
            String name,
            String category,
            String serialNumber,
            String brand,
            String model,
            String macAddress,
            LocalDate purchaseDate,
            BigDecimal purchaseValue,
            Location location,
            String assignedTo,
            String createdBy) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo no puede estar vacio");
        }
        // serialNumber is now optional
        if (purchaseValue != null && purchaseValue.signum() < 0) {
            throw new IllegalArgumentException("El valor de compra no puede ser negativo");
        }
        if (purchaseDate != null && purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de compra no puede ser futura");
        }
        if (location == null) {
            location = Location.of(null, null, null, null);
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("La categoria no puede estar vacia");
        }

        String normalizedMac = normalizeMacAddress(macAddress);
        if (normalizedMac != null && !isValidMacAddress(normalizedMac)) {
            throw new IllegalArgumentException("Formato de direccion MAC invalido. Use 12 caracteres hexadecimales (ej: AABBCCDDEEFF o AA:BB:CC:DD:EE:FF)");
        }

        LocalDateTime now = LocalDateTime.now();

        return Equipment.builder()
            .equipmentId(UUID.randomUUID())
            .name(name.trim())
            .category(category.trim().toUpperCase())
            .serialNumber(serialNumber != null && !serialNumber.isBlank() ? serialNumber.trim() : null)
            .brand(brand != null ? brand.trim() : "")
            .model(model != null ? model.trim() : "")
            .macAddress(normalizedMac != null ? normalizedMac : "")
            .status(EquipmentStatus.ACTIVE)
            .purchaseDate(purchaseDate)
            .purchaseValue(purchaseValue)
            .location(location)
            .assignedTo(assignedTo != null ? assignedTo.trim() : "")
            .ownershipType(OwnershipType.OWNED)
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .lastModifiedBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .build();
    }

    public void markForMaintenance() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException("No se puede enviar a mantenimiento un equipo retirado. Equipo: " + this.name);
        }
        if (this.status != EquipmentStatus.MAINTENANCE) {
            this.status = EquipmentStatus.MAINTENANCE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void markAsActive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException("No se puede reactivar un equipo retirado. Equipo: " + this.name);
        }
        this.status = EquipmentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInactive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException("No se puede marcar como inactivo un equipo retirado. Equipo: " + this.name);
        }
        this.status = EquipmentStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRetired() {
        this.status = EquipmentStatus.RETIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOperational() {
        return this.status == EquipmentStatus.ACTIVE;
    }

    public long getDaysOwnedCount() {
        if (this.purchaseDate == null) return 0;
        return ChronoUnit.DAYS.between(this.purchaseDate, LocalDate.now());
    }

    public double getYearsOwned() {
        long days = getDaysOwnedCount();
        return days / 365.0;
    }

    public boolean isOld() {
        return this.purchaseDate != null && getYearsOwned() > 3;
    }

    public boolean isVeryOld() {
        return this.purchaseDate != null && getYearsOwned() > 5;
    }

    public boolean isRented() {
        return ownershipType == OwnershipType.RENTED;
    }

    public boolean isOwned() {
        return ownershipType == null || ownershipType == OwnershipType.OWNED;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo no puede estar vacio");
        }
        this.name = newName.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLocation(Location newLocation) {
        if (newLocation == null) {
            throw new IllegalArgumentException("La ubicacion es obligatoria");
        }
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }

    public void reassignTo(String newAssignee) {
        this.assignedTo = newAssignee != null ? newAssignee.trim() : "";
        this.updatedAt = LocalDateTime.now();
    }

    public void updateHardware(Hardware newHardware) {
        this.hardware = newHardware;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRentalInfo(RentalInfo newRentalInfo) {
        this.rentalInfo = newRentalInfo;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeOwnershipType(OwnershipType newType) {
        this.ownershipType = newType;
        if (newType == OwnershipType.OWNED) {
            this.rentalInfo = null;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Asigna el centro de costo (en mayusculas). Cadena vacia o null lo limpia. */
    public void updateCostCenter(String newCostCenter) {
        this.costCenter = (newCostCenter == null || newCostCenter.isBlank())
            ? null : newCostCenter.trim().toUpperCase();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Asigna el sistema operativo (en mayusculas). Cadena vacia o null lo limpia.
     * Lanza IllegalArgumentException si no es uno de los valores permitidos.
     */
    public void updateOperatingSystem(String newOperatingSystem) {
        String normalized = OperatingSystemCatalog.normalize(newOperatingSystem);
        if (normalized != null && !OperatingSystemCatalog.VALUES.contains(normalized)) {
            throw new IllegalArgumentException(OperatingSystemCatalog.invalidMessage(newOperatingSystem));
        }
        this.operatingSystem = normalized;
        this.updatedAt = LocalDateTime.now();
    }

    /** Asigna la version del sistema operativo (en mayusculas, max. 100). Cadena vacia o null la limpia. */
    public void updateOsVersion(String newOsVersion) {
        String normalized = (newOsVersion == null || newOsVersion.isBlank()) ? null : newOsVersion.trim().toUpperCase();
        if (normalized != null && normalized.length() > OperatingSystemCatalog.OS_VERSION_MAX_LENGTH) {
            throw new IllegalArgumentException("La version del sistema operativo no puede superar "
                + OperatingSystemCatalog.OS_VERSION_MAX_LENGTH + " caracteres");
        }
        this.osVersion = normalized;
        this.updatedAt = LocalDateTime.now();
    }

    // ===================== Responsable =====================

    /** Cargo del responsable (mayusculas). Cadena vacia o null lo limpia. */
    public void updateResponsiblePosition(String value) {
        requireValid(EquipmentFieldRules.validateResponsiblePosition(value));
        this.responsiblePosition = EquipmentFieldRules.upper(value);
        this.updatedAt = LocalDateTime.now();
    }

    /** Documento del responsable (mayusculas). Cadena vacia o null lo limpia. */
    public void updateResponsibleDocument(String value) {
        requireValid(EquipmentFieldRules.validateResponsibleDocument(value));
        this.responsibleDocument = EquipmentFieldRules.upper(value);
        this.updatedAt = LocalDateTime.now();
    }

    /** Celular del responsable (10 digitos). Cadena vacia o null lo limpia. */
    public void updateResponsiblePhone(String value) {
        requireValid(EquipmentFieldRules.validatePhone(value));
        this.responsiblePhone = EquipmentFieldRules.clean(value);
        this.updatedAt = LocalDateTime.now();
    }

    /** Correo del responsable (minusculas). Cadena vacia o null lo limpia. */
    public void updateResponsibleEmail(String value) {
        requireValid(EquipmentFieldRules.validateEmail(value));
        String v = EquipmentFieldRules.clean(value);
        this.responsibleEmail = v != null ? v.toLowerCase(Locale.ROOT) : null;
        this.updatedAt = LocalDateTime.now();
    }

    // ===================== Red =====================

    /** Direccion IPv4. Cadena vacia o null la limpia. */
    public void updateIpAddress(String value) {
        requireValid(EquipmentFieldRules.validateIpAddress(value));
        this.ipAddress = EquipmentFieldRules.clean(value);
        this.updatedAt = LocalDateTime.now();
    }

    /** Asignacion de IP (DHCP | FIJA). Cadena vacia o null la limpia. */
    public void updateIpAssignment(String value) {
        requireValid(EquipmentFieldRules.validateIpAssignment(value));
        this.ipAssignment = EquipmentFieldRules.normalizeIpAssignment(value);
        this.updatedAt = LocalDateTime.now();
    }

    // ===================== Monitores asociados =====================

    /** true si la categoria del equipo contiene MONITOR. */
    public boolean isMonitor() {
        return isMonitorCategory(this.category);
    }

    public static boolean isMonitorCategory(String category) {
        return category != null && category.toUpperCase(Locale.ROOT).contains("MONITOR");
    }

    /**
     * Valida la asociacion de un monitor a otro equipo. Devuelve el mensaje de error en espanol
     * o null si es valida. Reglas: el equipo debe ser monitor; el destino debe existir, no ser
     * monitor, no estar retirado y no ser el mismo equipo.
     */
    public static String validateAssociation(String monitorCategory, UUID monitorId,
                                             UUID targetId, String targetCategory, EquipmentStatus targetStatus,
                                             boolean targetExists) {
        if (!isMonitorCategory(monitorCategory)) {
            return "Solo los monitores pueden asociarse a otro equipo";
        }
        if (!targetExists || targetId == null) {
            return "El equipo al que se desea asociar el monitor no existe";
        }
        if (monitorId != null && monitorId.equals(targetId)) {
            return "Un monitor no puede asociarse a si mismo";
        }
        if (isMonitorCategory(targetCategory)) {
            return "Un monitor no puede asociarse a otro monitor";
        }
        if (targetStatus == EquipmentStatus.RETIRED) {
            return "No se puede asociar el monitor a un equipo dado de baja (RETIRED)";
        }
        return null;
    }

    /** Asocia este monitor al equipo indicado (valida las reglas; lanza IllegalArgumentException). */
    public void associateTo(Equipment target) {
        requireValid(validateAssociation(this.category, this.equipmentId,
            target != null ? target.getEquipmentId() : null,
            target != null ? target.getCategory() : null,
            target != null ? target.getStatus() : null,
            target != null));
        this.associatedEquipmentId = target.getEquipmentId();
        this.updatedAt = LocalDateTime.now();
    }

    /** Quita la asociacion del monitor. */
    public void clearAssociation() {
        this.associatedEquipmentId = null;
        this.updatedAt = LocalDateTime.now();
    }

    // ===================== Calculados =====================

    /** Vida util calculada a la fecha de hoy (no se persiste). */
    public UsefulLife calculateUsefulLife() {
        return UsefulLife.calculate(this.category, this.purchaseDate,
            this.rentalInfo != null ? this.rentalInfo.getStartDate() : null,
            this.createdAt, LocalDate.now());
    }

    /** Criticidad segun el centro de costo (ALTA | MEDIA | BAJA). */
    public String calculateCriticality() {
        return Criticality.fromCostCenter(this.costCenter);
    }

    private static void requireValid(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }

    /** Asigna la segunda MAC (WiFi) normalizandola y validando su formato. */
    public void updateMacAddress2(String mac) {
        String normalized = normalizeMacAddress(mac);
        if (normalized != null && !isValidMacAddress(normalized)) {
            throw new IllegalArgumentException("Formato de la segunda direccion MAC invalido. Use 12 caracteres hexadecimales (ej: AABBCCDDEEFF o AA:BB:CC:DD:EE:FF)");
        }
        this.macAddress2 = normalized != null ? normalized : "";
        this.updatedAt = LocalDateTime.now();
    }

    private static String normalizeMacAddress(String mac) {
        if (mac == null || mac.isBlank()) return null;

        String clean = mac.replaceAll("[:\\-\\s]", "").toUpperCase();

        if (!clean.matches("^[0-9A-F]{12}$")) {
            return mac.trim().toUpperCase();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clean.length(); i += 2) {
            if (i > 0) sb.append(':');
            sb.append(clean, i, i + 2);
        }
        return sb.toString();
    }

    private static boolean isValidMacAddress(String mac) {
        if (mac == null || mac.isBlank()) {
            return true;
        }
        return mac.matches("^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(equipmentId, equipment.equipmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentId);
    }
}