package com.thoth.application.dto;

import java.time.LocalDate;

public record RentalInfoDTO(
    String rentalCompany,
    String contactName,
    String contactPhone,
    String contactEmail,
    LocalDate startDate,
    LocalDate endDate,
    String contractNumber,
    String contractFileUrl,
    String notes,
    Long daysUntilExpiry,
    Boolean isExpired,
    Boolean isExpiringSoon
) {}