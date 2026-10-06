package com.thoth.application.dto;

import java.util.List;

/** Resultado global de la importacion masiva de equipos. */
public record EquipmentImportResultDTO(
    boolean dryRun,
    int total,
    int validos,
    int conErrores,
    int importados,
    List<EquipmentImportRowResultDTO> rows
) {}
