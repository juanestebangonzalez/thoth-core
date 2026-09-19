package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
    @Size(max = 200)
    private String rentalCompany;
    @Size(max = 200)
    private String contactName;
    @Size(max = 30)
    private String contactPhone;
    @Email
    @Size(max = 200)
    private String contactEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    @Size(max = 100)
    private String contractNumber;
    @Size(max = 500)
    private String contractFileUrl;
    @Size(max = 2000)
    private String notes;
}