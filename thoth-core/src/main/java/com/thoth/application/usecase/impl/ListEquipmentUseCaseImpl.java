package com.thoth.application.usecase.impl;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.input.ListEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListEquipmentUseCaseImpl implements ListEquipmentUseCase {
    
    private final EquipmentRepositoryPort equipmentRepository;
    private final EquipmentDtoMapper equipmentMapper;
    
    @Override
    public PageResponseDTO<EquipmentDTO> listAll(ListEquipmentCommand command) {
        List<EquipmentDTO> content = equipmentRepository.findAll().stream()
            .map(equipmentMapper::toDTO)
            .collect(Collectors.toList());
        
        return new PageResponseDTO<>(
            content,
            command.pageNumber(),
            command.pageSize(),
            (long) content.size(),
            (content.size() + command.pageSize() - 1) / command.pageSize()
        );
    }
}
