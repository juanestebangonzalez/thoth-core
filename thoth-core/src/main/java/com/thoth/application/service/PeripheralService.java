package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.EquipmentPeripheralEntity;
import com.thoth.adapter.out.persistence.entity.PeripheralTypeEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentPeripheralRepository;
import com.thoth.adapter.out.persistence.repository.PeripheralTypeRepository;
import com.thoth.application.dto.PeripheralDTO;
import com.thoth.application.exception.EquipmentNotFoundException;
import com.thoth.application.exception.NotFoundException;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Perifericos de los equipos (teclado, mouse, diadema...). El tipo debe existir en el catalogo activo. */
@Service
@RequiredArgsConstructor
public class PeripheralService {

    private static final int MAX_LENGTH = 100;

    private final EquipmentPeripheralRepository peripheralRepository;
    private final PeripheralTypeRepository peripheralTypeRepository;
    private final EquipmentRepositoryPort equipmentRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PeripheralDTO> list(UUID equipmentId) {
        requireEquipment(equipmentId);
        return listUnchecked(equipmentId);
    }

    /** Perifericos sin validar que el equipo exista (uso interno, p. ej. hoja de vida). */
    @Transactional(readOnly = true)
    public List<PeripheralDTO> listUnchecked(UUID equipmentId) {
        return peripheralRepository.findByEquipmentIdOrderByCreatedAtAsc(equipmentId).stream()
            .map(PeripheralService::toDTO)
            .toList();
    }

    @Transactional
    public PeripheralDTO create(UUID equipmentId, String type, String brand, String user) {
        Equipment equipment = requireEquipment(equipmentId);
        String normalizedType = resolveType(type);
        String normalizedBrand = normalizeBrand(brand);
        EquipmentPeripheralEntity saved = peripheralRepository.save(EquipmentPeripheralEntity.builder()
            .equipmentId(equipmentId)
            .type(normalizedType)
            .brand(normalizedBrand)
            .createdAt(LocalDateTime.now())
            .createdBy(user(user))
            .build());
        auditService.log("CREATE", "PERIPHERAL", saved.getId().toString(), equipment.getName(),
            "Periferico agregado al equipo " + equipment.getName() + ": " + describe(normalizedType, normalizedBrand),
            user(user));
        return toDTO(saved);
    }

    @Transactional
    public PeripheralDTO update(UUID equipmentId, UUID peripheralId, String type, String brand, String user) {
        Equipment equipment = requireEquipment(equipmentId);
        EquipmentPeripheralEntity peripheral = requirePeripheral(equipmentId, peripheralId);
        String before = describe(peripheral.getType(), peripheral.getBrand());
        peripheral.setType(resolveType(type));
        peripheral.setBrand(normalizeBrand(brand));
        EquipmentPeripheralEntity saved = peripheralRepository.save(peripheral);
        auditService.log("UPDATE", "PERIPHERAL", peripheralId.toString(), equipment.getName(),
            "Periferico actualizado en el equipo " + equipment.getName() + ": " + before + " -> "
                + describe(saved.getType(), saved.getBrand()), user(user));
        return toDTO(saved);
    }

    @Transactional
    public void delete(UUID equipmentId, UUID peripheralId, String user) {
        Equipment equipment = requireEquipment(equipmentId);
        EquipmentPeripheralEntity peripheral = requirePeripheral(equipmentId, peripheralId);
        peripheralRepository.delete(peripheral);
        auditService.log("DELETE", "PERIPHERAL", peripheralId.toString(), equipment.getName(),
            "Periferico eliminado del equipo " + equipment.getName() + ": "
                + describe(peripheral.getType(), peripheral.getBrand()), user(user));
    }

    // ------------------------------------------------------------------ utilidades

    private Equipment requireEquipment(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new EquipmentNotFoundException(equipmentId));
    }

    private EquipmentPeripheralEntity requirePeripheral(UUID equipmentId, UUID peripheralId) {
        return peripheralRepository.findById(peripheralId)
            .filter(p -> equipmentId.equals(p.getEquipmentId()))
            .orElseThrow(() -> new NotFoundException("Periferico no encontrado: " + peripheralId));
    }

    /** El tipo debe existir en el catalogo activo de tipos de periferico (se guarda en mayusculas). */
    String resolveType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("El tipo de periferico es obligatorio");
        }
        String name = type.trim().toUpperCase(Locale.ROOT);
        PeripheralTypeEntity catalog = peripheralTypeRepository.findByNameIgnoreCase(name)
            .filter(t -> Boolean.TRUE.equals(t.getActive()))
            .orElseThrow(() -> new IllegalArgumentException(
                "El tipo de periferico '" + name + "' no existe en el catalogo o esta inactivo"));
        return catalog.getName().trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeBrand(String brand) {
        if (brand == null || brand.isBlank()) return null;
        String b = brand.trim().toUpperCase(Locale.ROOT);
        if (b.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("La marca no puede exceder " + MAX_LENGTH + " caracteres");
        }
        return b;
    }

    private static String describe(String type, String brand) {
        return type + (brand != null && !brand.isBlank() ? " (" + brand + ")" : "");
    }

    private static String user(String user) {
        return user != null && !user.isBlank() ? user : "SYSTEM";
    }

    public static PeripheralDTO toDTO(EquipmentPeripheralEntity e) {
        return new PeripheralDTO(e.getId(), e.getType(), e.getBrand(), e.getCreatedAt(), e.getCreatedBy());
    }
}
