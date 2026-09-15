package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.out.persistence.entity.SedeEntity;
import com.thoth.adapter.out.persistence.repository.SedeRepository;
import com.thoth.application.dto.SedeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/sedes")
@Tag(name = "Sedes", description = "Gestion de sedes del Instituto")
@RequiredArgsConstructor
public class SedeController {

    private final SedeRepository sedeRepository;

    @GetMapping
    @Operation(summary = "Listar todas las sedes activas")
    public ResponseEntity<List<SedeDTO>> listActive() {
        return ResponseEntity.ok(sedeRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(SedeController::toDTO)
            .toList());
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las sedes (incluyendo inactivas)")
    public ResponseEntity<List<SedeDTO>> listAll() {
        return ResponseEntity.ok(sedeRepository.findAll().stream()
            .map(SedeController::toDTO)
            .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sede por ID")
    public ResponseEntity<SedeDTO> getById(@PathVariable UUID id) {
        return sedeRepository.findById(id)
            .map(SedeController::toDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva sede")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
        }
        if (sedeRepository.existsByNameIgnoreCase(name.trim())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ya existe una sede con ese nombre"));
        }
        SedeEntity sede = SedeEntity.builder()
            .name(name.trim())
            .address(body.getOrDefault("address", ""))
            .phone(body.getOrDefault("phone", ""))
            .active(true)
            .build();
        return ResponseEntity.ok(toDTO(sedeRepository.save(sede)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sede")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return sedeRepository.findById(id).map(sede -> {
            String name = body.get("name");
            if (name != null && !name.isBlank()) {
                var existing = sedeRepository.findByNameIgnoreCase(name.trim());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body((Object) Map.of("message", "Ya existe otra sede con ese nombre"));
                }
                sede.setName(name.trim());
            }
            if (body.containsKey("address")) sede.setAddress(body.get("address"));
            if (body.containsKey("phone")) sede.setPhone(body.get("phone"));
            if (body.containsKey("active")) sede.setActive(Boolean.parseBoolean(body.get("active")));
            return ResponseEntity.ok((Object) toDTO(sedeRepository.save(sede)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar sede (soft delete)")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return sedeRepository.findById(id).map(sede -> {
            sede.setActive(false);
            sedeRepository.save(sede);
            return ResponseEntity.ok(Map.of("message", "Sede desactivada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private static SedeDTO toDTO(SedeEntity entity) {
        return new SedeDTO(
            entity.getId(),
            entity.getName(),
            entity.getAddress(),
            entity.getPhone(),
            Boolean.TRUE.equals(entity.getActive())
        );
    }
}