package com.thoth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Periferico de un equipo. */
public record PeripheralDTO(
    UUID id,
    String type,
    String brand,
    LocalDateTime createdAt,
    String createdBy
) {}
