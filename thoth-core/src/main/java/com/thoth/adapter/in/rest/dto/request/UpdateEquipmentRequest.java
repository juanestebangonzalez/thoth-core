package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
    /** Sistema operativo. null = sin cambio, "" = limpiar. */
    @Pattern(regexp = "(?i)^\\s*(WINDOWS|LINUX|MACOS|CHROMEOS|ANDROID|IOS|OTRO|N/A)?\\s*$",
             message = "Sistema operativo invalido. Valores permitidos: WINDOWS, LINUX, MACOS, CHROMEOS, ANDROID, IOS, OTRO, N/A")
    private String operatingSystem;
    /** Version del sistema operativo. null = sin cambio, "" = limpiar. */
    @Size(max = 100, message = "La version del sistema operativo no puede superar 100 caracteres")
    private String osVersion;
    @Valid
    private LocationRequest location;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}