package com.thoth.application.dto;

import java.util.UUID;

public record CostCenterDTO(
    UUID id,
    String name,
    String description,
    boolean active
) {}
