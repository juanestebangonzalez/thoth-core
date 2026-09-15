package com.thoth.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalInfo {
    private String rentalCompany;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractNumber;
    private String contractFileUrl;
    private String notes;

    public boolean isContractExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean isContractExpiringSoon() {
        if (endDate == null) return false;
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= 30;
    }

    public Long getDaysUntilExpiry() {
        if (endDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
}