package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.PasswordResetTokenEntity;
import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.PasswordResetTokenRepository;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);
    private static final String TOKEN = "token-de-prueba-123";

    private UserJpaRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuditService auditService;
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    private JavaMailSender mailSender;
    private UserEntity user;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        userRepository = mock(UserJpaRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditService = mock(AuditService.class);
        mailSenderProvider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        user = UserEntity.builder()
            .id(UUID.randomUUID())
            .username("jdoe")
            .email("jdoe@hospital.co")
            .password("HASH-VIEJO")
            .role(UserEntity.UserRole.USER)
            .enabled(true)
            .build();
        when(userRepository.findByUsername("jdoe")).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of());
    }

    private PasswordResetService service(boolean mailEnabled) {
        PasswordResetService s = new PasswordResetService(userRepository, tokenRepository, passwordEncoder,
            auditService, mailSenderProvider, mailEnabled, "no-responder@hospital.co", "https://thoth.example/");
        s.setClock(Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC));
        return s;
    }

    private PasswordResetTokenEntity tokenEntity(LocalDateTime expiresAt, LocalDateTime usedAt) {
        return PasswordResetTokenEntity.builder()
            .id(UUID.randomUUID())
            .userId(user.getId())
            .tokenHash(PasswordResetService.hash(TOKEN))
            .expiresAt(expiresAt)
            .usedAt(usedAt)
            .createdAt(NOW.minusMinutes(5))
            .build();
    }

    @Test
    @DisplayName("Con el correo deshabilitado no se genera token ni se envia correo")
    void requestReset_correoDeshabilitado_noEnvia() {
        boolean enviado = service(false).requestReset("jdoe", "jdoe@hospital.co");

        assertFalse(enviado);
        verify(tokenRepository, never()).save(any(PasswordResetTokenEntity.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Datos correctos: guarda solo el hash, vence en 30 minutos y envia el enlace")
    void requestReset_datosCorrectos_guardaHashYEnviaCorreo() {
        boolean enviado = service(true).requestReset("jdoe", "JDOE@hospital.co");
        assertTrue(enviado);

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetTokenEntity guardado = tokenCaptor.getValue();
        assertEquals(user.getId(), guardado.getUserId());
        assertEquals(64, guardado.getTokenHash().length());
        assertEquals(NOW.plusMinutes(30), guardado.getExpiresAt());
        assertNull(guardado.getUsedAt());

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage mensaje = mailCaptor.getValue();
        assertEquals("THOTH C.O.R.E. - Restablecer contrasena", mensaje.getSubject());
        assertEquals("jdoe@hospital.co", mensaje.getTo()[0]);

        String texto = mensaje.getText();
        String prefijo = "https://thoth.example/reset-password?token=";
        int inicio = texto.indexOf(prefijo);
        assertTrue(inicio >= 0, "El correo debe incluir el enlace de restablecimiento");
        String token = texto.substring(inicio + prefijo.length()).split("\\s")[0];
        // El token enviado corresponde al hash guardado (nunca se guarda en claro)
        assertEquals(guardado.getTokenHash(), PasswordResetService.hash(token));
        assertTrue(texto.contains("30 minutos"));
    }

    @Test
    @DisplayName("Correo que no coincide: no se envia nada")
    void requestReset_correoNoCoincide_noEnvia() {
        boolean enviado = service(true).requestReset("jdoe", "otro@hospital.co");

        assertFalse(enviado);
        verify(tokenRepository, never()).save(any(PasswordResetTokenEntity.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Token valido: cambia la contrasena, marca el token como usado y audita")
    void resetPassword_tokenValido_cambiaContrasena() {
        PasswordResetTokenEntity token = tokenEntity(NOW.plusMinutes(10), null);
        when(tokenRepository.findByTokenHash(PasswordResetService.hash(TOKEN))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("HASH-NUEVO");

        PasswordResetService.ResetResult result = service(true).resetPassword(TOKEN, "NuevaClave123");

        assertTrue(result.success());
        assertEquals("Contrasena actualizada correctamente", result.message());
        assertEquals("HASH-NUEVO", user.getPassword());
        assertFalse(user.isPasswordChangeRequired());
        assertNotNull(token.getUsedAt());
        verify(userRepository).save(user);
        verify(auditService).log(eq("PASSWORD_RESET"), eq("AUTH"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Token vencido: no cambia la contrasena")
    void resetPassword_tokenVencido_falla() {
        PasswordResetTokenEntity token = tokenEntity(NOW.minusMinutes(1), null);
        when(tokenRepository.findByTokenHash(PasswordResetService.hash(TOKEN))).thenReturn(Optional.of(token));

        PasswordResetService.ResetResult result = service(true).resetPassword(TOKEN, "NuevaClave123");

        assertFalse(result.success());
        assertEquals(PasswordResetService.MSG_INVALID_TOKEN, result.message());
        assertEquals("HASH-VIEJO", user.getPassword());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Token ya usado: no cambia la contrasena")
    void resetPassword_tokenUsado_falla() {
        PasswordResetTokenEntity token = tokenEntity(NOW.plusMinutes(10), NOW.minusMinutes(2));
        when(tokenRepository.findByTokenHash(PasswordResetService.hash(TOKEN))).thenReturn(Optional.of(token));

        PasswordResetService.ResetResult result = service(true).resetPassword(TOKEN, "NuevaClave123");

        assertFalse(result.success());
        assertEquals("HASH-VIEJO", user.getPassword());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Token inexistente: no es valido")
    void resetPassword_tokenInexistente_falla() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        PasswordResetService s = service(true);
        assertFalse(s.isTokenValid("no-existe"));
        assertFalse(s.resetPassword("no-existe", "NuevaClave123").success());
    }

    @Test
    @DisplayName("Validar token: true solo si esta vigente y sin usar")
    void isTokenValid_segunVigencia() {
        PasswordResetTokenEntity vigente = tokenEntity(NOW.plusMinutes(10), null);
        when(tokenRepository.findByTokenHash(PasswordResetService.hash(TOKEN))).thenReturn(Optional.of(vigente));
        assertTrue(service(true).isTokenValid(TOKEN));

        vigente.setUsedAt(NOW);
        assertFalse(service(true).isTokenValid(TOKEN));
        assertFalse(service(true).isTokenValid(null));
        assertFalse(service(true).isTokenValid(""));
    }
}
