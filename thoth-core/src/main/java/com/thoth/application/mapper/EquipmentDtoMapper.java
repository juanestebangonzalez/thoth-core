package com.thoth.application.mapper;

import com.thoth.application.dto.EquipmentDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EquipmentDtoMapper {
    
    private final LocationDtoMapper locationMapper;
    
    public EquipmentDTO toDTO(Equipment domain) {
        if (domain == null) return null;
        
        return new EquipmentDTO(
            domain.getEquipmentId(),
            domain.getName(),
            domain.getCategory().getDisplayName(),
            domain.getSerialNumber(),
            domain.getMacAddress(),
            domain.getBrand(),
            domain.getModel(),
            domain.getStatus().getDisplayName(),
            domain.getPurchaseDate(),
            domain.getPurchaseValue(),
            locationMapper.toDTO(domain.getLocation()),
            domain.getAssignedTo(),
            domain.getCreatedBy()
        );
    }
}
