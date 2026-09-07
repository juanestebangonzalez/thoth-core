package com.thoth.application.port.input;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface UpdateEquipmentUseCase {
    EquipmentResponseDTO update(UpdateEquipmentCommand command);
}
