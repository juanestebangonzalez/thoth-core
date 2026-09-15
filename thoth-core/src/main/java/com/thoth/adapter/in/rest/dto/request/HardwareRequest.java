package com.thoth.adapter.in.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HardwareRequest {
    private String processor;
    private Integer ramSizeGb;
    private String ramType;
    private String diskType;
    private Integer diskSizeGb;
    private Integer diskHealthPercent;
    private Integer diskTemperatureCelsius;
}