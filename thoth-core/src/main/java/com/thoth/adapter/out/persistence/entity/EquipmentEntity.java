package com.thoth.adapter.out.persistence.entity;

import com.thoth.domain.valueobject.DiskType;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.OwnershipType;
import com.thoth.domain.valueobject.RamType;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentEntity {

    @Id
    @Column(name = "equipment_id")
    private UUID equipmentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    @Column(name = "inventory_number")
    private String inventoryNumber;

    @Column(name = "mac_address")
    private String macAddress;

    private String brand;
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_value")
    private BigDecimal purchaseValue;

    @Column(name = "location_building")
    private String locationBuilding;

    @Column(name = "location_floor")
    private String locationFloor;

    @Column(name = "location_office")
    private String locationOffice;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type")
    private OwnershipType ownershipType;

    @Column(name = "rental_company")
    private String rentalCompany;

    @Column(name = "rental_contact_name")
    private String rentalContactName;

    @Column(name = "rental_contact_phone")
    private String rentalContactPhone;

    @Column(name = "rental_contact_email")
    private String rentalContactEmail;

    @Column(name = "rental_start_date")
    private LocalDate rentalStartDate;

    @Column(name = "rental_end_date")
    private LocalDate rentalEndDate;

    @Column(name = "rental_contract_number")
    private String rentalContractNumber;

    @Column(name = "rental_contract_file_url")
    private String rentalContractFileUrl;

    @Column(name = "rental_notes", columnDefinition = "TEXT")
    private String rentalNotes;

    @Column(name = "hardware_processor")
    private String hardwareProcessor;

    @Column(name = "hardware_ram_size_gb")
    private Integer hardwareRamSizeGb;

    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_ram_type")
    private RamType hardwareRamType;

    @Enumerated(EnumType.STRING)
    @Column(name = "hardware_disk_type")
    private DiskType hardwareDiskType;

    @Column(name = "hardware_disk_size_gb")
    private Integer hardwareDiskSizeGb;

    @Column(name = "hardware_disk_health_percent")
    private Integer hardwareDiskHealthPercent;

    @Column(name = "hardware_disk_temperature_celsius")
    private Integer hardwareDiskTemperatureCelsius;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;
}