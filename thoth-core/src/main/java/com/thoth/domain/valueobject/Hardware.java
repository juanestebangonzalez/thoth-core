package com.thoth.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hardware {
    private String processor;
    private Integer ramSizeGb;
    private RamType ramType;
    private DiskType diskType;
    private Integer diskSizeGb;
    private Integer diskHealthPercent;
    private Integer diskTemperatureCelsius;

    public boolean hasCriticalDiskHealth() {
        return diskHealthPercent != null && diskHealthPercent < 30;
    }

    public boolean hasWarningDiskHealth() {
        return diskHealthPercent != null && diskHealthPercent < 50;
    }

    public boolean hasCriticalTemperature() {
        return diskTemperatureCelsius != null && diskTemperatureCelsius >= 70;
    }

    public boolean hasWarningTemperature() {
        return diskTemperatureCelsius != null && diskTemperatureCelsius >= 60;
    }
}