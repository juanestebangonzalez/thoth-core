package com.thoth.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Monitor asociado a un equipo (los monitores son equipos normales: propios o alquilados). */
public record AssociatedMonitorDTO(
    UUID equipmentId,
    String name,
    String inventoryNumber,
    String brand,
    String model,
    String serialNumber,
    String status,
    String ownershipType,
    String rentalCompany,
    BigDecimal monthlyValue
) {}
