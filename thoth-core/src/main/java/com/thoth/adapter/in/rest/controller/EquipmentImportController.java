package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.EquipmentImportRequest;
import com.thoth.application.dto.EquipmentImportResultDTO;
import com.thoth.application.service.EquipmentImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipment Import", description = "Importacion masiva de equipos desde plantilla")
@RequiredArgsConstructor
public class EquipmentImportController {

    private final EquipmentImportService importService;

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping("/import")
    @Operation(summary = "Importar equipos masivamente",
               description = "dryRun=true solo valida; dryRun=false importa las filas validas")
    public ResponseEntity<?> importEquipment(
            @RequestParam(defaultValue = "true") boolean dryRun,
            @RequestBody(required = false) EquipmentImportRequest body,
            Principal principal) {
        if (body == null || body.getRows() == null || body.getRows().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El archivo no contiene filas para importar"));
        }
        if (body.getRows().size() > EquipmentImportService.MAX_ROWS) {
            return ResponseEntity.badRequest().body(Map.of("message",
                "El archivo tiene " + body.getRows().size() + " filas; el maximo permitido por importacion es "
                    + EquipmentImportService.MAX_ROWS));
        }
        EquipmentImportResultDTO result = importService.importRows(body.getRows(), dryRun,
            principal != null ? principal.getName() : "SYSTEM");
        return ResponseEntity.ok(result);
    }
}
