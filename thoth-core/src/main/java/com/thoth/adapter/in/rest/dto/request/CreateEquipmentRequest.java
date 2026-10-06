package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "El nombre del equipo es obligatorio")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "La categoria es obligatoria")
    @Size(max = 200)
    private String category;

    @Size(max = 200)
    private String serialNumber;

    @Size(max = 200)
    private String brand;

    private String model;
    private String inventoryNumber;
    private String macAddress;
    /** Segunda MAC (WiFi) para portatiles. Opcional. */
    private String macAddress2;

    /** Ubicacion opcional: building = sede, office = area. */
    @Valid
    private LocationRequest location;

    private LocalDate purchaseDate;

    private BigDecimal purchaseValue;

    private String assignedTo;
    /** Centro de costo (nombre del catalogo). Opcional. */
    @Size(max = 100)
    private String costCenter;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}