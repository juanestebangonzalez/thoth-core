package com.thoth.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentDTO(
    UUID equipmentId,
    String name,
    String category,
    String serialNumber,
    String inventoryNumber,
    String macAddress,
    String brand,
    String model,
    String status,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    LocationDTO location,
    String assignedTo,
    String createdBy,
    String ownershipType,
    RentalInfoDTO rentalInfo,
    HardwareDTO hardware,
    LocalDate nextMaintenanceDate,
    String macAddress2,
    String costCenter,
    String operatingSystem,
    String osVersion
) {
    /** Constructor de compatibilidad (sin sistema operativo). */
    public EquipmentDTO(UUID equipmentId, String name, String category, String serialNumber,
                        String inventoryNumber, String macAddress, String brand, String model,
                        String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                        LocationDTO location, String assignedTo, String createdBy,
                        String ownershipType, RentalInfoDTO rentalInfo, HardwareDTO hardware,
                        LocalDate nextMaintenanceDate, String macAddress2, String costCenter) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdBy, ownershipType,
             rentalInfo, hardware, nextMaintenanceDate, macAddress2, costCenter, null, null);
    }

    /** Constructor de compatibilidad (sin centro de costo). */
    public EquipmentDTO(UUID equipmentId, String name, String category, String serialNumber,
                        String inventoryNumber, String macAddress, String brand, String model,
                        String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                        LocationDTO location, String assignedTo, String createdBy,
                        String ownershipType, RentalInfoDTO rentalInfo, HardwareDTO hardware,
                        LocalDate nextMaintenanceDate, String macAddress2) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdBy, ownershipType,
             rentalInfo, hardware, nextMaintenanceDate, macAddress2, null, null, null);
    }

    /** Constructor de compatibilidad (sin segunda MAC). */
    public EquipmentDTO(UUID equipmentId, String name, String category, String serialNumber,
                        String inventoryNumber, String macAddress, String brand, String model,
                        String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                        LocationDTO location, String assignedTo, String createdBy,
                        String ownershipType, RentalInfoDTO rentalInfo, HardwareDTO hardware,
                        LocalDate nextMaintenanceDate) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdBy, ownershipType,
             rentalInfo, hardware, nextMaintenanceDate, null, null, null, null);
    }
}