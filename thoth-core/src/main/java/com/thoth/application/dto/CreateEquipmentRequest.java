package com.thoth.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEquipmentRequest(
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
