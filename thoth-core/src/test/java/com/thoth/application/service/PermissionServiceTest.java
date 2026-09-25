package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.entity.UserPermissionEntity;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import com.thoth.adapter.out.persistence.repository.UserPermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock private UserPermissionRepository permissionRepository;
    @Mock private UserJpaRepository userRepository;

    @InjectMocks private PermissionService permissionService;

    private final UUID userId = UUID.randomUUID();

    // === getUserPermissions ===

    @Test
    void getUserPermissions_returnsPermissionsGroupedByModule() {
        List<UserPermissionEntity> perms = List.of(
            UserPermissionEntity.builder().userId(userId).module("EQUIPMENT").action("VIEW").build(),
            UserPermissionEntity.builder().userId(userId).module("EQUIPMENT").action("CREATE").build(),
            UserPermissionEntity.builder().userId(userId).module("REPORTS").action("VIEW").build()
        );
        when(permissionRepository.findByUserId(userId)).thenReturn(perms);

        Map<String, List<String>> result = permissionService.getUserPermissions(userId);

        assertThat(result.get("EQUIPMENT")).containsExactly("VIEW", "CREATE");
        assertThat(result.get("REPORTS")).containsExactly("VIEW");
        assertThat(result.get("USERS")).isEmpty();
    }

    // === hasPermission ===

    @Test
    void hasPermission_returnsTrueWhenExists() {
        when(permissionRepository.existsByUserIdAndModuleAndAction(userId, "EQUIPMENT", "VIEW"))
            .thenReturn(true);

        assertThat(permissionService.hasPermission(userId, "equipment", "view")).isTrue();
    }

    @Test
    void hasPermission_returnsFalseWhenNotExists() {
        when(permissionRepository.existsByUserIdAndModuleAndAction(userId, "USERS", "DELETE"))
            .thenReturn(false);

        assertThat(permissionService.hasPermission(userId, "users", "delete")).isFalse();
    }

    // === requireModulePermission ===

    @Test
    void requireModulePermission_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.requireModulePermission("ghost", "EQUIPMENT", "VIEW"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("no encontrado");
    }

    @Test
    void requireModulePermission_throwsWhenNoPermission() {
        UserEntity user = UserEntity.builder().id(userId).username("viewer").build();
        when(userRepository.findByUsername("viewer")).thenReturn(Optional.of(user));
        when(permissionRepository.existsByUserIdAndModuleAndAction(userId, "USERS", "DELETE"))
            .thenReturn(false);

        assertThatThrownBy(() -> permissionService.requireModulePermission("viewer", "USERS", "DELETE"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("DELETE");
    }

    @Test
    void requireModulePermission_passesWhenHasPermission() {
        UserEntity user = UserEntity.builder().id(userId).username("admin").build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(permissionRepository.existsByUserIdAndModuleAndAction(userId, "EQUIPMENT", "VIEW"))
            .thenReturn(true);

        // Should not throw
        permissionService.requireModulePermission("admin", "EQUIPMENT", "VIEW");
    }

    // === getDefaultPermissions ===

    @Test
    void getDefaultPermissions_adminHasAllPermissions() {
        Map<String, List<String>> perms = permissionService.getDefaultPermissions("ADMIN");

        for (String module : PermissionService.ALL_MODULES) {
            assertThat(perms.get(module)).containsExactlyInAnyOrderElementsOf(PermissionService.ALL_ACTIONS);
        }
    }

    @Test
    void getDefaultPermissions_viewerHasLimitedAccess() {
        Map<String, List<String>> perms = permissionService.getDefaultPermissions("VIEWER");

        assertThat(perms.get("EQUIPMENT")).containsExactly("VIEW");
        assertThat(perms.get("MAINTENANCE")).isEmpty();
        assertThat(perms.get("USERS")).isEmpty();
    }

    @Test
    void getDefaultPermissions_unknownRoleGetsNothing() {
        Map<String, List<String>> perms = permissionService.getDefaultPermissions("UNKNOWN");

        for (String module : PermissionService.ALL_MODULES) {
            assertThat(perms.get(module)).isEmpty();
        }
    }

    // === setUserPermissions ===

    @Test
    void setUserPermissions_savesValidPermissions() {
        when(permissionRepository.findByUserId(userId)).thenReturn(List.of());
        Map<String, List<String>> input = Map.of(
            "EQUIPMENT", List.of("VIEW", "CREATE"),
            "INVALID_MODULE", List.of("VIEW")
        );

        permissionService.setUserPermissions(userId, input);

        // Should save 2 valid permissions, ignoring INVALID_MODULE
        verify(permissionRepository).saveAll(argThat(list ->
            ((List<?>) list).size() == 2));
    }

    @Test
    void setUserPermissions_deletesExistingFirst() {
        List<UserPermissionEntity> existing = List.of(
            UserPermissionEntity.builder().userId(userId).module("REPORTS").action("VIEW").build()
        );
        when(permissionRepository.findByUserId(userId)).thenReturn(existing);

        permissionService.setUserPermissions(userId, Map.of("EQUIPMENT", List.of("VIEW")));

        verify(permissionRepository).deleteAll(existing);
        verify(permissionRepository).flush();
    }
}
