package com.thoth.application.dto;

public record PartReplacedDTO(
    String partName,
    String partSerialNumber,
    String reason
) {}