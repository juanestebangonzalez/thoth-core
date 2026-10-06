package com.thoth.application.service;

import com.thoth.adapter.out.persistence.entity.AreaEntity;
import com.thoth.adapter.out.persistence.entity.CostCenterEntity;
import com.thoth.adapter.out.persistence.entity.DeviceTypeEntity;
import com.thoth.adapter.out.persistence.entity.MaintenanceCategoryEntity;
import com.thoth.adapter.out.persistence.entity.SedeEntity;
import com.thoth.adapter.out.persistence.repository.AreaRepository;
import com.thoth.adapter.out.persistence.repository.CostCenterRepository;
import com.thoth.adapter.out.persistence.repository.DeviceTypeJpaRepository;
import com.thoth.adapter.out.persistence.repository.MaintenanceCategoryJpaRepository;
import com.thoth.adapter.out.persistence.repository.SedeRepository;
import com.thoth.application.command.RegisterEquipmentCommand;
import com.thoth.application.dto.EquipmentImportResultDTO;
import com.thoth.application.dto.EquipmentImportRowDTO;
import com.thoth.application.dto.EquipmentImportRowResultDTO;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentImportService Tests")
class EquipmentImportServiceTest {

    @Mock private DeviceTypeJpaRepository deviceTypeRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private AreaRepository areaRepository;
    @Mock private CostCenterRepository costCenterRepository;
    @Mock private MaintenanceCategoryJpaRepository maintenanceCategoryRepository;
    @Mock private EquipmentRepositoryPort equipmentRepository;
    @Mock private EquipmentImportRowProcessor rowProcessor;
    @Mock private AuditService auditService;

    private EquipmentImportService service;

    @BeforeEach
    void setUp() {
        service = new EquipmentImportService(deviceTypeRepository, sedeRepository, areaRepository,
            costCenterRepository, maintenanceCategoryRepository, equipmentRepository,
            new MaintenanceSchedulerService(), rowProcessor, auditService);

        lenient().when(deviceTypeRepository.findByActiveTrueOrderByNameAsc())
            .thenReturn(List.of(DeviceTypeEntity.builder().name("LAPTOP").build()));
        lenient().when(sedeRepository.findAll())
            .thenReturn(List.of(SedeEntity.builder().name("PRINCIPAL").build()));
        lenient().when(areaRepository.findAll())
            .thenReturn(List.of(AreaEntity.builder().name("URGENCIAS").build()));
        lenient().when(costCenterRepository.findAll())
            .thenReturn(List.of(CostCenterEntity.builder().name("ASISTENCIAL").build()));
        lenient().when(maintenanceCategoryRepository.findByActiveTrueOrderByNameAsc())
            .thenReturn(List.of(MaintenanceCategoryEntity.builder().name("PREVENTIVO").build()));
    }

    private EquipmentImportRowDTO validRow(int rowNumber, String inventory) {
        return EquipmentImportRowDTO.builder()
            .rowNumber(rowNumber)
            .name("pc urgencias " + rowNumber)
            .category("laptop")
            .inventoryNumber(inventory)
            .sede("principal")
            .area("Urgencias")
            .costCenter("asistencial")
            .ownershipType("PROPIO")
            .purchaseValue(1500000)
            .ramSizeGb("8")
            .build();
    }

    @Test
    @DisplayName("Fila valida en dryRun: OK, calcula proxima fecha y no guarda")
    void validRow_dryRun_isOkAndDoesNotSave() {
        LocalDate last = LocalDate.now().minusMonths(2);
        EquipmentImportRowDTO row = validRow(2, "INV-001");
        row.setLastMaintenanceDate(last.toString());
        row.setLastMaintenanceType("preventivo");

        EquipmentImportResultDTO result = service.importRows(List.of(row), true, "admin");

        assertThat(result.dryRun()).isTrue();
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.validos()).isEqualTo(1);
        assertThat(result.conErrores()).isZero();
        assertThat(result.importados()).isZero();
        EquipmentImportRowResultDTO r = result.rows().get(0);
        assertThat(r.rowNumber()).isEqualTo(2);
        assertThat(r.status()).isEqualTo("OK");
        assertThat(r.errors()).isEmpty();
        assertThat(r.nextMaintenanceDate()).isEqualTo(last.plusMonths(6).toString());

