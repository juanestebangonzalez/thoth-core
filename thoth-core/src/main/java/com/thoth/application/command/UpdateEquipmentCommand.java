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
    Integer diskTemperatureCelsius
) {}