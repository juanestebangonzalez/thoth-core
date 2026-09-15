package com.thoth.adapter.in.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalInfoRequest {
    private String rentalCompany;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractNumber;
    private String contractFileUrl;
    private String notes;
}