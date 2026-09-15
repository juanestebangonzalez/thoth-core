package com.thoth.adapter.in.rest.controller;

import com.thoth.adapter.in.rest.dto.response.AIAnalysisResponse;
import com.thoth.application.port.input.GetEquipmentUseCase;
import com.thoth.application.port.output.AIAgentPort;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentCategory;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Analysis", description = "AI-powered equipment analysis")
@RequiredArgsConstructor
public class AIController {

    private final AIAgentPort aiAgentPort;
    private final GetEquipmentUseCase getEquipmentUseCase;

    @GetMapping("/maintenance/{equipmentId}")
    @Operation(summary = "Analyze Maintenance", description = "AI maintenance analysis for equipment")
    public ResponseEntity<AIAnalysisResponse> analyzeMaintenance(@PathVariable UUID equipmentId) {
        EquipmentDTO dto = getEquipmentUseCase.getById(equipmentId);
        Equipment equipment = mapToEquipment(dto);
        String result = aiAgentPort.analyzeMaintenance(equipment);
        return ResponseEntity.ok(buildResponse(equipmentId, dto.name(), "MAINTENANCE_ANALYSIS", result));
    }

    @GetMapping("/predict-failure/{equipmentId}")
    @Operation(summary = "Predict Failure", description = "AI failure prediction for equipment")
    public ResponseEntity<AIAnalysisResponse> predictFailure(@PathVariable UUID equipmentId) {
        EquipmentDTO dto = getEquipmentUseCase.getById(equipmentId);
        Equipment equipment = mapToEquipment(dto);
        String result = aiAgentPort.predictFailure(equipment);
        return ResponseEntity.ok(buildResponse(equipmentId, dto.name(), "FAILURE_PREDICTION", result));
    }

    @GetMapping("/recommend-replacement/{equipmentId}")
    @Operation(summary = "Recommend Replacement", description = "AI replacement recommendation")
    public ResponseEntity<AIAnalysisResponse> recommendReplacement(@PathVariable UUID equipmentId) {
        EquipmentDTO dto = getEquipmentUseCase.getById(equipmentId);
        Equipment equipment = mapToEquipment(dto);
        String result = aiAgentPort.recommendReplacement(equipment);
        return ResponseEntity.ok(buildResponse(equipmentId, dto.name(), "REPLACEMENT_RECOMMENDATION", result));
    }

    private Equipment mapToEquipment(EquipmentDTO dto) {
        EquipmentCategory category;
        try {
            category = EquipmentCategory.valueOf(dto.category());
        } catch (Exception e) {
            category = EquipmentCategory.LAPTOP;
        }
        EquipmentStatus status;
        try {
            status = EquipmentStatus.valueOf(dto.status());
        } catch (Exception e) {
            status = EquipmentStatus.ACTIVE;
        }
        return Equipment.builder()
            .equipmentId(dto.equipmentId())
            .name(dto.name())
            .category(category)
            .serialNumber(dto.serialNumber())
            .brand(dto.brand())
            .model(dto.model())
            .status(status)
            .purchaseDate(dto.purchaseDate())
            .purchaseValue(dto.purchaseValue())
            .location(Location.of(
                dto.location() != null ? dto.location().building() : "",
                dto.location() != null ? dto.location().floor() : "",
                dto.location() != null ? dto.location().office() : "",
                ""))
            .assignedTo(dto.assignedTo())
            .build();
    }

    private AIAnalysisResponse buildResponse(UUID id, String name, String type, String result) {
        return AIAnalysisResponse.builder()
            .equipmentId(id)
            .equipmentName(name)
            .analysisType(type)
            .result(result)
            .analyzedAt(LocalDateTime.now())
            .build();
    }
}