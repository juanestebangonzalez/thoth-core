package com.thoth.application.usecase;

import com.thoth.application.command.ListEquipmentCommand;
import com.thoth.application.dto.EquipmentDTO;
import com.thoth.application.dto.PageResponseDTO;
import com.thoth.application.mapper.EquipmentDtoMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.application.usecase.impl.ListEquipmentUseCaseImpl;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentStatus;
import com.thoth.domain.valueobject.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListEquipmentUseCaseImplTest {

    private EquipmentRepositoryPort equipmentRepository;
    private ListEquipmentUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        equipmentRepository = mock(EquipmentRepositoryPort.class);
        useCase = new ListEquipmentUseCaseImpl(equipmentRepository, new EquipmentDtoMapper());

        Equipment portatilActivo = equipo("PORTATIL 1", "LAPTOP", EquipmentStatus.ACTIVE);
        Equipment portatilRetirado = equipo("PORTATIL 2", "LAPTOP", EquipmentStatus.RETIRED);
        Equipment escritorioMantenimiento = equipo("PC 1", "DESKTOP", EquipmentStatus.MAINTENANCE);
        when(equipmentRepository.findAll()).thenReturn(List.of(portatilActivo, portatilRetirado, escritorioMantenimiento));
    }

    private static Equipment equipo(String nombre, String categoria, EquipmentStatus estado) {
        Equipment e = Equipment.create(nombre, categoria, null, "DELL", "X", null, null, null,
            Location.of("SEDE", "", "AREA", ""), "", "admin");
        e.setStatus(estado);
        return e;
    }

    private PageResponseDTO<EquipmentDTO> listar(String status, String category) {
        return useCase.listAll(new ListEquipmentCommand(0, 20, "name", status, category));
    }

    @Test
    @DisplayName("Sin filtros devuelve todos los equipos")
    void sinFiltros_devuelveTodos() {
        PageResponseDTO<EquipmentDTO> page = listar(null, null);
        assertEquals(3, page.content().size());
        assertEquals(3L, page.totalElements());
    }

    @Test
    @DisplayName("Filtros vacios se ignoran")
    void filtrosVacios_seIgnoran() {
        assertEquals(3, listar("", "  ").content().size());
    }

    @Test
    @DisplayName("Filtra por estado con el codigo exacto")
    void filtraPorEstado() {
        PageResponseDTO<EquipmentDTO> page = listar("ACTIVE", null);
        assertEquals(1, page.content().size());
        assertEquals("PORTATIL 1", page.content().get(0).name());
        assertEquals(1L, page.totalElements());

        assertEquals(1, listar("RETIRED", null).content().size());
        assertEquals(0, listar("INACTIVE", null).content().size());
    }

    @Test
    @DisplayName("Filtra por categoria sin distinguir mayusculas")
    void filtraPorCategoriaSinMayusculas() {
        PageResponseDTO<EquipmentDTO> page = listar(null, "laptop");
        assertEquals(2, page.content().size());
        assertTrue(page.content().stream().allMatch(e -> e.category().equals("LAPTOP")));
    }

    @Test
    @DisplayName("Combina estado y categoria")
    void combinaEstadoYCategoria() {
        PageResponseDTO<EquipmentDTO> page = listar("RETIRED", "Laptop");
        assertEquals(1, page.content().size());
        assertEquals("PORTATIL 2", page.content().get(0).name());
        assertEquals(0, listar("ACTIVE", "desktop").content().size());
    }
}
