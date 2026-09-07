package com.thoth.application.command;

import java.util.UUID;

public record ChangeStatusCommand(
    UUID equipmentId,
    String newStatus,
    String modifiedBy
) {}
