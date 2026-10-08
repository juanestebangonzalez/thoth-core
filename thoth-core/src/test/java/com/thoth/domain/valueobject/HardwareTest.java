package com.thoth.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HardwareTest {

    @Test
    @DisplayName("Los umbrales centralizados tienen los valores acordados")
    void umbrales_valoresAcordados() {
        assertEquals(60, Hardware.DISK_HEALTH_WARNING);
        assertEquals(30, Hardware.DISK_HEALTH_CRITICAL);
        assertEquals(50, Hardware.DISK_TEMP_WARNING);
        assertEquals(60, Hardware.DISK_TEMP_CRITICAL);
    }

    @Test
    @DisplayName("Salud del disco: ADVERTENCIA si < 60, CRITICO si < 30")
    void saludDelDisco_niveles() {
        assertEquals("OK", Hardware.diskHealthLevel(null));
        assertEquals("OK", Hardware.diskHealthLevel(100));
        assertEquals("OK", Hardware.diskHealthLevel(60));
        assertEquals("ADVERTENCIA", Hardware.diskHealthLevel(59));
        assertEquals("ADVERTENCIA", Hardware.diskHealthLevel(30));
        assertEquals("CRITICO", Hardware.diskHealthLevel(29));
        assertEquals("CRITICO", Hardware.diskHealthLevel(0));
    }

    @Test
    @DisplayName("Temperatura del disco: ADVERTENCIA si > 50, CRITICO si >= 60")
    void temperaturaDelDisco_niveles() {
        assertEquals("OK", Hardware.diskTemperatureLevel(null));
        assertEquals("OK", Hardware.diskTemperatureLevel(35));
        assertEquals("OK", Hardware.diskTemperatureLevel(50));
        assertEquals("ADVERTENCIA", Hardware.diskTemperatureLevel(51));
        assertEquals("ADVERTENCIA", Hardware.diskTemperatureLevel(59));
        assertEquals("CRITICO", Hardware.diskTemperatureLevel(60));
        assertEquals("CRITICO", Hardware.diskTemperatureLevel(75));
    }

    @Test
    @DisplayName("Los metodos de instancia usan los mismos umbrales")
    void metodosDeInstancia_coherentesConUmbrales() {
        Hardware advertencia = Hardware.builder().diskHealthPercent(55).diskTemperatureCelsius(52).build();
        assertTrue(advertencia.hasWarningDiskHealth());
        assertFalse(advertencia.hasCriticalDiskHealth());
        assertTrue(advertencia.hasWarningTemperature());
        assertFalse(advertencia.hasCriticalTemperature());

        Hardware critico = Hardware.builder().diskHealthPercent(20).diskTemperatureCelsius(60).build();
        assertTrue(critico.hasCriticalDiskHealth());
        assertTrue(critico.hasWarningDiskHealth());
        assertTrue(critico.hasCriticalTemperature());

        Hardware sano = Hardware.builder().diskHealthPercent(95).diskTemperatureCelsius(40).build();
        assertFalse(sano.hasWarningDiskHealth());
        assertFalse(sano.hasWarningTemperature());
    }

    @Test
    @DisplayName("isEmpty detecta cualquier campo de hardware")
    void isEmpty_detectaCualquierCampo() {
        assertTrue(Hardware.builder().build().isEmpty());
        assertFalse(Hardware.builder().diskHealthPercent(80).build().isEmpty());
        assertFalse(Hardware.builder().ramType(RamType.DDR4).build().isEmpty());
        assertFalse(Hardware.builder().diskSizeGb(512).build().isEmpty());
    }
}
