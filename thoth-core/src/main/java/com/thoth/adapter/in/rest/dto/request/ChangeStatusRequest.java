package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;
    
    @Size(max = 500)
    private String reason;
}