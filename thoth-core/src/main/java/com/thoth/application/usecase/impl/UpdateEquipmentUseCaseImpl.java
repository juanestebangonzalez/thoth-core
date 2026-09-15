package com.thoth.application.usecase.impl;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.UpdateEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateEquipmentUseCaseImpl implements UpdateEquipmentUseCase {

    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper mapper;

    @Override
    @Transactional
    public EquipmentResponseDTO update(UpdateEquipmentCommand command) {
        Equipment equipment = equipmentRepository.findById(command.equipmentId())
            .orElseThrow(() -> new EquipmentNotFoundException(command.equipmentId()));

        if (command.name() != null && !command.name().isBlank()) {
            equipment.rename(command.name());
        }

        if (command.assignedTo() != null) {
            equipment.reassignTo(command.assignedTo());
        }

        if (command.building() != null && command.floor() != null && command.office() != null) {
            Location newLocation = Location.of(command.building(), command.floor(), command.office(), "");
            equipment.updateLocation(newLocation);
        }

        if (command.brand() != null) equipment.setBrand(command.brand());
        if (command.model() != null) equipment.setModel(command.model());
        if (command.macAddress() != null) equipment.setMacAddress(command.macAddress());

        if (command.ownershipType() != null && !command.ownershipType().isBlank()) {
            try {
                OwnershipType newType = OwnershipType.valueOf(command.ownershipType().toUpperCase());
                equipment.changeOwnershipType(newType);
            } catch (IllegalArgumentException ignored) {}
        }

        if (equipment.getOwnershipType() == OwnershipType.RENTED && hasRentalData(command)) {
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
            equipment.updateRentalInfo(rentalInfo);
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
            equipment.updateHardware(hardware);
        }

        equipment.setLastModifiedBy(command.modifiedBy());
        equipment.setUpdatedAt(LocalDateTime.now());

        if (command.inventoryNumber() != null && !command.inventoryNumber().isBlank()) {
            var existing = equipmentRepository.findByInventoryNumber(command.inventoryNumber());
            if (existing.isPresent() && !existing.get().getEquipmentId().equals(command.equipmentId())) {
                throw new IllegalArgumentException("Ya existe un equipo con el numero de inventario: " + command.inventoryNumber());
            }
            equipment.setInventoryNumber(command.inventoryNumber());
        }

        Equipment saved = equipmentRepository.save(equipment);
        return mapper.toResponseDTO(saved);
    }

    private boolean hasRentalData(UpdateEquipmentCommand cmd) {
        return cmd.rentalCompany() != null || cmd.rentalStartDate() != null;
    }

    private boolean hasHardwareData(UpdateEquipmentCommand cmd) {
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