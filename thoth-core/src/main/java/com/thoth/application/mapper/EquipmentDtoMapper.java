package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.dto.HardwareDTO;
import com.thoth.application.dto.LocationDTO;
import com.thoth.application.dto.RentalInfoDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Hardware;
import com.thoth.domain.valueobject.RentalInfo;
import org.springframework.stereotype.Component;

@Component
public class EquipmentDtoMapper {

    public EquipmentDTO toDTO(Equipment equipment) {
        return new EquipmentDTO(
            equipment.getEquipmentId(),
            equipment.getName(),
            equipment.getCategory(),
            equipment.getSerialNumber(),
            equipment.getInventoryNumber(),
            equipment.getMacAddress(),
            equipment.getBrand(),
            equipment.getModel(),
            equipment.getStatus() != null ? equipment.getStatus().name() : null,
            equipment.getPurchaseDate(),
            equipment.getPurchaseValue(),
            mapLocation(equipment),
            equipment.getAssignedTo(),
            equipment.getCreatedBy(),
            equipment.getOwnershipType() != null ? equipment.getOwnershipType().name() : "OWNED",
            mapRental(equipment),
            mapHardware(equipment),
            equipment.getNextMaintenanceDate()
        );
    }

    public EquipmentResponseDTO toResponseDTO(Equipment equipment) {
        return new EquipmentResponseDTO(
            equipment.getEquipmentId(),
            equipment.getName(),
            equipment.getCategory(),
            equipment.getSerialNumber(),
            equipment.getInventoryNumber(),
            equipment.getMacAddress(),
            equipment.getBrand(),
            equipment.getModel(),
            equipment.getStatus() != null ? equipment.getStatus().name() : null,
            equipment.getPurchaseDate(),
            equipment.getPurchaseValue(),
            mapLocation(equipment),
            equipment.getAssignedTo(),
            equipment.getCreatedAt(),
            equipment.getUpdatedAt(),
            equipment.getCreatedBy(),
            equipment.getOwnershipType() != null ? equipment.getOwnershipType().name() : "OWNED",
            mapRental(equipment),
            mapHardware(equipment),
            equipment.getNextMaintenanceDate()
        );
    }

    private LocationDTO mapLocation(Equipment equipment) {
        if (equipment.getLocation() == null) return null;
        return new LocationDTO(
            equipment.getLocation().getBuilding(),
            equipment.getLocation().getFloor(),
            equipment.getLocation().getOffice(),
            equipment.getLocation().getDescription()
        );
    }

    private RentalInfoDTO mapRental(Equipment equipment) {
        if (equipment.getRentalInfo() == null) return null;
        RentalInfo r = equipment.getRentalInfo();
        return new RentalInfoDTO(
            r.getRentalCompany(),
            r.getContactName(),
            r.getContactPhone(),
            r.getContactEmail(),
            r.getStartDate(),
            r.getEndDate(),
            r.getContractNumber(),
            r.getContractFileUrl(),
            r.getNotes(),
            r.getDaysUntilExpiry(),
            r.isContractExpired(),
            r.isContractExpiringSoon()
        );
    }

    private HardwareDTO mapHardware(Equipment equipment) {
        if (equipment.getHardware() == null) return null;
        Hardware h = equipment.getHardware();

        String healthStatus = "OK";
        if (h.hasCriticalDiskHealth()) healthStatus = "CRITICO";
        else if (h.hasWarningDiskHealth()) healthStatus = "ADVERTENCIA";

        String tempStatus = "OK";
        if (h.hasCriticalTemperature()) tempStatus = "CRITICO";
        else if (h.hasWarningTemperature()) tempStatus = "ADVERTENCIA";

        return new HardwareDTO(
            h.getProcessor(),
            h.getRamSizeGb(),
            h.getRamType() != null ? h.getRamType().name() : null,
            h.getDiskType() != null ? h.getDiskType().name() : null,
            h.getDiskSizeGb(),
            h.getDiskHealthPercent(),
            h.getDiskTemperatureCelsius(),
            healthStatus,
            tempStatus
        );
    }
}