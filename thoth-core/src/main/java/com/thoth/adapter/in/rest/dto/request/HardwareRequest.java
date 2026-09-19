package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HardwareRequest {
    @Size(max = 200)
    private String processor;
    @Min(0)
    @Max(1024)
    private Integer ramSizeGb;
    @Size(max = 50)
    private String ramType;
    @Size(max = 50)
    private String diskType;
    @Min(0)
    @Max(100000)
    private Integer diskSizeGb;
    @Min(0)
    @Max(100)
    private Integer diskHealthPercent;
    @Min(-50)
    @Max(200)
    private Integer diskTemperatureCelsius;
}