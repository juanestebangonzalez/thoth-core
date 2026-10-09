package com.thoth.domain.model;

import com.thoth.domain.valueobject.Location;
import com.thoth.domain.valueobject.OperatingSystemCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Equipo - software y licenciamiento del sistema operativo")
class EquipmentOsLicensingTest {

    private Equipment equipo() {
        return Equipment.create("PC", "DESKTOP", "SN-1", "DELL", "OPTIPLEX", null,
            LocalDate.of(2024, 1, 1), BigDecimal.ONE, Location.of("A", "1", "101", ""), "JUAN", "admin");
    }

    @Test
    void edicionValidaSeGuardaEnMayusculas() {
        Equipment e = equipo();
        e.updateOsEdition("windows 11");
        assertEquals("WINDOWS 11", e.getOsEdition());
        e.updateOsEdition("Windows10");
        assertEquals("WINDOWS 10", e.getOsEdition());
    }

    @Test
    void edicionVaciaLimpia() {
        Equipment e = equipo();
        e.updateOsEdition("WINDOWS 10");
        e.updateOsEdition("");
        assertNull(e.getOsEdition());
    }

    @Test
    void edicionInvalidaLanzaError() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> equipo().updateOsEdition("WINDOWS XP"));
        assertTrue(ex.getMessage().contains("WINDOWS 10, WINDOWS 11"));
    }

    @Test
    void tipoLicenciaValidoEInvalido() {
        Equipment e = equipo();
        e.updateOsLicenseType("oem");
        assertEquals("OEM", e.getOsLicenseType());
        e.updateOsLicenseType("volumen");
        assertEquals("VOLUMEN", e.getOsLicenseType());
        e.updateOsLicenseType(" ");
        assertNull(e.getOsLicenseType());
        assertThrows(IllegalArgumentException.class, () -> e.updateOsLicenseType("GRATIS"));
    }

    @Test
    void edicionSinWindowsEsInvalida() {
        Equipment e = equipo();
        e.updateOperatingSystem("LINUX");
        e.updateOsEdition("WINDOWS 11");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> e.validateOperatingSystemData(true));
        assertTrue(ex.getMessage().contains("solo aplica cuando el sistema operativo es WINDOWS"));
    }

    @Test
    void versionDeWindowsDebeSerDelCatalogo() {
        Equipment e = equipo();
        e.updateOperatingSystem("WINDOWS");
        e.updateOsEdition("WINDOWS 11");
        e.updateOsVersion("24h2");
        assertEquals("24H2", e.getOsVersion());
        assertDoesNotThrow(() -> e.validateOperatingSystemData(true));
        e.updateOsVersion("22H2");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> e.validateOperatingSystemData(true));
        assertTrue(ex.getMessage().contains("Version de WINDOWS invalida"));
        // Sin validar version (dato antiguo sin cambios) no falla
        assertDoesNotThrow(() -> e.validateOperatingSystemData(false));
    }

    @Test
    void versionLibreParaOtrosSistemas() {
        Equipment e = equipo();
        e.updateOperatingSystem("LINUX");
        e.updateOsVersion("UBUNTU 24.04");
        assertDoesNotThrow(() -> e.validateOperatingSystemData(true));
    }

    @Test
    void vencimientoSegunTipoDeLicencia() {
        assertEquals("NO APLICA", OperatingSystemCatalog.licenseExpiration("OEM"));
        assertEquals("NO APLICA", OperatingSystemCatalog.licenseExpiration("retail"));
        assertNull(OperatingSystemCatalog.licenseExpiration("VOLUMEN"));
        assertNull(OperatingSystemCatalog.licenseExpiration(null));
    }
}
