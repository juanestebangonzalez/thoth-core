package com.thoth.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateEquipmentCommand(
    UUID equipmentId,
    String name,
    String inventoryNumber,
    String assignedTo,
    String building,
    String floor,
    String office,
    String modifiedBy,
    String brand,
    String model,
    String macAddress,
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
    String associatedEquipmentId,
    String osEdition,
    String osLicenseType
) {
    /** Constructor de compatibilidad (sin licenciamiento del sistema operativo). */
    public UpdateEquipmentCommand(UUID equipmentId, String name, String inventoryNumber, String assignedTo, String building, String floor, String office, String modifiedBy, String brand, String model, String macAddress, String ownershipType, String rentalCompany, String rentalContactName, String rentalContactPhone, String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate, String rentalContractNumber, String rentalContractFileUrl, String rentalNotes, String processor, Integer ramSizeGb, String ramType, String diskType, Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius, String macAddress2, String costCenter, BigDecimal rentalMonthlyValue, String operatingSystem, String osVersion, String responsiblePosition, String responsibleDocument, String responsiblePhone, String responsibleEmail, String ipAddress, String ipAssignment, String associatedEquipmentId) {
        this(equipmentId, name, inventoryNumber, assignedTo, building, floor, office, modifiedBy, brand, model, macAddress, ownershipType, rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius, macAddress2, costCenter, rentalMonthlyValue, operatingSystem, osVersion, responsiblePosition, responsibleDocument, responsiblePhone, responsibleEmail, ipAddress, ipAssignment, associatedEquipmentId,
             null, null);
    }

    /** Constructor de compatibilidad (sin responsable, red ni monitor asociado). */
    public UpdateEquipmentCommand(UUID equipmentId, String name, String inventoryNumber, String assignedTo, String building, String floor, String office, String modifiedBy, String brand, String model, String macAddress, String ownershipType, String rentalCompany, String rentalContactName, String rentalContactPhone, String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate, String rentalContractNumber, String rentalContractFileUrl, String rentalNotes, String processor, Integer ramSizeGb, String ramType, String diskType, Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius, String macAddress2, String costCenter, BigDecimal rentalMonthlyValue, String operatingSystem, String osVersion) {
        this(equipmentId, name, inventoryNumber, assignedTo, building, floor, office, modifiedBy, brand, model, macAddress, ownershipType, rentalCompany, rentalContactName, rentalContactPhone, rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl, rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius, macAddress2, costCenter, rentalMonthlyValue, operatingSystem, osVersion,
             null, null, null, null, null, null, null);
    }

    /** Constructor de compatibilidad (sin sistema operativo). */
    public UpdateEquipmentCommand(UUID equipmentId, String name, String inventoryNumber, String assignedTo,
                                  String building, String floor, String office, String modifiedBy,
                                  String brand, String model, String macAddress, String ownershipType,
                                  String rentalCompany, String rentalContactName, String rentalContactPhone,
                                  String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                  String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                  String processor, Integer ramSizeGb, String ramType, String diskType,
                                  Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius,
                                  String macAddress2, String costCenter, BigDecimal rentalMonthlyValue) {
        this(equipmentId, name, inventoryNumber, assignedTo, building, floor, office, modifiedBy,
             brand, model, macAddress, ownershipType, rentalCompany, rentalContactName, rentalContactPhone,
             rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl,
             rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent,
             diskTemperatureCelsius, macAddress2, costCenter, rentalMonthlyValue, null, null);
    }

    /** Constructor de compatibilidad (sin centro de costo ni valor mensual de alquiler). */
    public UpdateEquipmentCommand(UUID equipmentId, String name, String inventoryNumber, String assignedTo,
                                  String building, String floor, String office, String modifiedBy,
                                  String brand, String model, String macAddress, String ownershipType,
                                  String rentalCompany, String rentalContactName, String rentalContactPhone,
                                  String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                  String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                  String processor, Integer ramSizeGb, String ramType, String diskType,
                                  Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius,
                                  String macAddress2) {
        this(equipmentId, name, inventoryNumber, assignedTo, building, floor, office, modifiedBy,
             brand, model, macAddress, ownershipType, rentalCompany, rentalContactName, rentalContactPhone,
             rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl,
             rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent,
             diskTemperatureCelsius, macAddress2, null, null, null, null);
    }

    /** Constructor de compatibilidad (sin segunda MAC). */
    public UpdateEquipmentCommand(UUID equipmentId, String name, String inventoryNumber, String assignedTo,
                                  String building, String floor, String office, String modifiedBy,
                                  String brand, String model, String macAddress, String ownershipType,
                                  String rentalCompany, String rentalContactName, String rentalContactPhone,
                                  String rentalContactEmail, LocalDate rentalStartDate, LocalDate rentalEndDate,
                                  String rentalContractNumber, String rentalContractFileUrl, String rentalNotes,
                                  String processor, Integer ramSizeGb, String ramType, String diskType,
                                  Integer diskSizeGb, Integer diskHealthPercent, Integer diskTemperatureCelsius) {
        this(equipmentId, name, inventoryNumber, assignedTo, building, floor, office, modifiedBy,
             brand, model, macAddress, ownershipType, rentalCompany, rentalContactName, rentalContactPhone,
             rentalContactEmail, rentalStartDate, rentalEndDate, rentalContractNumber, rentalContractFileUrl,
             rentalNotes, processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent,
             diskTemperatureCelsius, null, null, null, null, null);
    }
}