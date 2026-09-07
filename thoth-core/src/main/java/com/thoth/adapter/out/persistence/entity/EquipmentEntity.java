package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentEntity {
    
    @Id
    @Column(name = "equipment_id")
    private UUID equipmentId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "model", length = 100)
    private String model;
    
    @Column(name = "mac_address", length = 17)
    private String macAddress;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "location_building", nullable = false, length = 100)
    private String locationBuilding;
    
    @Column(name = "location_floor", length = 50)
    private String locationFloor;
    
    @Column(name = "location_office", length = 50)
    private String locationOffice;
    
    @Column(name = "assigned_to", length = 255)
    private String assignedTo;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;
    
    @Column(name = "purchase_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseValue;
    
    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", length = 255)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}