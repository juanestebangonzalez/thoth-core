package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class EquipmentEntityMapper {
    
    public EquipmentEntity toEntity(Equipment domain) {
        if (domain == null) {
            return null;
        }
        
        return EquipmentEntity.builder()
            .equipmentId(domain.getEquipmentId())
            .name(domain.getName())
            .category(domain.getCategory().name())
            .serialNumber(domain.getSerialNumber())
            .brand(domain.getBrand())
            .model(domain.getModel())
            .macAddress(domain.getMacAddress())
            .status(domain.getStatus().name())
            .locationBuilding(domain.getLocation().getBuilding())
            .locationFloor(domain.getLocation().getFloor())
            .locationOffice(domain.getLocation().getOffice())
            .assignedTo(domain.getAssignedTo())
            .purchaseDate(domain.getPurchaseDate())
            .purchaseValue(domain.getPurchaseValue())
            .createdBy(domain.getCreatedBy())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
    
    public Equipment toDomain(EquipmentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Location location = Location.of(
                entity.getLocationBuilding(),
                entity.getLocationFloor(),
                entity.getLocationOffice(),
                ""
        );
        
        Equipment equipment = Equipment.builder()
            .equipmentId(entity.getEquipmentId())
            .name(entity.getName())
            .category(EquipmentCategory.valueOf(entity.getCategory()))
            .serialNumber(entity.getSerialNumber())
            .brand(entity.getBrand())
            .model(entity.getModel())
            .macAddress(entity.getMacAddress())
            .status(EquipmentStatus.valueOf(entity.getStatus()))
            .location(location)
            .assignedTo(entity.getAssignedTo())
            .purchaseDate(entity.getPurchaseDate())
            .purchaseValue(entity.getPurchaseValue())
            .createdBy(entity.getCreatedBy())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
        
        return equipment;
    }
}