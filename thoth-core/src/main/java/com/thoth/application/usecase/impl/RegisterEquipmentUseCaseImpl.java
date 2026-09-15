package com.thoth.application.usecase.impl;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.RegisterEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.service.MaintenanceSchedulerService;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterEquipmentUseCaseImpl implements RegisterEquipmentUseCase {

    private final EquipmentRepositoryPort equipmentRepository;
    private final MaintenanceSchedulerService schedulerService;
    private final EquipmentDtoMapper mapper;

    @Override
    @Transactional
    public EquipmentResponseDTO register(RegisterEquipmentCommand command) {
        Location location = Location.of(command.building(), command.floor(), command.office(), "");
        EquipmentCategory category = EquipmentCategory.valueOf(command.category().toUpperCase());

        Equipment equipment = Equipment.create(
            command.name(),
            category,
            command.serialNumber(),
            command.brand(),
            command.model(),
            command.macAddress(),
            command.purchaseDate(),
            command.purchaseValue(),
            location,
            command.assignedTo(),
            command.createdBy()
        );

        // Inventory number - validar unicidad
        if (command.inventoryNumber() != null && !command.inventoryNumber().isBlank()) {
            var existing = equipmentRepository.findByInventoryNumber(command.inventoryNumber());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Ya existe un equipo con el numero de inventario: " + command.inventoryNumber());
            }
            equipment.setInventoryNumber(command.inventoryNumber());
        }

        if (command.ownershipType() != null && !command.ownershipType().isBlank()) {
            try {
                equipment.setOwnershipType(OwnershipType.valueOf(command.ownershipType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                equipment.setOwnershipType(OwnershipType.OWNED);
            }
        }

        if (equipment.getOwnershipType() == OwnershipType.RENTED) {
            RentalInfo rentalInfo = RentalInfo.builder()
                .rentalCompany(command.rentalCompany())
                .contactName(command.rentalContactName())
                .contactPhone(command.rentalContactPhone())
                .contactEmail(command.rentalContactEmail())
                .startDate(command.rentalStartDate())
                .endDate(command.rentalEndDate())
                .contractNumber(command.rentalContractNumber())
                .contractFileUrl(command.rentalContractFileUrl())
                .notes(command.rentalNotes())
                .build();
            equipment.setRentalInfo(rentalInfo);
        }

        if (hasHardwareData(command)) {
            Hardware hardware = Hardware.builder()
                .processor(command.processor())
                .ramSizeGb(command.ramSizeGb())
                .ramType(parseRamType(command.ramType()))
                .diskType(parseDiskType(command.diskType()))
                .diskSizeGb(command.diskSizeGb())
                .diskHealthPercent(command.diskHealthPercent())
                .diskTemperatureCelsius(command.diskTemperatureCelsius())
                .build();
            equipment.setHardware(hardware);
        }

        // Calcular proxima fecha de mantenimiento automatica
        java.time.LocalDate nextDate = schedulerService.calculateNextMaintenanceDate(equipment.getCategory(), java.time.LocalDate.now());
        if (nextDate != null) {
            equipment.setNextMaintenanceDate(nextDate);
        }
        Equipment saved = equipmentRepository.save(equipment);
        return mapper.toResponseDTO(saved);
    }

    private boolean hasHardwareData(RegisterEquipmentCommand cmd) {
        return cmd.processor() != null || cmd.ramSizeGb() != null || cmd.diskType() != null;
    }

    private RamType parseRamType(String type) {
        if (type == null || type.isBlank()) return null;
        try { return RamType.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }

    private DiskType parseDiskType(String type) {
        if (type == null || type.isBlank()) return null;
        try { return DiskType.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}