package com.thoth.adapter.in.rest.controller;

import com.thoth.application.dto.AssociatedMonitorDTO;
import com.thoth.application.service.EquipmentMonitorService;
import com.thoth.application.service.HojaVidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Hoja de vida", description = "Hoja de vida y monitores asociados de un equipo")
@RequiredArgsConstructor
public class EquipmentHojaVidaController {

    private final EquipmentMonitorService equipmentMonitorService;
    private final HojaVidaService hojaVidaService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/monitors")
    @Operation(summary = "Monitores asociados al equipo")
    public ResponseEntity<List<AssociatedMonitorDTO>> monitors(@PathVariable UUID id) {
        return ResponseEntity.ok(equipmentMonitorService.listMonitors(id));
    }

    @PreAuthorize("@perm.can(authentication,'EQUIPMENT','VIEW')")
    @GetMapping("/{id}/hoja-vida")
    @Operation(summary = "Hoja de vida del equipo",
        description = "Datos completos, perifericos, monitores, mantenimientos con partes, traslados, documentos y baja")
    public ResponseEntity<Map<String, Object>> hojaVida(@PathVariable UUID id) {
        return ResponseEntity.ok(hojaVidaService.build(id));
    }
}
