package com.thoth.application.dto;

import java.util.UUID;

public record SedeDTO(
    UUID id,
    String name,
    String address,
    String phone,
    boolean active
) {}