        verify(rowProcessor, never()).importRow(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("Fila sin nombre: ERROR")
    void missingName_isError() {
        EquipmentImportRowDTO row = validRow(2, "INV-002");
        row.setName("   ");

        EquipmentImportResultDTO result = service.importRows(List.of(row), true, "admin");

        EquipmentImportRowResultDTO r = result.rows().get(0);
        assertThat(r.status()).isEqualTo("ERROR");
        assertThat(r.errors()).anyMatch(e -> e.contains("nombre"));
        assertThat(result.conErrores()).isEqualTo(1);
        assertThat(result.validos()).isZero();
    }

    @Test
    @DisplayName("Sede inexistente: ERROR, no se crea automaticamente")
    void unknownSede_isError() {
        EquipmentImportRowDTO row = validRow(3, "INV-003");
        row.setSede("Norte");

        EquipmentImportResultDTO result = service.importRows(List.of(row), true, "admin");

        EquipmentImportRowResultDTO r = result.rows().get(0);
        assertThat(r.status()).isEqualTo("ERROR");
        assertThat(r.errors()).contains("La sede 'NORTE' no existe en THOTH");
        verify(sedeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inventario repetido dentro del archivo: la segunda fila es ERROR")
    void duplicatedInventoryInFile_isError() {
        EquipmentImportRowDTO first = validRow(2, "INV-100");
        EquipmentImportRowDTO second = validRow(3, "inv-100");

        EquipmentImportResultDTO result = service.importRows(List.of(first, second), true, "admin");

        assertThat(result.rows().get(0).status()).isEqualTo("OK");
        assertThat(result.rows().get(1).status()).isEqualTo("ERROR");
        assertThat(result.rows().get(1).errors()).anyMatch(e -> e.contains("repetido"));
    }

    @Test
    @DisplayName("dryRun=false importa solo las filas validas con el comando canonico")
    void import_onlyValidRows() {
        when(rowProcessor.importRow(any(), any(), any(), any(), any(), any())).thenReturn(UUID.randomUUID());
        EquipmentImportRowDTO ok = validRow(2, "INV-200");
        ok.setOwnershipType("alquilado");
        ok.setRentalMonthlyValue("85000.50");
        EquipmentImportRowDTO bad = validRow(3, "INV-201");
        bad.setCategory("NAVE ESPACIAL");

        EquipmentImportResultDTO result = service.importRows(List.of(ok, bad), false, "admin");

        assertThat(result.dryRun()).isFalse();
        assertThat(result.importados()).isEqualTo(1);
        assertThat(result.conErrores()).isEqualTo(1);
        assertThat(result.rows().get(0).status()).isEqualTo("IMPORTADO");
        assertThat(result.rows().get(1).status()).isEqualTo("ERROR");

        ArgumentCaptor<RegisterEquipmentCommand> captor = ArgumentCaptor.forClass(RegisterEquipmentCommand.class);
        verify(rowProcessor, times(1)).importRow(captor.capture(), any(), any(), any(), any(), eq("admin"));
        RegisterEquipmentCommand cmd = captor.getValue();
        assertThat(cmd.building()).isEqualTo("PRINCIPAL");
        assertThat(cmd.office()).isEqualTo("URGENCIAS");
        assertThat(cmd.floor()).isEmpty();
        assertThat(cmd.costCenter()).isEqualTo("ASISTENCIAL");
        assertThat(cmd.ownershipType()).isEqualTo("RENTED");
        assertThat(cmd.rentalMonthlyValue()).isEqualByComparingTo(new BigDecimal("85000.50"));
        assertThat(cmd.ramSizeGb()).isEqualTo(8);

        verify(auditService).log(eq("IMPORT"), eq("EQUIPMENT"), anyString(), anyString(),
            eq("Importados 1 equipos"), eq("admin"));
    }

    @Test
    @DisplayName("Mas de 2000 filas: IllegalArgumentException")
    void tooManyRows_throws() {
        List<EquipmentImportRowDTO> rows = new ArrayList<>();
        for (int i = 0; i < EquipmentImportService.MAX_ROWS + 1; i++) rows.add(new EquipmentImportRowDTO());

        assertThatThrownBy(() -> service.importRows(rows, true, "admin"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
