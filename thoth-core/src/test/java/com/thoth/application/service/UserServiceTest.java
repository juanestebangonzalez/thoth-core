package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.entity.UserEntity.UserRole;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserJpaRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PermissionService permissionService;

    @InjectMocks private UserService userService;

    private UserEntity testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = UserEntity.builder()
            .id(userId)
            .username("tecnico1")
            .email("tecnico1@hospital.com")
            .password("encoded")
            .role(UserRole.TECHNICIAN)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    // === listAll ===

    @Test
    void listAll_returnsAllUsers() {
        UserEntity admin = UserEntity.builder()
            .id(UUID.randomUUID()).username("admin").email("admin@hospital.com")
            .password("x").role(UserRole.ADMIN).enabled(true).createdAt(LocalDateTime.now())
            .build();
        when(userRepository.findAll()).thenReturn(List.of(testUser, admin));

        List<UserService.UserInfo> result = userService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("tecnico1");
        assertThat(result.get(1).role()).isEqualTo("ADMIN");
    }

    // === changeRole ===

    @Test
    void changeRole_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        UserService.UpdateResult result = userService.changeRole(userId, "ADMIN");

        assertThat(result.success()).isTrue();
        assertThat(result.message()).contains("ADMIN");
        verify(permissionService).assignDefaultPermissions(userId, "ADMIN");
    }

    @Test
    void changeRole_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserService.UpdateResult result = userService.changeRole(userId, "ADMIN");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("no encontrado");
    }

    @Test
    void changeRole_invalidRole() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserService.UpdateResult result = userService.changeRole(userId, "SUPERADMIN");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("invalido");
    }

    // === toggleEnabled ===

    @Test
    void toggleEnabled_disablesUser() {
        testUser.setEnabled(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        UserService.UpdateResult result = userService.toggleEnabled(userId);

        assertThat(result.success()).isTrue();
        assertThat(testUser.isEnabled()).isFalse();
    }

    @Test
    void toggleEnabled_enablesUser() {
        testUser.setEnabled(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        UserService.UpdateResult result = userService.toggleEnabled(userId);

        assertThat(result.success()).isTrue();
        assertThat(testUser.isEnabled()).isTrue();
    }

    @Test
    void toggleEnabled_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserService.UpdateResult result = userService.toggleEnabled(userId);

        assertThat(result.success()).isFalse();
    }

    // === resetPassword ===

    @Test
    void resetPassword_generatesTemporaryPassword() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_new");
        when(userRepository.save(any())).thenReturn(testUser);

        UserService.ResetPasswordResult result = userService.resetPassword(userId);

        assertThat(result.success()).isTrue();
        assertThat(result.newPassword()).isNotBlank().hasSize(12);
        assertThat(testUser.isPasswordChangeRequired()).isTrue();
        verify(passwordEncoder).encode(result.newPassword());
    }

    @Test
    void resetPassword_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserService.ResetPasswordResult result = userService.resetPassword(userId);

        assertThat(result.success()).isFalse();
        assertThat(result.newPassword()).isNull();
    }

    // === changeEmail ===

    @Test
    void changeEmail_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("nuevo@hospital.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(testUser);

        UserService.UpdateResult result = userService.changeEmail(userId, "nuevo@hospital.com");

        assertThat(result.success()).isTrue();
        assertThat(testUser.getEmail()).isEqualTo("nuevo@hospital.com");
    }

    @Test
    void changeEmail_duplicateEmail() {
        UserEntity otherUser = UserEntity.builder().id(UUID.randomUUID()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("duplicado@hospital.com")).thenReturn(Optional.of(otherUser));

        UserService.UpdateResult result = userService.changeEmail(userId, "duplicado@hospital.com");

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Ya existe");
    }

    @Test
    void changeEmail_blankEmail() {
        UserService.UpdateResult result = userService.changeEmail(userId, "  ");

        assertThat(result.success()).isFalse();
    }

    @Test
    void changeEmail_nullEmail() {
        UserService.UpdateResult result = userService.changeEmail(userId, null);

        assertThat(result.success()).isFalse();
    }

    // === listTechnicians ===

    @Test
    void listTechnicians_onlyEnabledTechnicians() {
        UserEntity disabledTech = UserEntity.builder()
            .id(UUID.randomUUID()).username("tech2").email("t2@h.com")
            .password("x").role(UserRole.TECHNICIAN).enabled(false).build();
        UserEntity admin = UserEntity.builder()
            .id(UUID.randomUUID()).username("admin").email("a@h.com")
            .password("x").role(UserRole.ADMIN).enabled(true).build();
        when(userRepository.findAll()).thenReturn(List.of(testUser, disabledTech, admin));

        List<UserService.TechnicianInfo> result = userService.listTechnicians();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("tecnico1");
    }
}
