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
    // ---- Responsable ----
    /** Cargo del responsable (max. 100). */
    @Size(max = 100, message = "El cargo del responsable no puede superar 100 caracteres")
    private String responsiblePosition;
    /** Documento del responsable (max. 30). */
    @Size(max = 30, message = "El documento del responsable no puede superar 30 caracteres")
    private String responsibleDocument;
    /** Celular del responsable: exactamente 10 digitos. */
    @Pattern(regexp = "^\\s*(\\d{10})?\\s*$",
             message = "El celular del responsable debe tener exactamente 10 digitos (solo numeros)")
    private String responsiblePhone;
    /** Correo del responsable (max. 150). */
    @Size(max = 150, message = "El correo del responsable no puede superar 150 caracteres")
    @Pattern(regexp = "^\\s*([A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,})?\\s*$",
             message = "El correo del responsable no tiene un formato valido")
    private String responsibleEmail;

    // ---- Red ----
    /** Direccion IPv4. */
    @Pattern(regexp = "^\\s*(((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d))?\\s*$",
             message = "La direccion IP no es una IPv4 valida (ej: 192.168.1.10)")
    private String ipAddress;
    /** Asignacion de la IP: DHCP | FIJA. */
    @Pattern(regexp = "(?i)^\\s*(DHCP|FIJA)?\\s*$", message = "La asignacion de IP no es valida (use DHCP o FIJA)")
    private String ipAssignment;

    // ---- Monitor asociado ----
    /** UUID del equipo al que se asocia este monitor. Vacio = sin asociar. */
    private String associatedEquipmentId;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}