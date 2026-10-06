package com.thoth.application.dto;

import java.math.BigDecimal;
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
    Boolean isExpiringSoon,
    BigDecimal monthlyValue
) {
    /** Constructor de compatibilidad (sin valor mensual). */
    public RentalInfoDTO(String rentalCompany, String contactName, String contactPhone, String contactEmail,
                         LocalDate startDate, LocalDate endDate, String contractNumber, String contractFileUrl,
                         String notes, Long daysUntilExpiry, Boolean isExpired, Boolean isExpiringSoon) {
        this(rentalCompany, contactName, contactPhone, contactEmail, startDate, endDate, contractNumber,
             contractFileUrl, notes, daysUntilExpiry, isExpired, isExpiringSoon, null);
    }
}