package com.thoth.adapter.in.rest.controller;

import com.thoth.domain.valueobject.Hardware;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parametros de configuracion de solo lectura que el frontend necesita conocer.
 */
@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Configuracion", description = "Parametros de configuracion del sistema")
public class SettingsController {

    @GetMapping("/hardware-thresholds")
    @Operation(summary = "Umbrales de salud y temperatura del disco",
               description = "Salud: ADVERTENCIA si < diskHealthWarning, CRITICO si < diskHealthCritical. "
                   + "Temperatura: ADVERTENCIA si > diskTempWarning, CRITICO si >= diskTempCritical.")
    public ResponseEntity<Map<String, Integer>> hardwareThresholds() {
        Map<String, Integer> umbrales = new LinkedHashMap<>();
        umbrales.put("diskHealthWarning", Hardware.DISK_HEALTH_WARNING);
        umbrales.put("diskHealthCritical", Hardware.DISK_HEALTH_CRITICAL);
        umbrales.put("diskTempWarning", Hardware.DISK_TEMP_WARNING);
        umbrales.put("diskTempCritical", Hardware.DISK_TEMP_CRITICAL);
        return ResponseEntity.ok(umbrales);
    }
}
