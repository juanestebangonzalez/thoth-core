package com.thoth.config.security;

import com.thoth.application.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionGuardTest {

    private PermissionService permissionService;
    private PermissionGuard guard;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        guard = new PermissionGuard(permissionService);
    }

    private static Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    @DisplayName("ADMIN siempre tiene permiso sin consultar la tabla")
    void admin_siempreTienePermiso() {
        assertTrue(guard.can(auth("admin", "ADMIN"), "CATALOGS", "EDIT"));
        verify(permissionService, never()).hasPermissionByUsername(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Otros roles consultan el permiso granular del usuario autenticado")
    void tecnico_consultaPermisoGranular() {
        when(permissionService.hasPermissionByUsername("tecnico", "IMPORT", "CREATE")).thenReturn(true);
        when(permissionService.hasPermissionByUsername("tecnico", "CATALOGS", "EDIT")).thenReturn(false);

        assertTrue(guard.can(auth("tecnico", "TECHNICIAN"), "IMPORT", "CREATE"));
        assertFalse(guard.can(auth("tecnico", "TECHNICIAN"), "CATALOGS", "EDIT"));
    }

    @Test
    @DisplayName("Sin autenticacion o con datos incompletos se niega")
    void sinAutenticacion_niega() {
        assertFalse(guard.can(null, "AUDIT", "VIEW"));
        assertFalse(guard.can(auth("usuario", "USER"), null, "VIEW"));
        assertFalse(guard.can(auth("usuario", "USER"), "AUDIT", null));
    }

    @Test
    @DisplayName("Si la consulta falla se niega el acceso en vez de lanzar error")
    void errorAlConsultar_niega() {
        when(permissionService.hasPermissionByUsername("usuario", "RENTALS", "VIEW"))
            .thenThrow(new IllegalStateException("BD caida"));

        assertFalse(guard.can(auth("usuario", "USER"), "RENTALS", "VIEW"));
    }
}
