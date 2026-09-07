package com.thoth.application.usecase.impl;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.ValidationException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.mapper.LocationDtoMapper;
import com.thoth.application.port.input.RegisterEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterEquipmentUseCaseImpl implements RegisterEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final LocationDtoMapper locationMapper;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public EquipmentResponseDTO register(RegisterEquipmentCommand command) {
        validateCommand(command);
        
        Location location = Location.of(
            command.building(),
            command.floor(),
            command.office(),
            ""
        );
        
        EquipmentCategory category = EquipmentCategory.valueOf(command.category());
        
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
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            location.getFullAddress(),
            saved.getAssignedTo(),
            "Equipment registered successfully"
        );
    }
    
    private void validateCommand(RegisterEquipmentCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new ValidationException("Name cannot be blank");
        }
        if (command.serialNumber() == null || command.serialNumber().isBlank()) {
            throw new ValidationException("Serial number cannot be blank");
        }
        try {
            EquipmentCategory.valueOf(command.category());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid equipment category: " + command.category());
        }
    }
}
