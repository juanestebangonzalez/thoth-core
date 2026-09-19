package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import com.thoth.adapter.out.persistence.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * SEC-016 fix: sanitize user-supplied text before storing in audit log
     * to prevent stored XSS when audit entries are rendered in a web UI.
     */
    private String sanitize(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#39;");
    }

    public void log(String action, String module, String entityId, String entityName,
                    String details, String performedBy, String ipAddress) {
        AuditLogEntity entry = AuditLogEntity.builder()
                .action(action)
                .module(module)
                .entityId(entityId)
                .entityName(sanitize(entityName))
                .details(sanitize(details))
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(entry);
    }

    public void log(String action, String module, String entityId, String entityName,
                    String details, String performedBy) {
        log(action, module, entityId, entityName, details, performedBy, null);
    }

    public Page<AuditLogEntity> getAll(int page, int size) {
        return auditLogRepository.findAllByOrderByPerformedAtDesc(PageRequest.of(page, size));
    }

    public Page<AuditLogEntity> getFiltered(String module, String user, String action, int page, int size) {
        return auditLogRepository.findFiltered(
                module != null && module.isBlank() ? null : module,
                user != null && user.isBlank() ? null : user,
                action != null && action.isBlank() ? null : action,
                PageRequest.of(page, size)
        );
    }
}
