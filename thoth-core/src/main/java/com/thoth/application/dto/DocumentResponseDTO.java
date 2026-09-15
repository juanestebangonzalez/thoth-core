package com.thoth.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponseDTO(
    UUID id,
    UUID equipmentId,
    String originalName,
    String contentType,
    Long fileSize,
    String documentType,
    String description,
    String uploadedBy,
    LocalDateTime uploadedAt
) {}
