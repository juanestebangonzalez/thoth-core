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
            throw new IllegalArgumentException("Equipment name cannot be blank");
        }
        // serialNumber is now optional
        if (purchaseValue != null && purchaseValue.signum() < 0) {
            throw new IllegalArgumentException("Purchase value cannot be negative");
        }
        if (purchaseDate != null && purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Purchase date cannot be in the future");
        }
        if (location == null) {
            throw new IllegalArgumentException("La ubicacion es obligatoria");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
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
            .serialNumber(serialNumber != null ? serialNumber.trim() : null)
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
            throw new IllegalStateException("Cannot mark retired equipment for maintenance. Equipment: " + this.name);
        }
        if (this.status != EquipmentStatus.MAINTENANCE) {
            this.status = EquipmentStatus.MAINTENANCE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void markAsActive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException("Cannot reactivate retired equipment. Equipment: " + this.name);
        }
        this.status = EquipmentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInactive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException("Cannot mark retired equipment as inactive. Equipment: " + this.name);
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
            throw new IllegalArgumentException("Equipment name cannot be blank");
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