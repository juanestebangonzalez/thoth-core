package com.thoth.adapter.out.persistence.repository;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.valueobject.EquipmentStatus;
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

    // DT-19: Alertas por consulta en DB en lugar de cargar toda la tabla
    @Query("SELECT e FROM EquipmentEntity e WHERE e.nextMaintenanceDate IS NOT NULL " +
           "AND e.nextMaintenanceDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.MAINTENANCE " +
           "ORDER BY e.nextMaintenanceDate ASC")
    List<EquipmentEntity> findUpcomingMaintenance(@Param("limit") LocalDate limit);

    @Query("SELECT e FROM EquipmentEntity e WHERE e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED " +
           "AND ((e.hardwareDiskHealthPercent IS NOT NULL AND e.hardwareDiskHealthPercent < 30) " +
           "OR (e.hardwareDiskTemperatureCelsius IS NOT NULL AND e.hardwareDiskTemperatureCelsius >= 70) " +
           "OR (e.hardwareRamSizeGb IS NOT NULL AND e.hardwareRamSizeGb < 4))")
    List<EquipmentEntity> findHardwareCritical();

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
           "AND ((e.hardwareDiskHealthPercent IS NOT NULL AND e.hardwareDiskHealthPercent < 30) " +
           "OR (e.hardwareDiskTemperatureCelsius IS NOT NULL AND e.hardwareDiskTemperatureCelsius >= 70) " +
           "OR (e.hardwareRamSizeGb IS NOT NULL AND e.hardwareRamSizeGb < 4))")
    long countHardwareCritical();

    @Query("SELECT COUNT(e) FROM EquipmentEntity e WHERE e.rentalEndDate IS NOT NULL " +
           "AND e.rentalEndDate <= :limit " +
           "AND e.status <> com.thoth.domain.valueobject.EquipmentStatus.RETIRED")
    long countRentalExpiring(@Param("limit") LocalDate limit);
}
