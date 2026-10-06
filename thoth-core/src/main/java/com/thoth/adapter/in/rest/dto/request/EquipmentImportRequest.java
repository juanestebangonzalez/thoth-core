package com.thoth.adapter.in.rest.dto.request;

import com.thoth.application.dto.EquipmentImportRowDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/** Cuerpo de POST /api/v1/equipment/import. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentImportRequest {
    private List<EquipmentImportRowDTO> rows;
}
