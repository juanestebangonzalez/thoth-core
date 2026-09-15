package com.thoth.adapter.in.rest.controller;

import com.thoth.application.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Gestion de permisos granulares")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/{userId}")
    @Operation(summary = "Obtener permisos de un usuario")
    public ResponseEntity<Map<String, List<String>>> getUserPermissions(@PathVariable UUID userId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Establecer permisos de un usuario")
    public ResponseEntity<Map<String, List<String>>> setUserPermissions(
            @PathVariable UUID userId,
            @RequestBody Map<String, List<String>> permissions) {
        return ResponseEntity.ok(permissionService.setUserPermissions(userId, permissions));
    }

    @PostMapping("/{userId}/defaults")
    @Operation(summary = "Asignar permisos por defecto segun rol")
    public ResponseEntity<Map<String, List<String>>> assignDefaults(
            @PathVariable UUID userId,
            @RequestParam String role) {
        permissionService.assignDefaultPermissions(userId, role);
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    @GetMapping("/{userId}/check")
    @Operation(summary = "Verificar si un usuario tiene un permiso especifico")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @PathVariable UUID userId,
            @RequestParam String module,
            @RequestParam String action) {
        boolean has = permissionService.hasPermission(userId, module, action);
        return ResponseEntity.ok(Map.of("hasPermission", has));
    }

    @GetMapping("/modules")
    @Operation(summary = "Listar todos los modulos y acciones disponibles")
    public ResponseEntity<Map<String, Object>> getModules() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("modules", PermissionService.ALL_MODULES);
        result.put("actions", PermissionService.ALL_ACTIONS);

        Map<String, String> moduleLabels = new LinkedHashMap<>();
        moduleLabels.put("EQUIPMENT", "Equipos");
        moduleLabels.put("MAINTENANCE", "Mantenimiento");
        moduleLabels.put("AI", "Inteligencia Artificial");
        moduleLabels.put("REPORTS", "Reportes");
        moduleLabels.put("CALENDAR", "Calendario");
        moduleLabels.put("ALERTS", "Alertas");
        moduleLabels.put("QR", "Codigos QR");
        moduleLabels.put("DOCUMENTS", "Documentos");
        moduleLabels.put("USERS", "Usuarios");
        result.put("moduleLabels", moduleLabels);

        Map<String, String> actionLabels = new LinkedHashMap<>();
        actionLabels.put("VIEW", "Ver");
        actionLabels.put("CREATE", "Crear");
        actionLabels.put("EDIT", "Editar");
        actionLabels.put("DELETE", "Eliminar");
        result.put("actionLabels", actionLabels);

        return ResponseEntity.ok(result);
    }
}