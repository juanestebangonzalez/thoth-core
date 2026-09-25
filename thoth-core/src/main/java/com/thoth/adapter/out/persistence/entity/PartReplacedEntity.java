package com.thoth.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "part_replaced")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartReplacedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "part_id")
    private UUID partId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_id", nullable = false)
    private MaintenanceHistoryEntity maintenanceHistory;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "part_serial_number")
    private String partSerialNumber;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "ticket_number", length = 100)
    private String ticketNumber;
}