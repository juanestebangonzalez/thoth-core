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
    /** Software/edicion del SO: WINDOWS 10 | WINDOWS 11 (solo con SO WINDOWS). null = sin cambio, "" = limpiar. */
    @Pattern(regexp = "(?i)^\\s*(WINDOWS\\s*10|WINDOWS\\s*11)?\\s*$",
             message = "Software invalido. Valores permitidos: WINDOWS 10, WINDOWS 11")
    private String osEdition;
    /** Tipo de licencia del SO: OEM | RETAIL | VOLUMEN. null = sin cambio, "" = limpiar. */
    @Pattern(regexp = "(?i)^\\s*(OEM|RETAIL|VOLUMEN)?\\s*$",
             message = "Tipo de licencia invalido. Valores permitidos: OEM, RETAIL, VOLUMEN")
    private String osLicenseType;
    @Valid
    private LocationRequest location;
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
    /** UUID del equipo al que se asocia este monitor. null = sin cambio, "" = desasociar. */
    private String associatedEquipmentId;
    private String ownershipType;
    @Valid
    private RentalInfoRequest rentalInfo;
    @Valid
    private HardwareRequest hardware;
}