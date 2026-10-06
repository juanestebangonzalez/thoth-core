package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
    private String macAddress2;
    private String assignedTo;
    /** Centro de costo (nombre del catalogo). null = sin cambio, "" = limpiar. */
    @Size(max = 100)
    private String costCenter;
    @Valid
    private LocationRequest location;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}