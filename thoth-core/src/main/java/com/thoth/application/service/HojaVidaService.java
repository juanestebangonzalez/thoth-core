package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AuditLogEntity;
import com.thoth.adapter.out.persistence.entity.DocumentEntity;
import com.thoth.adapter.out.persistence.entity.LocationHistoryEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.entity.PartReplacedEntity;
import com.thoth.adapter.out.persistence.repository.AuditLogRepository;
import com.thoth.adapter.out.persistence.repository.DocumentRepository;
import com.thoth.adapter.out.persistence.repository.LocationHistoryRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceHistoryJpaRepository;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.port.input.GetEquipmentUseCase;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hoja de vida del equipo: datos completos, perifericos, monitores, mantenimientos (con partes),
 * traslados, documentos y baja. Respuesta de solo lectura armada como mapa JSON.
 */
@Service
@RequiredArgsConstructor
public class HojaVidaService {

    private static final String MOTIVO = "Motivo:";

    private final GetEquipmentUseCase getEquipmentUseCase;
    private final PeripheralService peripheralService;
    private final EquipmentMonitorService equipmentMonitorService;
    private final MaintenanceHistoryJpaRepository maintenanceRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final EquipmentRepositoryPort equipmentRepositoryPort;

    @Transactional(readOnly = true)
    public Map<String, Object> build(UUID equipmentId) {
        // Lanza EquipmentNotFoundException (404) si no existe
        EquipmentDTO equipment = getEquipmentUseCase.getById(equipmentId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generatedAt", LocalDateTime.now());
        result.put("equipment", equipment);
        // Fecha de registro del equipo (no viaja en EquipmentDTO)
        result.put("registeredAt", equipmentRepositoryPort.findById(equipmentId)
            .map(eq -> eq.getCreatedAt()).orElse(null));
        result.put("peripherals", peripheralService.listUnchecked(equipmentId));
        result.put("monitors", equipmentMonitorService.listMonitors(equipmentId));
        result.put("maintenances", maintenances(equipmentId));
        result.put("transfers", transfers(equipmentId));
        result.put("documents", documents(equipmentId));
        result.put("baja", "RETIRED".equals(equipment.status()) ? baja(equipmentId) : null);
        return result;
    }

    private List<Map<String, Object>> maintenances(UUID equipmentId) {
        List<MaintenanceHistoryEntity> list = new ArrayList<>(
            maintenanceRepository.findByEquipmentIdOrderByPerformedDateDesc(equipmentId));
        list.sort(Comparator.comparing(MaintenanceHistoryEntity::getPerformedDate,
            Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MaintenanceHistoryEntity m : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("performedDate", m.getPerformedDate());
            item.put("maintenanceType", m.getMaintenanceType());
            item.put("reason", m.getReason());
            item.put("description", m.getDescription());
            item.put("technicianName", m.getTechnicianName());
            item.put("signedBy", m.getSignedBy());
            List<Map<String, Object>> parts = new ArrayList<>();
            if (m.getPartsReplaced() != null) {
                for (PartReplacedEntity p : m.getPartsReplaced()) {
                    Map<String, Object> part = new LinkedHashMap<>();
                    part.put("partName", p.getPartName());
                    part.put("partSerialNumber", p.getPartSerialNumber());
                    part.put("reason", p.getReason());
                    parts.add(part);
                }
            }
            item.put("parts", parts);
            out.add(item);
        }
        return out;
    }

    private List<Map<String, Object>> transfers(UUID equipmentId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (LocationHistoryEntity h : locationHistoryRepository.findByEquipmentIdOrderByTransferredAtDesc(equipmentId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", h.getTransferredAt());
            item.put("fromBuilding", h.getFromBuilding());
            item.put("fromOffice", h.getFromOffice());
            item.put("toBuilding", h.getToBuilding());
            item.put("toOffice", h.getToOffice());
            item.put("reason", h.getReason());
            item.put("performedBy", h.getPerformedBy());
            out.add(item);
        }
        return out;
    }

    private List<Map<String, Object>> documents(UUID equipmentId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (DocumentEntity d : documentRepository.findByEquipmentIdOrderByUploadedAtDesc(equipmentId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            // Nombre visible: el nombre original del archivo (file_name es el nombre interno en disco)
            item.put("fileName", d.getOriginalName() != null ? d.getOriginalName() : d.getFileName());
            item.put("documentType", d.getDocumentType());
            item.put("uploadedAt", d.getUploadedAt());
            item.put("uploadedBy", d.getUploadedBy());
            out.add(item);
        }
        return out;
    }

    /** Ultimo registro de baja en la auditoria; el motivo se toma del texto "Motivo: ..." si existe. */
    private Map<String, Object> baja(UUID equipmentId) {
        List<AuditLogEntity> entries = auditLogRepository.findRetirementEntries(equipmentId.toString());
        Map<String, Object> baja = new LinkedHashMap<>();
        if (entries.isEmpty()) {
            baja.put("date", null);
            baja.put("reason", null);
            return baja;
        }
        // Preferir el cambio de estado con motivo; si no, el registro mas reciente
        AuditLogEntity chosen = entries.get(0);
        String reason = null;
        for (AuditLogEntity e : entries) {
            String r = extractReason(e.getDetails());
            if (r != null) {
                chosen = e;
                reason = r;
                break;
            }
        }
        baja.put("date", chosen.getPerformedAt());
        baja.put("reason", reason);
        return baja;
    }

    static String extractReason(String details) {
        if (details == null) return null;
        int idx = details.indexOf(MOTIVO);
        if (idx < 0) return null;
        String r = unescape(details.substring(idx + MOTIVO.length()).trim());
        return r.isEmpty() ? null : r;
    }

    /** La auditoria guarda el texto escapado (SEC-016); se revierte para mostrarlo. */
    private static String unescape(String s) {
        return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
            .replace("&#39;", "'").replace("&amp;", "&");
    }
}
