package com.thoth.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EquipmentResponseDTO(
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
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String ownershipType,
    RentalInfoDTO rentalInfo,
    HardwareDTO hardware,
    LocalDate nextMaintenanceDate,
    String macAddress2,
    String costCenter,
    String operatingSystem,
    String osVersion,
    String responsiblePosition,
    String responsibleDocument,
    String responsiblePhone,
    String responsibleEmail,
    String ipAddress,
    String ipAssignment,
    UUID associatedEquipmentId,
    String associatedEquipmentName,
    String associatedEquipmentInventory,
    UsefulLifeDTO usefulLife,
    String criticality,
    String osEdition,
    String osLicenseType
) {
    /** Constructor de compatibilidad (sin licenciamiento del sistema operativo). */
    public EquipmentResponseDTO(UUID equipmentId, String name, String category, String serialNumber, String inventoryNumber, String macAddress, String brand, String model, String status, LocalDate purchaseDate, BigDecimal purchaseValue, LocationDTO location, String assignedTo, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String ownershipType, RentalInfoDTO rentalInfo, HardwareDTO hardware, LocalDate nextMaintenanceDate, String macAddress2, String costCenter, String operatingSystem, String osVersion, String responsiblePosition, String responsibleDocument, String responsiblePhone, String responsibleEmail, String ipAddress, String ipAssignment, UUID associatedEquipmentId, String associatedEquipmentName, String associatedEquipmentInventory, UsefulLifeDTO usefulLife, String criticality) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model, status, purchaseDate, purchaseValue, location, assignedTo, createdAt, updatedAt, createdBy, ownershipType, rentalInfo, hardware, nextMaintenanceDate, macAddress2, costCenter, operatingSystem, osVersion, responsiblePosition, responsibleDocument, responsiblePhone, responsibleEmail, ipAddress, ipAssignment, associatedEquipmentId, associatedEquipmentName, associatedEquipmentInventory, usefulLife, criticality,
             null, null);
    }

    /** Constructor de compatibilidad (sin responsable, red, monitor asociado ni calculados). */
    public EquipmentResponseDTO(UUID equipmentId, String name, String category, String serialNumber,
                                String inventoryNumber, String macAddress, String brand, String model,
                                String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                                LocationDTO location, String assignedTo, LocalDateTime createdAt,
                                LocalDateTime updatedAt, String createdBy, String ownershipType,
                                RentalInfoDTO rentalInfo, HardwareDTO hardware,
                                LocalDate nextMaintenanceDate, String macAddress2, String costCenter,
                                String operatingSystem, String osVersion) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdAt, updatedAt,
             createdBy, ownershipType, rentalInfo, hardware, nextMaintenanceDate, macAddress2, costCenter,
             operatingSystem, osVersion, null, null, null, null, null, null, null, null, null, null, null);
    }

    /** Constructor de compatibilidad (sin sistema operativo). */
    public EquipmentResponseDTO(UUID equipmentId, String name, String category, String serialNumber,
                                String inventoryNumber, String macAddress, String brand, String model,
                                String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                                LocationDTO location, String assignedTo, LocalDateTime createdAt,
                                LocalDateTime updatedAt, String createdBy, String ownershipType,
                                RentalInfoDTO rentalInfo, HardwareDTO hardware,
                                LocalDate nextMaintenanceDate, String macAddress2, String costCenter) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdAt, updatedAt,
             createdBy, ownershipType, rentalInfo, hardware, nextMaintenanceDate, macAddress2, costCenter,
             null, null);
    }

    /** Constructor de compatibilidad (sin centro de costo). */
    public EquipmentResponseDTO(UUID equipmentId, String name, String category, String serialNumber,
                                String inventoryNumber, String macAddress, String brand, String model,
                                String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                                LocationDTO location, String assignedTo, LocalDateTime createdAt,
                                LocalDateTime updatedAt, String createdBy, String ownershipType,
                                RentalInfoDTO rentalInfo, HardwareDTO hardware,
                                LocalDate nextMaintenanceDate, String macAddress2) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdAt, updatedAt,
             createdBy, ownershipType, rentalInfo, hardware, nextMaintenanceDate, macAddress2, null, null, null);
    }

    /** Constructor de compatibilidad (sin segunda MAC). */
    public EquipmentResponseDTO(UUID equipmentId, String name, String category, String serialNumber,
                                String inventoryNumber, String macAddress, String brand, String model,
                                String status, LocalDate purchaseDate, BigDecimal purchaseValue,
                                LocationDTO location, String assignedTo, LocalDateTime createdAt,
                                LocalDateTime updatedAt, String createdBy, String ownershipType,
                                RentalInfoDTO rentalInfo, HardwareDTO hardware,
                                LocalDate nextMaintenanceDate) {
        this(equipmentId, name, category, serialNumber, inventoryNumber, macAddress, brand, model,
             status, purchaseDate, purchaseValue, location, assignedTo, createdAt, updatedAt,
             createdBy, ownershipType, rentalInfo, hardware, nextMaintenanceDate, null, null, null, null);
    }
}