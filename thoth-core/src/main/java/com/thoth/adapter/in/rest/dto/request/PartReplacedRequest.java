package com.thoth.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartReplacedRequest {
    @NotBlank(message = "Part name is required")
    private String partName;

    private String partSerialNumber;
    private String reason;
    private LocalDate purchaseDate;

    @Size(max = 100)
    private String ticketNumber;
}