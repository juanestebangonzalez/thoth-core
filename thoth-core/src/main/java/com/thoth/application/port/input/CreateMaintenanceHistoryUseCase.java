package com.thoth.application.port.input;

import com.thoth.application.command.CreateMaintenanceHistoryCommand;
import com.thoth.application.dto.MaintenanceHistoryDTO;

public interface CreateMaintenanceHistoryUseCase {
    MaintenanceHistoryDTO create(CreateMaintenanceHistoryCommand command);
}