package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponseDTO {
    private UUID equipmentId;
    private String name;
    private String category;
    private String serialNumber;
    private String brand;
    private String model;
    private String macAddress;
    private String status;
    private LocationDTO location;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}