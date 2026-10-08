package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    /** Sistema operativo: WINDOWS, LINUX, MACOS, CHROMEOS, ANDROID, IOS, OTRO, N/A (o vacio). */
    @Pattern(regexp = "(?i)^\\s*(WINDOWS|LINUX|MACOS|CHROMEOS|ANDROID|IOS|OTRO|N/A)?\\s*$",
             message = "Sistema operativo invalido. Valores permitidos: WINDOWS, LINUX, MACOS, CHROMEOS, ANDROID, IOS, OTRO, N/A")
    private String operatingSystem;
    /** Version del sistema operativo (texto libre). */
    @Size(max = 100, message = "La version del sistema operativo no puede superar 100 caracteres")
    private String osVersion;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}