package com.thoth.config.security;

import com.thoth.application.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Evaluador de permisos granulares para usar en {@code @PreAuthorize}:
 * <pre>@PreAuthorize("@perm.can(authentication,'CATALOGS','EDIT')")</pre>
 *
 * - ADMIN: siempre tiene permiso.
 * - Otros roles: se consulta la tabla user_permission del usuario autenticado (por su username).
 * - Nunca lanza excepciones: ante cualquier error se niega el acceso (403).
 */
@Component("perm")
public class PermissionGuard {

    private static final Logger log = LoggerFactory.getLogger(PermissionGuard.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final PermissionService permissionService;

    public PermissionGuard(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public boolean can(Authentication auth, String module, String action) {
        if (auth == null || !auth.isAuthenticated() || module == null || action == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (ROLE_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        try {
            return permissionService.hasPermissionByUsername(auth.getName(), module, action);
        } catch (RuntimeException e) {
            log.warn("No se pudo verificar el permiso {}/{} del usuario {}: {}", module, action, auth.getName(), e.getMessage());
            return false;
        }
    }
}
