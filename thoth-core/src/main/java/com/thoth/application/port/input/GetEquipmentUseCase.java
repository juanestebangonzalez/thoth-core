package com.thoth.application.port.input;

import com.thoth.application.dto.EquipmentDTO;
import java.util.UUID;

public interface GetEquipmentUseCase {
    EquipmentDTO getById(UUID equipmentId);
    EquipmentDTO getBySerialNumber(String serialNumber);
}
