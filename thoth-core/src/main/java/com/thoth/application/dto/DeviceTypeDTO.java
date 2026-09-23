package com.thoth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceTypeDTO(
    UUID id,
    String name,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
