package com.thoth.application.usecase.impl;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.LocationDtoMapper;
import com.thoth.application.port.input.UpdateEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateEquipmentUseCaseImpl implements UpdateEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final LocationDtoMapper locationMapper;
    
    @Override
    public EquipmentResponseDTO update(UpdateEquipmentCommand command) {
        Equipment equipment = equipmentRepository.findById(command.equipmentId())
            .orElseThrow(() -> new EquipmentNotFoundException(command.equipmentId()));
        
        if (command.name() != null && !command.name().isBlank()) {
            equipment.setName(command.name());
        }
        
        if (command.assignedTo() != null && !command.assignedTo().isBlank()) {
            equipment.reassignTo(command.assignedTo());
        }
        
        if (command.building() != null && command.floor() != null && command.office() != null) {
            Location location = Location.of(
                command.building(),
                command.floor(),
                command.office(),
                ""
            );
            equipment.updateLocation(location);
        }
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            saved.getLocation().getFullAddress(),
            saved.getAssignedTo(),
            "Equipment updated successfully"
        );
    }
}
