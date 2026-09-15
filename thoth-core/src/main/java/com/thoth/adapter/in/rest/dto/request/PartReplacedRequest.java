package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartReplacedRequest {
    @NotBlank(message = "Part name is required")
    private String partName;

    private String partSerialNumber;
    private String reason;
}