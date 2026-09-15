package com.thoth.application.dto;

public record HardwareDTO(
    String processor,
    Integer ramSizeGb,
    String ramType,
    String diskType,
    Integer diskSizeGb,
    Integer diskHealthPercent,
    Integer diskTemperatureCelsius,
    String diskHealthStatus,
    String diskTemperatureStatus
) {}