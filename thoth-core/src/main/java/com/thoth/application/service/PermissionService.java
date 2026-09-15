package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.entity.UserPermissionEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import com.thoth.adapter.out.persistence.repository.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserPermissionRepository permissionRepository;
    private final UserJpaRepository userRepository;

    public static final List<String> ALL_MODULES = List.of(
        "EQUIPMENT", "MAINTENANCE", "AI", "REPORTS", "CALENDAR", "ALERTS", "QR", "DOCUMENTS", "USERS"
    );

    public static final List<String> ALL_ACTIONS = List.of(
        "VIEW", "CREATE", "EDIT", "DELETE"
    );

    /**
     * Devuelve los permisos de un usuario como Map<modulo, List<acciones>>
     */
    public Map<String, List<String>> getUserPermissions(UUID userId) {
        List<UserPermissionEntity> perms = permissionRepository.findByUserId(userId);

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String module : ALL_MODULES) {
            result.put(module, new ArrayList<>());
        }

        for (UserPermissionEntity p : perms) {
            result.computeIfAbsent(p.getModule(), k -> new ArrayList<>()).add(p.getAction());
        }

        return result;
    }

    /**
     * Reemplaza todos los permisos de un usuario
     */
    @Transactional
    public Map<String, List<String>> setUserPermissions(UUID userId, Map<String, List<String>> permissions) {
        // Borrar todos los permisos existentes
        List<UserPermissionEntity> existing = permissionRepository.findByUserId(userId);
        if (!existing.isEmpty()) {
            permissionRepository.deleteAll(existing);
            permissionRepository.flush();
        }

        List<UserPermissionEntity> entities = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
            String module = entry.getKey().toUpperCase();
            if (!ALL_MODULES.contains(module)) continue;

            for (String action : entry.getValue()) {
                String act = action.toUpperCase();
                if (!ALL_ACTIONS.contains(act)) continue;

                entities.add(UserPermissionEntity.builder()
                    .userId(userId)
                    .module(module)
                    .action(act)
                    .build());
            }
        }

        if (!entities.isEmpty()) {
            permissionRepository.saveAll(entities);
        }
        return getUserPermissions(userId);
    }

    /**
     * Verifica si un usuario tiene permiso sobre un modulo y accion
     */
    public boolean hasPermission(UUID userId, String module, String action) {
        return permissionRepository.existsByUserIdAndModuleAndAction(userId, module.toUpperCase(), action.toUpperCase());
    }

    /**
     * Guardia de autorizacion para usar directamente en controladores: resuelve el
     * username autenticado a su userId y exige el permiso modulo/accion indicado.
     * Lanza AccessDeniedException (403, via GlobalExceptionHandler) si no lo tiene.
     */
    public void requireModulePermission(String username, String module, String action) {
        UUID userId = userRepository.findByUsername(username)
            .map(UserEntity::getId)
            .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));

        if (!hasPermission(userId, module, action)) {
            throw new AccessDeniedException(
                "No tiene el permiso " + action.toUpperCase() + " sobre " + module.toUpperCase());
        }
    }

    /**
     * Asigna permisos por defecto segun el rol
     */
    @Transactional
    public void assignDefaultPermissions(UUID userId, String role) {
        Map<String, List<String>> defaults = getDefaultPermissions(role);
        setUserPermissions(userId, defaults);
    }

    /**
     * Permisos por defecto segun rol
     */
    public Map<String, List<String>> getDefaultPermissions(String role) {
        Map<String, List<String>> perms = new LinkedHashMap<>();

        switch (role.toUpperCase()) {
            case "ADMIN":
                for (String m : ALL_MODULES) perms.put(m, new ArrayList<>(ALL_ACTIONS));
                break;
            case "TECHNICIAN":
                perms.put("EQUIPMENT", List.of("VIEW", "CREATE", "EDIT"));
                perms.put("MAINTENANCE", List.of("VIEW", "CREATE", "EDIT"));
                perms.put("AI", List.of("VIEW"));
                perms.put("REPORTS", List.of("VIEW"));
                perms.put("CALENDAR", List.of("VIEW"));
                perms.put("ALERTS", List.of("VIEW"));
                perms.put("QR", List.of("VIEW"));
                perms.put("DOCUMENTS", List.of("VIEW", "CREATE", "DELETE"));
                perms.put("USERS", List.of());
                break;
            case "USER":
                perms.put("EQUIPMENT", List.of("VIEW"));
                perms.put("MAINTENANCE", List.of("VIEW"));
                perms.put("AI", List.of());
                perms.put("REPORTS", List.of("VIEW"));
                perms.put("CALENDAR", List.of("VIEW"));
                perms.put("ALERTS", List.of("VIEW"));
                perms.put("QR", List.of("VIEW"));
                perms.put("DOCUMENTS", List.of("VIEW"));
                perms.put("USERS", List.of());
                break;
            case "VIEWER":
                perms.put("EQUIPMENT", List.of("VIEW"));
                perms.put("MAINTENANCE", List.of());
                perms.put("AI", List.of());
                perms.put("REPORTS", List.of("VIEW"));
                perms.put("CALENDAR", List.of("VIEW"));
                perms.put("ALERTS", List.of());
                perms.put("QR", List.of());
                perms.put("DOCUMENTS", List.of());
                perms.put("USERS", List.of());
                break;
            default:
                for (String m : ALL_MODULES) perms.put(m, List.of());
                break;
        }
        return perms;
    }
}