package com.thoth.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartReplaced {
    private String partName;
    private String partSerialNumber;
    private String reason;
    private LocalDate purchaseDate;
    private String ticketNumber;
}