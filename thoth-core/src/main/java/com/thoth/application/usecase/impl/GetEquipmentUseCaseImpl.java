package com.thoth.application.usecase.impl;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.GetEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetEquipmentUseCaseImpl implements GetEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public EquipmentDTO getById(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .map(equipmentMapper::toDTO)
            .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
    }
    
    @Override
    public EquipmentDTO getBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber)
            .map(equipmentMapper::toDTO)
            .orElseThrow(() -> new EquipmentNotFoundException(serialNumber));
    }
}
