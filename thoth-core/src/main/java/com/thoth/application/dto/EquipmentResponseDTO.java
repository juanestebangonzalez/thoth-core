package com.thoth.application.dto;

import java.util.UUID;

public record EquipmentResponseDTO(
    UUID equipmentId,
    String name,
    String category,
    String status,
    String location,
    String assignedTo,
    String message
) {}
