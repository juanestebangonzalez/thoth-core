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
        Location location = Location.of(toUpper(command.building()), toUpper(command.floor()), toUpper(command.office()), "");

        Equipment equipment = Equipment.create(
            toUpper(command.name()),
            toUpper(command.category()),
            toUpper(command.serialNumber()),
            toUpper(command.brand()),
            toUpper(command.model()),
            command.macAddress() != null ? command.macAddress().toUpperCase().trim() : null,
            command.purchaseDate(),
            command.purchaseValue(),
            location,
            toUpper(command.assignedTo()),
            command.createdBy()
        );

        // Inventory number - validar unicidad
        if (command.inventoryNumber() != null && !command.inventoryNumber().isBlank()) {
            String invNum = command.inventoryNumber().toUpperCase().trim();
            var existing = equipmentRepository.findByInventoryNumber(invNum);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Ya existe un equipo con el numero de inventario: " + invNum);
            }
            equipment.setInventoryNumber(invNum);
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
                .rentalCompany(toUpper(command.rentalCompany()))
                .contactName(toUpper(command.rentalContactName()))
                .contactPhone(command.rentalContactPhone())
                .contactEmail(command.rentalContactEmail())
                .startDate(command.rentalStartDate())
                .endDate(command.rentalEndDate())
                .contractNumber(toUpper(command.rentalContractNumber()))
                .contractFileUrl(command.rentalContractFileUrl())
                .notes(toUpper(command.rentalNotes()))
                .build();
            equipment.setRentalInfo(rentalInfo);
        }

        if (hasHardwareData(command)) {
            Hardware hardware = Hardware.builder()
                .processor(toUpper(command.processor()))
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
        java.time.LocalDate nextDate = schedulerService.calculateNextMaintenanceDate(equipment.getCategory().toUpperCase(), java.time.LocalDate.now());
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

    private String toUpper(String value) {
        return value != null ? value.toUpperCase().trim() : null;
    }
}