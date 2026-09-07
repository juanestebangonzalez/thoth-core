package com.thoth.application.dto;

public record UpdateEquipmentRequest(
    String name,
    String assignedTo,
    String building,
    String floor,
    String office
) {}
