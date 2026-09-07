package com.thoth.application.port.input;

import com.thoth.application.command.ChangeStatusCommand;
import com.thoth.application.dto.EquipmentResponseDTO;

public interface ChangeEquipmentStatusUseCase {
    EquipmentResponseDTO changeStatus(ChangeStatusCommand command);
}
