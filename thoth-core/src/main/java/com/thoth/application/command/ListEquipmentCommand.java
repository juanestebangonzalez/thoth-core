package com.thoth.application.command;

public record ListEquipmentCommand(
    int pageNumber,
    int pageSize,
    String sortBy,
    String status,
    String category
) {}
