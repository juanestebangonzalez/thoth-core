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
    BigDecimal rentalMonthlyValue
) {
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
             diskTemperatureCelsius, macAddress2, null, null);
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
             diskTemperatureCelsius, null, null, null);
    }
}