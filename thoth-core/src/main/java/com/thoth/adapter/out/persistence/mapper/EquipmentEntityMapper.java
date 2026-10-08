package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Hardware;
import com.thoth.domain.valueobject.Location;
import com.thoth.domain.valueobject.RentalInfo;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEntityMapper {

    public EquipmentEntity toEntity(Equipment equipment) {
        EquipmentEntity.EquipmentEntityBuilder builder = EquipmentEntity.builder()
            .equipmentId(equipment.getEquipmentId())
            .name(equipment.getName())
            .category(equipment.getCategory())
            .serialNumber(equipment.getSerialNumber())
            .inventoryNumber(equipment.getInventoryNumber())
            .macAddress(equipment.getMacAddress())
            .macAddress2(equipment.getMacAddress2())
            .brand(equipment.getBrand())
            .model(equipment.getModel())
            .status(equipment.getStatus())
            .purchaseDate(equipment.getPurchaseDate())
            .purchaseValue(equipment.getPurchaseValue())
            .assignedTo(equipment.getAssignedTo())
            .createdAt(equipment.getCreatedAt())
            .updatedAt(equipment.getUpdatedAt())
            .createdBy(equipment.getCreatedBy())
            .updatedBy(equipment.getLastModifiedBy())
            .ownershipType(equipment.getOwnershipType())
            .nextMaintenanceDate(equipment.getNextMaintenanceDate())
            .costCenter(equipment.getCostCenter())
            .operatingSystem(equipment.getOperatingSystem())
            .osVersion(equipment.getOsVersion())
            .responsiblePosition(equipment.getResponsiblePosition())
            .responsibleDocument(equipment.getResponsibleDocument())
            .responsiblePhone(equipment.getResponsiblePhone())
            .responsibleEmail(equipment.getResponsibleEmail())
            .ipAddress(equipment.getIpAddress())
            .ipAssignment(equipment.getIpAssignment())
            .associatedEquipmentId(equipment.getAssociatedEquipmentId());

        if (equipment.getLocation() != null) {
            builder.locationBuilding(equipment.getLocation().getBuilding())
                   .locationFloor(equipment.getLocation().getFloor())
                   .locationOffice(equipment.getLocation().getOffice());
        }

        if (equipment.getRentalInfo() != null) {
            RentalInfo r = equipment.getRentalInfo();
            builder.rentalCompany(r.getRentalCompany())
                   .rentalContactName(r.getContactName())
                   .rentalContactPhone(r.getContactPhone())
                   .rentalContactEmail(r.getContactEmail())
                   .rentalStartDate(r.getStartDate())
                   .rentalEndDate(r.getEndDate())
                   .rentalContractNumber(r.getContractNumber())
                   .rentalContractFileUrl(r.getContractFileUrl())
                   .rentalNotes(r.getNotes())
                   .rentalMonthlyValue(r.getMonthlyValue());
        }

        if (equipment.getHardware() != null) {
            Hardware h = equipment.getHardware();
            builder.hardwareProcessor(h.getProcessor())
                   .hardwareRamSizeGb(h.getRamSizeGb())
                   .hardwareRamType(h.getRamType())
                   .hardwareDiskType(h.getDiskType())
                   .hardwareDiskSizeGb(h.getDiskSizeGb())
                   .hardwareDiskHealthPercent(h.getDiskHealthPercent())
                   .hardwareDiskTemperatureCelsius(h.getDiskTemperatureCelsius());
        }

        return builder.build();
    }

    public Equipment toDomain(EquipmentEntity entity) {
        Location location = Location.of(
            entity.getLocationBuilding() != null ? entity.getLocationBuilding() : "",
            entity.getLocationFloor() != null ? entity.getLocationFloor() : "",
            entity.getLocationOffice() != null ? entity.getLocationOffice() : "",
            ""
        );

        RentalInfo rentalInfo = null;
        if (entity.getRentalCompany() != null || entity.getRentalStartDate() != null
                || entity.getRentalMonthlyValue() != null) {
            rentalInfo = RentalInfo.builder()
                .rentalCompany(entity.getRentalCompany())
                .contactName(entity.getRentalContactName())
                .contactPhone(entity.getRentalContactPhone())
                .contactEmail(entity.getRentalContactEmail())
                .startDate(entity.getRentalStartDate())
                .endDate(entity.getRentalEndDate())
                .contractNumber(entity.getRentalContractNumber())
                .contractFileUrl(entity.getRentalContractFileUrl())
                .notes(entity.getRentalNotes())
                .monthlyValue(entity.getRentalMonthlyValue())
                .build();
        }

        Hardware hardware = null;
        // Cualquier dato de hardware registrado hace que el bloque se lea
        if (entity.getHardwareProcessor() != null || entity.getHardwareRamSizeGb() != null
                || entity.getHardwareRamType() != null || entity.getHardwareDiskType() != null
                || entity.getHardwareDiskSizeGb() != null || entity.getHardwareDiskHealthPercent() != null
                || entity.getHardwareDiskTemperatureCelsius() != null) {
            hardware = Hardware.builder()
                .processor(entity.getHardwareProcessor())
                .ramSizeGb(entity.getHardwareRamSizeGb())
                .ramType(entity.getHardwareRamType())
                .diskType(entity.getHardwareDiskType())
                .diskSizeGb(entity.getHardwareDiskSizeGb())
                .diskHealthPercent(entity.getHardwareDiskHealthPercent())
                .diskTemperatureCelsius(entity.getHardwareDiskTemperatureCelsius())
                .build();
        }

        return Equipment.builder()
            .equipmentId(entity.getEquipmentId())
            .name(entity.getName())
            .category(entity.getCategory())
            .serialNumber(entity.getSerialNumber())
            .inventoryNumber(entity.getInventoryNumber())
            .macAddress(entity.getMacAddress())
            .macAddress2(entity.getMacAddress2())
            .brand(entity.getBrand())
            .model(entity.getModel())
            .status(entity.getStatus())
            .purchaseDate(entity.getPurchaseDate())
            .purchaseValue(entity.getPurchaseValue())
            .location(location)
            .assignedTo(entity.getAssignedTo())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .createdBy(entity.getCreatedBy())
            .lastModifiedBy(entity.getUpdatedBy())
            .ownershipType(entity.getOwnershipType())
            .nextMaintenanceDate(entity.getNextMaintenanceDate())
            .costCenter(entity.getCostCenter())
            .operatingSystem(entity.getOperatingSystem())
            .osVersion(entity.getOsVersion())
            .responsiblePosition(entity.getResponsiblePosition())
            .responsibleDocument(entity.getResponsibleDocument())
            .responsiblePhone(entity.getResponsiblePhone())
            .responsibleEmail(entity.getResponsibleEmail())
            .ipAddress(entity.getIpAddress())
            .ipAssignment(entity.getIpAssignment())
            .associatedEquipmentId(entity.getAssociatedEquipmentId())
            .rentalInfo(rentalInfo)
            .hardware(hardware)
            .build();
    }
}