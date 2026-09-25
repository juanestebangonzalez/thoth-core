package com.thoth.application.mapper;

import com.thoth.application.dto.MaintenanceHistoryDTO;
import com.thoth.application.dto.PartReplacedDTO;
import com.thoth.domain.model.MaintenanceHistory;
import com.thoth.domain.valueobject.PartReplaced;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MaintenanceHistoryDtoMapper {

    public MaintenanceHistoryDTO toDTO(MaintenanceHistory h) {
        List<PartReplacedDTO> partsDto = h.getPartsReplaced() != null
            ? h.getPartsReplaced().stream().map(this::toPartDTO).toList()
            : List.of();

        return new MaintenanceHistoryDTO(
            h.getMaintenanceId(),
            h.getEquipmentId(),
            h.getMaintenanceType() != null ? h.getMaintenanceType().name() : null,
            h.getPerformedDate(),
            h.getTechnicianName(),
            h.getTechnicianId(),
            h.getReason(),
            h.getDescription(),
            h.getNextScheduledDate(),
            partsDto,
            h.getPartsCount(),
            h.getCreatedBy(),
            h.getSignatureBase64(),
            h.getSignedBy(),
            h.getCreatedAt()
        );
    }

    private PartReplacedDTO toPartDTO(PartReplaced p) {
        return new PartReplacedDTO(
            p.getPartName(),
            p.getPartSerialNumber(),
            p.getReason(),
            p.getPurchaseDate(),
            p.getTicketNumber()
        );
    }
}