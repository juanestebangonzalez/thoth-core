package com.thoth.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterEquipmentCommand(
    String name,
    String category,
    String serialNumber,
    String brand,
    String model,
    String macAddress,
    LocalDate purchaseDate,
    BigDecimal purchaseValue,
    String building,
    String floor,
    String office,
    String assignedTo,
    String createdBy
) {}
