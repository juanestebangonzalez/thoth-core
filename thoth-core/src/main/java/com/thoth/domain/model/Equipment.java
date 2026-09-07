package com.thoth.domain.model;

import com.thoth.domain.valueobject.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"location"})
public class Equipment {
    
    private UUID equipmentId;
    private String name;
    private EquipmentCategory category;
    private String serialNumber;
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
    
    public static Equipment create(
            String name,
            EquipmentCategory category,
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
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be blank");
        }
        if (purchaseValue == null || purchaseValue.signum() <= 0) {
            throw new IllegalArgumentException("Purchase value must be positive");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null");
        }
        if (purchaseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Purchase date cannot be in the future");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (macAddress != null && !isValidMacAddress(macAddress)) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Equipment equipment = Equipment.builder()
            .equipmentId(UUID.randomUUID())
            .name(name.trim())
            .category(category)
            .serialNumber(serialNumber.trim())
            .brand(brand != null ? brand.trim() : "")
            .model(model != null ? model.trim() : "")
            .macAddress(macAddress != null ? macAddress.trim() : "")
            .status(EquipmentStatus.ACTIVE)
            .purchaseDate(purchaseDate)
            .purchaseValue(purchaseValue)
            .location(location)
            .assignedTo(assignedTo != null ? assignedTo.trim() : "")
            .createdAt(now)
            .updatedAt(now)
            .createdBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .lastModifiedBy(createdBy != null ? createdBy.trim() : "SYSTEM")
            .build();
        
        return equipment;
    }
    
    public void markForMaintenance() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException(
                "Cannot mark retired equipment for maintenance. Equipment: " + this.name);
        }
        if (this.status != EquipmentStatus.MAINTENANCE) {
            this.status = EquipmentStatus.MAINTENANCE;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void markAsActive() {
        this.status = EquipmentStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsInactive() {
        if (this.status == EquipmentStatus.RETIRED) {
            throw new IllegalStateException(
                "Cannot mark retired equipment as inactive. Equipment: " + this.name);
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
        return ChronoUnit.DAYS.between(this.purchaseDate, LocalDate.now());
    }
    
    public double getYearsOwned() {
        long days = getDaysOwnedCount();
        return days / 365.0;
    }
    
    public boolean isOld() {
        return getYearsOwned() > 3;
    }
    
    public boolean isVeryOld() {
        return getYearsOwned() > 5;
    }
    
    public void updateLocation(Location newLocation) {
        if (newLocation == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reassignTo(String newAssignee) {
        this.assignedTo = newAssignee != null ? newAssignee.trim() : "";
        this.updatedAt = LocalDateTime.now();
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
