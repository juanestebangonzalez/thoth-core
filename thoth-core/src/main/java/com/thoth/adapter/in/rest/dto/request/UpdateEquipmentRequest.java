package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentRequest {
    @NotBlank(message = "Equipment name is required")
    private String name;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    private String model;
    private String macAddress;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private String assignedTo;
}