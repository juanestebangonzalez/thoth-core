package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentRequest {
    private String name;
    private String brand;
    private String model;
    private String inventoryNumber;
    private String macAddress;
    private String assignedTo;
    @Valid
    private LocationRequest location;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}