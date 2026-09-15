package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.entity.PartReplacedEntity;
import com.thoth.domain.model.MaintenanceHistory;
import com.thoth.domain.valueobject.PartReplaced;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class MaintenanceHistoryEntityMapper {

    public MaintenanceHistoryEntity toEntity(MaintenanceHistory domain) {
        MaintenanceHistoryEntity entity = MaintenanceHistoryEntity.builder()
            .maintenanceId(domain.getMaintenanceId())
            .equipmentId(domain.getEquipmentId())
            .maintenanceType(domain.getMaintenanceType())
            .performedDate(domain.getPerformedDate())
            .technicianName(domain.getTechnicianName())
            .technicianId(domain.getTechnicianId())
            .reason(domain.getReason())
            .description(domain.getDescription())
            .nextScheduledDate(domain.getNextScheduledDate())
            .createdAt(domain.getCreatedAt())
            .createdBy(domain.getCreatedBy())
            .partsReplaced(new ArrayList<>())
            .build();

        if (domain.getPartsReplaced() != null) {
            for (PartReplaced part : domain.getPartsReplaced()) {
                PartReplacedEntity partEntity = PartReplacedEntity.builder()
                    .partName(part.getPartName())
                    .partSerialNumber(part.getPartSerialNumber())
                    .reason(part.getReason())
                    .maintenanceHistory(entity)
                    .build();
                entity.getPartsReplaced().add(partEntity);
            }
        }

        return entity;
    }

    public MaintenanceHistory toDomain(MaintenanceHistoryEntity entity) {
        List<PartReplaced> parts = new ArrayList<>();
        if (entity.getPartsReplaced() != null) {
            for (PartReplacedEntity partEntity : entity.getPartsReplaced()) {
                parts.add(PartReplaced.builder()
                    .partName(partEntity.getPartName())
                    .partSerialNumber(partEntity.getPartSerialNumber())
                    .reason(partEntity.getReason())
                    .build());
            }
        }

        return MaintenanceHistory.builder()
            .maintenanceId(entity.getMaintenanceId())
            .equipmentId(entity.getEquipmentId())
            .maintenanceType(entity.getMaintenanceType())
            .performedDate(entity.getPerformedDate())
            .technicianName(entity.getTechnicianName())
            .technicianId(entity.getTechnicianId())
            .reason(entity.getReason())
            .description(entity.getDescription())
            .nextScheduledDate(entity.getNextScheduledDate())
            .partsReplaced(parts)
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .build();
    }
}