package com.thoth.application.dto;

import java.time.LocalDate;

public record PartReplacedDTO(
    String partName,
    String partSerialNumber,
    String reason,
    LocalDate purchaseDate,
    String ticketNumber
) {}
