package com.thoth.application.port.input;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;

public interface ListEquipmentUseCase {
    PageResponseDTO<EquipmentDTO> listAll(ListEquipmentCommand command);
}
