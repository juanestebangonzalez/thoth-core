package com.thoth.application.usecase;

import com.thoth.application.command.UpdateEquipmentCommand;
import com.thoth.application.dto.EquipmentResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.UpdateEquipmentUseCaseImpl;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateEquipmentUseCase - licenciamiento del SO")
class UpdateEquipmentOsLicensingTest {

    @Mock
    private EquipmentRepositoryPort equipmentRepository;

    private UpdateEquipmentUseCaseImpl useCase;
    private Equipment equipment;
    private UUID id;

    @BeforeEach
    void setUp() {
        useCase = new UpdateEquipmentUseCaseImpl(equipmentRepository, new EquipmentDtoMapper());
        id = UUID.randomUUID();
        equipment = Equipment.create("PC", "DESKTOP", "SN-1", "DELL", "OPTIPLEX", null,
            LocalDate.of(2024, 1, 1), BigDecimal.ONE, Location.of("A", "1", "101", ""), "JUAN", "admin");
        equipment.setEquipmentId(id);
        lenient().when(equipmentRepository.findById(id)).thenReturn(Optional.of(equipment));
        lenient().when(equipmentRepository.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private UpdateEquipmentCommand cmd(String os, String version, String edition, String license) {
        return new UpdateEquipmentCommand(id, null, null, null, null, null, null, "admin",
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, os, version,
            null, null, null, null, null, null, null, edition, license);
    }

    @Test
    void guardaEdicionVersionYLicencia() {
        EquipmentResponseDTO r = useCase.update(cmd("windows", "25h2", "windows 11", "retail"));
        assertEquals("WINDOWS 11", r.osEdition());
        assertEquals("25H2", r.osVersion());
        assertEquals("RETAIL", r.osLicenseType());
    }

    @Test
    void versionWindowsFueraDeCatalogoFalla() {
        assertThrows(IllegalArgumentException.class, () -> useCase.update(cmd("WINDOWS", "22H2", null, null)));
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void versionAntiguaSinCambiosNoSeValida() {
        equipment.updateOperatingSystem("WINDOWS");
        equipment.updateOsVersion("WINDOWS 10 PRO");
        EquipmentResponseDTO r = useCase.update(cmd("WINDOWS", "windows 10 pro", "WINDOWS 10", "OEM"));
        assertEquals("WINDOWS 10 PRO", r.osVersion());
        assertEquals("WINDOWS 10", r.osEdition());
    }

    @Test
    void edicionConSistemaNoWindowsFalla() {
        assertThrows(IllegalArgumentException.class, () -> useCase.update(cmd("LINUX", null, "WINDOWS 11", null)));
    }

    @Test
    void cambiarAOtroSistemaSinEnviarEdicionLaLimpia() {
        equipment.updateOperatingSystem("WINDOWS");
        equipment.updateOsEdition("WINDOWS 11");
        EquipmentResponseDTO r = useCase.update(cmd("LINUX", null, null, null));
        assertNull(r.osEdition());
        assertEquals("LINUX", r.operatingSystem());
    }

    @Test
    void cadenaVaciaLimpiaLicencia() {
        equipment.updateOsLicenseType("OEM");
        EquipmentResponseDTO r = useCase.update(cmd(null, null, null, ""));
        assertNull(r.osLicenseType());
    }
}
