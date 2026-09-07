package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.request.ChangeStatusRequest;
import com.thoth.adapter.in.rest.dto.request.CreateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.request.UpdateEquipmentRequest;
import com.thoth.adapter.in.rest.dto.response.EquipmentResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Equipment", description = "Equipment Management API")
@RequiredArgsConstructor
public class EquipmentController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Equipment", description = "Register a new equipment")
    public ResponseEntity<EquipmentResponseDTO> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get Equipment", description = "Retrieve equipment by ID")
    public ResponseEntity<EquipmentResponseDTO> getEquipment(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List Equipment", description = "Get all equipment with pagination")
    public ResponseEntity<?> listEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update Equipment", description = "Update equipment information")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request) {
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change Equipment Status", description = "Update equipment status")
    public ResponseEntity<EquipmentResponseDTO> changeEquipmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Equipment", description = "Soft delete equipment")
    public void deleteEquipment(@PathVariable UUID id) {
    }
}