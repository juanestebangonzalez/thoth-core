package com.thoth.domain.model;

import com.thoth.domain.valueobject.EquipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reglas de asociacion de monitores")
class EquipmentMonitorAssociationTest {

    private static Equipment equipo(String category) {
        Equipment e = Equipment.create("EQ " + category, category, null, null, null, null,
            LocalDate.of(2024, 1, 1), null, null, null, "admin");
        e.setEquipmentId(UUID.randomUUID());
        return e;
    }

    @Test
    @DisplayName("Un monitor se asocia a un PC activo")
    void monitorToPc() {
        Equipment monitor = equipo("MONITOR");
        Equipment pc = equipo("DESKTOP");
        monitor.associateTo(pc);
        assertEquals(pc.getEquipmentId(), monitor.getAssociatedEquipmentId());

        monitor.clearAssociation();
        assertNull(monitor.getAssociatedEquipmentId());
    }

    @Test
    @DisplayName("Solo los monitores pueden asociarse")
    void onlyMonitors() {
        Equipment laptop = equipo("LAPTOP");
        Equipment pc = equipo("DESKTOP");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> laptop.associateTo(pc));
        assertTrue(ex.getMessage().contains("monitores"));
    }

    @Test
    @DisplayName("El destino debe existir, no ser monitor, no estar retirado y no ser el mismo equipo")
    void targetRules() {
        Equipment monitor = equipo("MONITOR LED");
        assertThrows(IllegalArgumentException.class, () -> monitor.associateTo(null));
        assertThrows(IllegalArgumentException.class, () -> monitor.associateTo(monitor));
        assertThrows(IllegalArgumentException.class, () -> monitor.associateTo(equipo("MONITOR")));

        Equipment retired = equipo("DESKTOP");
        retired.markAsRetired();
        assertThrows(IllegalArgumentException.class, () -> monitor.associateTo(retired));
        assertNull(monitor.getAssociatedEquipmentId());
    }

    @Test
    @DisplayName("Validacion estatica reutilizada por la importacion")
    void staticValidation() {
        UUID target = UUID.randomUUID();
        assertNull(Equipment.validateAssociation("MONITOR", null, target, "DESKTOP", EquipmentStatus.ACTIVE, true));
        assertNotNull(Equipment.validateAssociation("DESKTOP", null, target, "DESKTOP", EquipmentStatus.ACTIVE, true));
        assertNotNull(Equipment.validateAssociation("MONITOR", null, null, null, null, false));
        assertNotNull(Equipment.validateAssociation("MONITOR", target, target, "DESKTOP", EquipmentStatus.ACTIVE, true));
        assertNotNull(Equipment.validateAssociation("MONITOR", null, target, "MONITOR", EquipmentStatus.ACTIVE, true));
        assertNotNull(Equipment.validateAssociation("MONITOR", null, target, "DESKTOP", EquipmentStatus.RETIRED, true));
    }
}
