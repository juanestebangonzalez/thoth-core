package com.thoth.adapter.in.rest.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de auditoría para la API REST.
 * Evita exponer la entidad JPA directamente al cliente (DT-11).
 */
public record AuditLogDTO(
    UUID id,
    String action,
    String module,
    String entityId,
    String entityName,
    String details,
    String performedBy,
    LocalDateTime performedAt,
    String ipAddress
) {}
