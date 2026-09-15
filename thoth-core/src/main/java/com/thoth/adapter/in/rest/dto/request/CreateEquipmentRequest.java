package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Brand is required")
    private String brand;

    private String model;
    private String inventoryNumber;
    private String macAddress;

    @NotNull(message = "Location is required")
    @Valid
    private LocationRequest location;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotNull(message = "Purchase value is required")
    private BigDecimal purchaseValue;

    private String assignedTo;
    private String ownershipType;
    private RentalInfoRequest rentalInfo;
    private HardwareRequest hardware;
}