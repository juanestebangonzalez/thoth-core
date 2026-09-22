package com.thoth.application.port.input;

import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface RegisterEquipmentUseCase {
    EquipmentResponseDTO register(RegisterEquipmentCommand command);
}
