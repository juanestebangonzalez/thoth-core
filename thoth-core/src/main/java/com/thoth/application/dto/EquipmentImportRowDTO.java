package com.thoth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila de la plantilla de importacion masiva de equipos.
 * Todos los campos son Object para tolerar texto, numeros o null desde el JSON;
 * el servicio de importacion los convierte y valida.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentImportRowDTO {
    private Object rowNumber;
    private Object name;
    private Object category;
    private Object serialNumber;
    private Object inventoryNumber;
    private Object brand;
    private Object model;
    private Object macAddress;
    private Object macAddress2;
    private Object sede;
    private Object area;
    private Object costCenter;
    private Object assignedTo;
    private Object ownershipType;
    private Object purchaseDate;
    private Object purchaseValue;
    private Object rentalCompany;
    private Object rentalContractNumber;
    private Object rentalStartDate;
    private Object rentalEndDate;
    private Object rentalMonthlyValue;
    private Object processor;
    private Object ramSizeGb;
    private Object ramType;
    private Object diskType;
    private Object diskSizeGb;
    private Object lastMaintenanceDate;
    private Object lastMaintenanceType;
    private Object lastMaintenanceTechnician;
    private Object lastMaintenanceDescription;
}
