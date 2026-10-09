package com.thoth.application.service;

import com.thoth.application.dto.EquipmentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hoja de vida - software y licenciamiento")
class HojaVidaSoftwareTest {

    private EquipmentDTO dto(String os, String version, String edition, String license) {
        return new EquipmentDTO(UUID.randomUUID(), "PC", "DESKTOP", null, null, null, null, null, "ACTIVE",
            null, null, null, null, null, "OWNED", null, null, null, null, null, os, version,
            null, null, null, null, null, null, null, null, null, null, null, edition, license);
    }

    @Test
    void sinSistemaOperativoListaVacia() {
        assertTrue(HojaVidaService.software(dto(null, null, null, null)).isEmpty());
    }

    @Test
    void windowsOemConEdicion() {
        List<Map<String, Object>> sw = HojaVidaService.software(dto("WINDOWS", "24H2", "WINDOWS 11", "OEM"));
        assertEquals(1, sw.size());
        Map<String, Object> item = sw.get(0);
        assertEquals("WINDOWS 11", item.get("software"));
        assertEquals("24H2", item.get("version"));
        assertEquals("OEM", item.get("licenseType"));
        assertTrue(item.containsKey("licenseKey"));
        assertNull(item.get("licenseKey"));
        assertEquals("NO APLICA", item.get("expiration"));
    }

    @Test
    void sinEdicionUsaSistemaOperativoYVolumenSinVencimiento() {
        Map<String, Object> item = HojaVidaService.software(dto("LINUX", "UBUNTU 24.04", null, "VOLUMEN")).get(0);
        assertEquals("LINUX", item.get("software"));
        assertNull(item.get("expiration"));
    }
}
