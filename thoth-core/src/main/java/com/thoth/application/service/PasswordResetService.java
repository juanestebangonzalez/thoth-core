package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.PasswordResetTokenEntity;
import com.thoth.adapter.out.persistence.entity.UserEntity;
import com.thoth.adapter.out.persistence.repository.PasswordResetTokenRepository;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Recuperacion de contrasena por correo electronico.
 *
 * - Genera un token aleatorio de 32 bytes (Base64 URL), guarda solo su hash SHA-256 y lo envia por correo.
 * - El token vence en {@link #TOKEN_VALIDITY_MINUTES} minutos y es de un solo uso.
 * - Si thoth.mail.enabled=false no se envia nada (se conserva el flujo de auditoria para el administrador).
 * - JavaMailSender se obtiene con ObjectProvider para que la aplicacion arranque sin SMTP configurado.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    public static final int TOKEN_VALIDITY_MINUTES = 30;
    public static final String MAIL_SUBJECT = "THOTH C.O.R.E. - Restablecer contrasena";
    public static final String MSG_SUCCESS = "Contrasena actualizada correctamente";
    public static final String MSG_INVALID_TOKEN = "El enlace de restablecimiento no es valido, ya fue usado o vencio. Solicite uno nuevo.";

    private static final int TOKEN_BYTES = 32;

    private final UserJpaRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean mailEnabled;
    private final String mailFrom;
    private final String frontendUrl;
    private final SecureRandom secureRandom = new SecureRandom();
    private Clock clock = Clock.systemDefaultZone();

    public PasswordResetService(UserJpaRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                AuditService auditService,
                                ObjectProvider<JavaMailSender> mailSenderProvider,
                                @Value("${thoth.mail.enabled:false}") boolean mailEnabled,
                                @Value("${thoth.mail.from:}") String mailFrom,
                                @Value("${thoth.frontend-url:https://thoth-core.netlify.app}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.mailSenderProvider = mailSenderProvider;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl;
    }

    /** Solo para pruebas: permite fijar el reloj. */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public record ResetResult(boolean success, String message) {}

    /**
     * Si el usuario existe, el correo coincide, esta habilitado y el correo esta activo, genera un token
     * y lo envia. Nunca revela si el usuario existe: el llamador responde siempre el mismo mensaje.
     *
     * @return true si se envio (o intento enviar) el correo.
     */
    @Transactional
    public boolean requestReset(String username, String email) {
        if (!mailEnabled || username == null || email == null) return false;

        Optional<UserEntity> optUser = userRepository.findByUsername(username.trim());
        if (optUser.isEmpty()) return false;
        UserEntity user = optUser.get();
        if (!user.isEnabled() || user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email.trim())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        invalidatePendingTokens(user, now);

        String token = generateToken();
        tokenRepository.save(PasswordResetTokenEntity.builder()
            .userId(user.getId())
            .tokenHash(hash(token))
            .expiresAt(now.plusMinutes(TOKEN_VALIDITY_MINUTES))
            .createdAt(now)
            .build());

        try {
            sendMail(user, token);
            auditService.log("PASSWORD_RESET_REQUEST", "AUTH", user.getId().toString(), user.getUsername(),
                "Se envio enlace de restablecimiento de contrasena a " + user.getEmail(), "system");
        } catch (RuntimeException e) {
            // No se revela el error al cliente; queda en el log y en la auditoria
            log.warn("No se pudo enviar el correo de restablecimiento al usuario {}: {}", user.getUsername(), e.getMessage());
            auditService.log("PASSWORD_RESET_REQUEST", "AUTH", user.getId().toString(), user.getUsername(),
                "Fallo el envio del correo de restablecimiento de contrasena", "system");
        }
        return true;
    }

    /** true si el token existe, no se ha usado, no ha vencido y su usuario esta habilitado. */
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return findUsableToken(token)
            .flatMap(t -> userRepository.findById(t.getUserId()))
            .map(UserEntity::isEnabled)
            .orElse(false);
    }

    /**
     * Cambia la contrasena usando el token. La nueva contrasena ya debe cumplir la politica
     * (la valida el controlador con PasswordPolicy).
     */
    @Transactional
    public ResetResult resetPassword(String token, String newPassword) {
        LocalDateTime now = LocalDateTime.now(clock);
        Optional<PasswordResetTokenEntity> optToken = findUsableToken(token);
        if (optToken.isEmpty()) {
            return new ResetResult(false, MSG_INVALID_TOKEN);
        }
        PasswordResetTokenEntity resetToken = optToken.get();
        Optional<UserEntity> optUser = userRepository.findById(resetToken.getUserId());
        if (optUser.isEmpty() || !optUser.get().isEnabled()) {
            return new ResetResult(false, MSG_INVALID_TOKEN);
        }
        if (newPassword == null || newPassword.isBlank()) {
            return new ResetResult(false, "La nueva contrasena es obligatoria");
        }

        UserEntity user = optUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        user.setUpdatedAt(now);
        userRepository.save(user);

        resetToken.setUsedAt(now);
        tokenRepository.save(resetToken);
        invalidatePendingTokens(user, now);

        // No existe lista de revocacion de JWT: los tokens emitidos vencen por su expiracion corta.
        auditService.log("PASSWORD_RESET", "AUTH", user.getId().toString(), user.getUsername(),
            "Contrasena restablecida mediante enlace enviado por correo", user.getUsername());
        return new ResetResult(true, MSG_SUCCESS);
    }

    // ------------------------------------------------------------------ utilidades

    private Optional<PasswordResetTokenEntity> findUsableToken(String token) {
        if (token == null || token.isBlank() || token.length() > 200) return Optional.empty();
        LocalDateTime now = LocalDateTime.now(clock);
        return tokenRepository.findByTokenHash(hash(token.trim()))
            .filter(t -> t.isUsable(now));
    }

    private void invalidatePendingTokens(UserEntity user, LocalDateTime now) {
        List<PasswordResetTokenEntity> pending = tokenRepository.findByUserIdAndUsedAtIsNull(user.getId());
        if (pending == null || pending.isEmpty()) return;
        for (PasswordResetTokenEntity t : pending) {
            t.setUsedAt(now);
        }
        tokenRepository.saveAll(pending);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private void sendMail(UserEntity user, String token) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("No hay servidor de correo configurado (MAIL_HOST)");
        }
        String base = frontendUrl == null ? "" : frontendUrl.trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String link = base + "/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom.trim());
        }
        message.setTo(user.getEmail());
        message.setSubject(MAIL_SUBJECT);
        message.setText("Hola " + user.getUsername() + ",\n\n"
            + "Recibimos una solicitud para restablecer la contrasena de tu cuenta en THOTH C.O.R.E.\n\n"
            + "Para crear una nueva contrasena abre el siguiente enlace:\n"
            + link + "\n\n"
            + "Este enlace vence en " + TOKEN_VALIDITY_MINUTES + " minutos y solo puede usarse una vez.\n\n"
            + "Si no solicitaste este cambio, ignora este mensaje: tu contrasena actual seguira funcionando.\n\n"
            + "THOTH C.O.R.E. - Control, Ordenamiento, Registro de Equipos");
        sender.send(message);
    }
}
