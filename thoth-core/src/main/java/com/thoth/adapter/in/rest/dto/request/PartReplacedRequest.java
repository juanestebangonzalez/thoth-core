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
    @NotBlank(message = "El nombre de la parte es obligatorio")
    private String partName;

    private String partSerialNumber;
    private String reason;
    private LocalDate purchaseDate;

    @Size(max = 100)
    private String ticketNumber;
}