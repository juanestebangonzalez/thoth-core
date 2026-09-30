package com.thoth.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    String macAddress2
) {
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
             ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius, null);
    }
}