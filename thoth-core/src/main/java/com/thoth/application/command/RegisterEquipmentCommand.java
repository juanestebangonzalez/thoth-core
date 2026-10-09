package com.thoth.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterEquipmentCommand(
    String name,
    String category,
    String serialNumber,
    String inventoryNumber,
    String brand,
    String model,
    String macAddress,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    String building,
    String floor,
    String office,
    String assignedTo,
    String createdBy,
    String ownershipType,
    String rentalCompany,
    String rentalContactName,
    String rentalContactPhone,
    String rentalContactEmail,
    LocalDate rentalStartDate,
    LocalDate rentalEndDate,
    String rentalContractNumber,
    String rentalContractFileUrl,
    String rentalNotes,
    String processor,
    Integer ramSizeGb,
    String ramType,
    String diskType,
    Integer diskSizeGb,
    Integer diskHealthPercent,
    Integer diskTemperatureCelsius,
    String macAddress2,
    String costCenter,
    BigDecimal rentalMonthlyValue,
    String operatingSystem,
    String osVersion,
    String responsiblePosition,
    String responsibleDocument,
    String responsiblePhone,
    String responsibleEmail,
    String ipAddress,
    String ipAssignment,
    UUID associatedEquipmentId,
    String osEdition,
    String osLicenseType
) {
    /** Constructor de compatibilidad (sin licenciamiento del sistema operativo). */
    public RegisterEquipmentCommand(String name, String category, String serialNumber, String inventoryNumber, String brand, String model, String macAddress, LocalDate purchaseDate, BigDecimal purchaseValue, String building, String floor, String office, String assignedTo, String createdBy, String ownershipType, String rentalCompany, String rentalContactName, String rentalContactPhone, String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate, String rentalContractNumber, String rentalContractFileUrl, String rentalNotes, String processor, Integer ramSizeGb, String ramType, String diskType, Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius, String macAddress2, String costCenter, BigDecimal rentalMonthlyValue, String operatingSystem, String osVersion, String responsiblePosition, String responsibleDocument, String responsiblePhone, String responsibleEmail, String ipAddress, String ipAssignment, UUID associatedEquipmentId) {
        this(name, category, serialNumber, inventoryNumber, brand, model, macAddress, purchaseDate, purchaseValue, building, floor, office, assignedTo, createdBy, ownershipType, rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius, macAddress2, costCenter, rentalMonthlyValue, operatingSystem, osVersion, responsiblePosition, responsibleDocument, responsiblePhone, responsibleEmail, ipAddress, ipAssignment, associatedEquipmentId,
             null, null);
    }

    /** Constructor de compatibilidad (sin responsable, red ni monitor asociado). */
    public RegisterEquipmentCommand(String name, String category, String serialNumber, String inventoryNumber, String brand, String model, String macAddress, LocalDate purchaseDate, BigDecimal purchaseValue, String building, String floor, String office, String assignedTo, String createdBy, String ownershipType, String rentalCompany, String rentalContactName, String rentalContactPhone, String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate, String rentalContractNumber, String rentalContractFileUrl, String rentalNotes, String processor, Integer ramSizeGb, String ramType, String diskType, Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius, String macAddress2, String costCenter, BigDecimal rentalMonthlyValue, String operatingSystem, String osVersion) {
        this(name, category, serialNumber, inventoryNumber, brand, model, macAddress, purchaseDate, purchaseValue, building, floor, office, assignedTo, createdBy, ownershipType, rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius, macAddress2, costCenter, rentalMonthlyValue, operatingSystem, osVersion,
             null, null, null, null, null, null, null);
    }

    /** Constructor de compatibilidad (sin sistema operativo). */
    public RegisterEquipmentCommand(String name, String category, String serialNumber, String inventoryNumber,
                                    String brand, String model, String macAddress, LocalDate purchaseDate,
                                    BigDecimal purchaseValue, String building, String floor, String office,
                                    String assignedTo, String createdBy, String ownershipType,
                                    String rentalCompany, String rentalContactName, String rentalContactPhone,
                                    String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                    String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                    String processor, Integer ramSizeGb, String ramType, String diskType,
                                    Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius,
                                    String macAddress2, String costCenter, BigDecimal rentalMonthlyValue) {
        this(name, category, serialNumber, inventoryNumber, brand, model, macAddress, purchaseDate,
             purchaseValue, building, floor, office, assignedTo, createdBy, ownershipType,
             rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate,
             rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor,
             ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius,
             macAddress2, costCenter, rentalMonthlyValue, null, null);
    }

    /** Constructor de compatibilidad (sin centro de costo ni valor mensual de alquiler). */
    public RegisterEquipmentCommand(String name, String category, String serialNumber, String inventoryNumber,
                                    String brand, String model, String macAddress, LocalDate purchaseDate,
                                    BigDecimal purchaseValue, String building, String floor, String office,
                                    String assignedTo, String createdBy, String ownershipType,
                                    String rentalCompany, String rentalContactName, String rentalContactPhone,
                                    String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                    String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                    String processor, Integer ramSizeGb, String ramType, String diskType,
                                    Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius,
                                    String macAddress2) {
        this(name, category, serialNumber, inventoryNumber, brand, model, macAddress, purchaseDate,
             purchaseValue, building, floor, office, assignedTo, createdBy, ownershipType,
             rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate,
             rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor,
             ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius,
             macAddress2, null, null, null, null);
    }

    /** Constructor de compatibilidad (sin segunda MAC). */
    public RegisterEquipmentCommand(String name, String category, String serialNumber, String inventoryNumber,
                                    String brand, String model, String macAddress, LocalDate purchaseDate,
                                    BigDecimal purchaseValue, String building, String floor, String office,
                                    String assignedTo, String createdBy, String ownershipType,
                                    String rentalCompany, String rentalContactName, String rentalContactPhone,
                                    String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                    String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                    String processor, Integer ramSizeGb, String ramType, String diskType,
                                    Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius) {
        this(name, category, serialNumber, inventoryNumber, brand, model, macAddress, purchaseDate,
             purchaseValue, building, floor, office, assignedTo, createdBy, ownershipType,
             rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate,
             rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor,
             ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius,
             null, null, null, null, null);
    }
}