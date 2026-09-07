package com.thoth.application.port.output;

import com.thoth.domain.model.Equipment;

public interface AIAgentPort {
    String analyzeMaintenance(Equipment equipment);
    String predictFailure(Equipment equipment);
    String recommendReplacement(Equipment equipment);
}
