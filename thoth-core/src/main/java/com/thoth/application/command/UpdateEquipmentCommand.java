package com.thoth.application.command;

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
    String macAddress2
) {
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
             diskTemperatureCelsius, null);
    }
}