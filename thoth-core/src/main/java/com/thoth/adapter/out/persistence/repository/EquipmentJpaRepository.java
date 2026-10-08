package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.Hardware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentJpaRepository extends JpaRepository<EquipmentEntity, UUID> {

    Optional<EquipmentEntity> findBySerialNumber(String serialNumber);

    List<EquipmentEntity> findByStatus(EquipmentStatus status);

    List<EquipmentEntity> findByCategory(String category);

    Page<EquipmentEntity> findByStatus(EquipmentStatus status, Pageable pageable);

    Page<EquipmentEntity> findByLocationBuilding(String building, Pageable pageable);

    Page<EquipmentEntity> findByAssignedTo(String assignedTo, Pageable pageable);

    Page<EquipmentEntity> findAll(Pageable pageable);

    long countByStatus(EquipmentStatus status);

    long countByCategory(String category);

    Optional<EquipmentEntity> findByInventoryNumber(String inventoryNumber);

    /** Monitores asociados a un equipo. */
    List<EquipmentEntity> findByAssociatedEquipmentId(UUID associatedEquipmentId);

    // DT-19: Alertas por consulta en DB en lugar de cargar toda la tabla
    @Query("SELECT e FROM EquipmentEntity e WHERE e.nextMaintenanceDate IS NOT NULL " +
           "AND e.nextMaintenanceDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.MAINTENANCE " +
           "ORDER BY e.nextMaintenanceDate ASC")
    List<EquipmentEntity> findUpcomingMaintenance(@Param("limit") LocalDate limit);

    /**
     * Equipos (no retirados) con alguna alerta de hardware: salud del disco por debajo de
     * {@code saludAdvertencia}, temperatura por encima de {@code temperaturaAdvertencia} o RAM menor a 4 GB.
     */
    @Query("SELECT e FROM EquipmentEntity e WHERE e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND ((e.hardwareDiskHealthPercent IS NOT NULL AND e.hardwareDiskHealthPercent < :saludAdvertencia) " +
           "OR (e.hardwareDiskTemperatureCelsius IS NOT NULL AND e.hardwareDiskTemperatureCelsius > :temperaturaAdvertencia) " +
           "OR (e.hardwareRamSizeGb IS NOT NULL AND e.hardwareRamSizeGb < 4))")
    List<EquipmentEntity> findHardwareAlerts(@Param("saludAdvertencia") int saludAdvertencia,
                                             @Param("temperaturaAdvertencia") int temperaturaAdvertencia);

    /** Alertas de hardware con los umbrales centralizados en {@link Hardware} (desde ADVERTENCIA). */
    default List<EquipmentEntity> findHardwareCritical() {
        return findHardwareAlerts(Hardware.DISK_HEALTH_WARNING, Hardware.DISK_TEMP_WARNING);
    }

    @Query("SELECT e FROM EquipmentEntity e WHERE e.rentalEndDate IS NOT NULL " +
           "AND e.rentalEndDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "ORDER BY e.rentalEndDate ASC")
    List<EquipmentEntity> findRentalExpiring(@Param("limit") LocalDate limit);

    // DT-19: Conteos para el resumen sin cargar entidades
    @Query("SELECT COUNT(e) FROM EquipmentEntity e WHERE e.nextMaintenanceDate IS NOT NULL " +
           "AND e.nextMaintenanceDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.MAINTENANCE")
    long countUpcomingMaintenance(@Param("limit") LocalDate limit);

    @Query("SELECT COUNT(e) FROM EquipmentEntity e WHERE e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND ((e.hardwareDiskHealthPercent IS NOT NULL AND e.hardwareDiskHealthPercent < :saludAdvertencia) " +
           "OR (e.hardwareDiskTemperatureCelsius IS NOT NULL AND e.hardwareDiskTemperatureCelsius > :temperaturaAdvertencia) " +
           "OR (e.hardwareRamSizeGb IS NOT NULL AND e.hardwareRamSizeGb < 4))")
    long countHardwareAlerts(@Param("saludAdvertencia") int saludAdvertencia,
                             @Param("temperaturaAdvertencia") int temperaturaAdvertencia);

    default long countHardwareCritical() {
        return countHardwareAlerts(Hardware.DISK_HEALTH_WARNING, Hardware.DISK_TEMP_WARNING);
    }

    @Query("SELECT COUNT(e) FROM EquipmentEntity e WHERE e.rentalEndDate IS NOT NULL " +
           "AND e.rentalEndDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED")
    long countRentalExpiring(@Param("limit") LocalDate limit);

    // DT-19/DT-32: Query para el job programado — equipos con mantenimiento vencido o del día
    @Query("SELECT e FROM EquipmentEntity e WHERE e.nextMaintenanceDate IS NOT NULL " +
           "AND e.nextMaintenanceDate <= :today " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.MAINTENANCE")
    List<EquipmentEntity> findDueForMaintenance(@Param("today") LocalDate today);
}
