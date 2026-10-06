package com.thoth.application.dto;

import java.util.List;

/** Resultado por fila de la importacion masiva. status: OK | ERROR | IMPORTADO. */
public record EquipmentImportRowResultDTO(
    Integer rowNumber,
    String name,
    String status,
    List<String> errors,
    List<String> warnings,
    String nextMaintenanceDate
) {}
