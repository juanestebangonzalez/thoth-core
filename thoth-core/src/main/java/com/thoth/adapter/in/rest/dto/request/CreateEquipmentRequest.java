package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
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
public class CreateEquipmentRequest {
    @NotBlank(message = "Equipment name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 200)
    private String category;

    @Size(max = 200)
    private String serialNumber;

    @Size(max = 200)
    private String brand;

    private String model;
    private String inventoryNumber;
    private String macAddress;

    @NotNull(message = "Location is required")
    @Valid
    private LocationRequest location;

    private LocalDate purchaseDate;

    private BigDecimal purchaseValue;

    private String assignedTo;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}