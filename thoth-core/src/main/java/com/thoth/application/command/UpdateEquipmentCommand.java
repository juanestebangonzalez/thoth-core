package com.thoth.application.command;

import java.util.UUID;

public record UpdateEquipmentCommand(
    UUID equipmentId,
    String name,
    String assignedTo,
    String building,
    String floor,
    String office,
    String modifiedBy
) {}
