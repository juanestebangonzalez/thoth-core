package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.dto.HardwareDTO;
import com.thoth.application.dto.LocationDTO;
import com.thoth.application.dto.RentalInfoDTO;
import com.thoth.application.dto.UsefulLifeDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Hardware;
import com.thoth.domain.valueobject.RentalInfo;
import com.thoth.domain.valueobject.UsefulLife;
import org.springframework.stereotype.Component;

@Component
public class EquipmentDtoMapper {

    public EquipmentDTO toDTO(Equipment equipment) {
        return toDTO(equipment, null);
    }

    /**
     * @param associated equipo al que esta asociado el monitor (para nombre e inventario); puede ser null.
     */
    public EquipmentDTO toDTO(Equipment equipment, Equipment associated) {
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
            equipment.getNextMaintenanceDate(),
            equipment.getMacAddress2(),
            equipment.getCostCenter(),
            equipment.getOperatingSystem(),
            equipment.getOsVersion(),
            equipment.getResponsiblePosition(),
            equipment.getResponsibleDocument(),
            equipment.getResponsiblePhone(),
            equipment.getResponsibleEmail(),
            equipment.getIpAddress(),
            equipment.getIpAssignment(),
            equipment.getAssociatedEquipmentId(),
            associatedName(equipment, associated),
            associatedInventory(equipment, associated),
            mapUsefulLife(equipment),
            equipment.calculateCriticality(),
            equipment.getOsEdition(),
            equipment.getOsLicenseType()
        );
    }

    public EquipmentResponseDTO toResponseDTO(Equipment equipment) {
        return toResponseDTO(equipment, null);
    }

    public EquipmentResponseDTO toResponseDTO(Equipment equipment, Equipment associated) {
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
            equipment.getNextMaintenanceDate(),
            equipment.getMacAddress2(),
            equipment.getCostCenter(),
            equipment.getOperatingSystem(),
            equipment.getOsVersion(),
            equipment.getResponsiblePosition(),
            equipment.getResponsibleDocument(),
            equipment.getResponsiblePhone(),
            equipment.getResponsibleEmail(),
            equipment.getIpAddress(),
            equipment.getIpAssignment(),
            equipment.getAssociatedEquipmentId(),
            associatedName(equipment, associated),
            associatedInventory(equipment, associated),
            mapUsefulLife(equipment),
            equipment.calculateCriticality(),
            equipment.getOsEdition(),
            equipment.getOsLicenseType()
        );
    }

    /** Vida util calculada (no se persiste). */
    public UsefulLifeDTO mapUsefulLife(Equipment equipment) {
        UsefulLife life = equipment.calculateUsefulLife();
        return new UsefulLifeDTO(life.getYears(), life.getAgeYears(), life.getConsumedPercent(),
            life.getRemainingYears(), life.isEstimated());
    }

    private static boolean matchesAssociated(Equipment equipment, Equipment associated) {
        return associated != null && equipment.getAssociatedEquipmentId() != null
            && equipment.getAssociatedEquipmentId().equals(associated.getEquipmentId());
    }

    private static String associatedName(Equipment equipment, Equipment associated) {
        return matchesAssociated(equipment, associated) ? associated.getName() : null;
    }

    private static String associatedInventory(Equipment equipment, Equipment associated) {
        return matchesAssociated(equipment, associated) ? associated.getInventoryNumber() : null;
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
            r.isContractExpiringSoon(),
            r.getMonthlyValue()
        );
    }

    private HardwareDTO mapHardware(Equipment equipment) {
        if (equipment.getHardware() == null) return null;
        Hardware h = equipment.getHardware();

        // Umbrales centralizados en Hardware: "OK" / "ADVERTENCIA" / "CRITICO"
        String healthStatus = Hardware.diskHealthLevel(h.getDiskHealthPercent());
        String tempStatus = Hardware.diskTemperatureLevel(h.getDiskTemperatureCelsius());

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