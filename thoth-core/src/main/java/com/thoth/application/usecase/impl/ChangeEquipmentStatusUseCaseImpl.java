package com.thoth.application.usecase.impl;

import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.exception.InvalidStatusTransitionException;
import com.thoth.application.port.input.ChangeEquipmentStatusUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeEquipmentStatusUseCaseImpl implements ChangeEquipmentStatusUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    
    @Override
    public EquipmentResponseDTO changeStatus(ChangeStatusCommand command) {
        Equipment equipment = equipmentRepository.findById(command.equipmentId())
            .orElseThrow(() -> new EquipmentNotFoundException(command.equipmentId()));
        
        EquipmentStatus newStatus = parseStatus(command.newStatus());
        
        switch (newStatus) {
            case MAINTENANCE:
                equipment.markForMaintenance();
                break;
            case ACTIVE:
                equipment.markAsActive();
                break;
            case INACTIVE:
                equipment.markAsInactive();
                break;
            case RETIRED:
                equipment.markAsRetired();
                break;
        }
        
        Equipment saved = equipmentRepository.save(equipment);
        
        return new EquipmentResponseDTO(
            saved.getEquipmentId(),
            saved.getName(),
            saved.getCategory().getDisplayName(),
            saved.getStatus().getDisplayName(),
            saved.getLocation().getFullAddress(),
            saved.getAssignedTo(),
            "Status changed to " + newStatus.getDisplayName()
        );
    }
    
    private EquipmentStatus parseStatus(String status) {
        try {
            return EquipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException("Invalid status: " + status);
        }
    }
}
