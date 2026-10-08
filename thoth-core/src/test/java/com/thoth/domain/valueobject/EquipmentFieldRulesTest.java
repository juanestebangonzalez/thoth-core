package com.thoth.domain.valueobject;

import com.thoth.domain.model.Equipment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validacion de celular, IP y correo del equipo")
class EquipmentFieldRulesTest {

    @Test
    @DisplayName("Celular: opcional, solo digitos y exactamente 10")
    void phone() {
        assertNull(EquipmentFieldRules.validatePhone(null));
        assertNull(EquipmentFieldRules.validatePhone(""));
        assertNull(EquipmentFieldRules.validatePhone("3001234567"));
        assertNotNull(EquipmentFieldRules.validatePhone("300123456"));
        assertNotNull(EquipmentFieldRules.validatePhone("30012345678"));
        assertNotNull(EquipmentFieldRules.validatePhone("300-123-45"));
        assertNotNull(EquipmentFieldRules.validatePhone("30012345ab"));
    }

    @Test
    @DisplayName("IP: opcional e IPv4 valida")
    void ip() {
        assertNull(EquipmentFieldRules.validateIpAddress(null));
        assertNull(EquipmentFieldRules.validateIpAddress("192.168.1.10"));
        assertNull(EquipmentFieldRules.validateIpAddress("10.0.0.255"));
        assertNotNull(EquipmentFieldRules.validateIpAddress("256.1.1.1"));
        assertNotNull(EquipmentFieldRules.validateIpAddress("192.168.1"));
        assertNotNull(EquipmentFieldRules.validateIpAddress("abc"));
    }

    @Test
    @DisplayName("Asignacion de IP: DHCP o FIJA")
    void ipAssignment() {
        assertNull(EquipmentFieldRules.validateIpAssignment("dhcp"));
        assertNull(EquipmentFieldRules.validateIpAssignment("FIJA"));
        assertNull(EquipmentFieldRules.validateIpAssignment(null));
        assertNotNull(EquipmentFieldRules.validateIpAssignment("OTRA"));
        assertEquals("DHCP", EquipmentFieldRules.normalizeIpAssignment(" dhcp "));
    }

    @Test
    @DisplayName("Correo: opcional, formato valido y maximo 150 caracteres")
    void email() {
        assertNull(EquipmentFieldRules.validateEmail(null));
        assertNull(EquipmentFieldRules.validateEmail("juan.perez@hospital.gov.co"));
        assertNotNull(EquipmentFieldRules.validateEmail("juan@"));
        assertNotNull(EquipmentFieldRules.validateEmail("sin-arroba.com"));
        assertNotNull(EquipmentFieldRules.validateEmail("a".repeat(145) + "@x.com"));
    }

    @Test
    @DisplayName("El dominio normaliza: mayusculas salvo el correo (minusculas) y '' limpia")
    void equipmentNormalization() {
        Equipment e = Equipment.create("PC", "DESKTOP", null, null, null, null,
            LocalDate.of(2024, 1, 1), null, null, null, "admin");
        e.updateResponsiblePosition("jefe de enfermeria");
        e.updateResponsibleDocument("cc 123");
        e.updateResponsiblePhone("3001234567");
        e.updateResponsibleEmail("Juan.Perez@Hospital.CO");
        e.updateIpAddress("192.168.0.20");
        e.updateIpAssignment("fija");
        assertEquals("JEFE DE ENFERMERIA", e.getResponsiblePosition());
        assertEquals("CC 123", e.getResponsibleDocument());
        assertEquals("juan.perez@hospital.co", e.getResponsibleEmail());
        assertEquals("FIJA", e.getIpAssignment());

        e.updateResponsibleEmail("");
        e.updateIpAddress("");
        assertNull(e.getResponsibleEmail());
        assertNull(e.getIpAddress());

        assertThrows(IllegalArgumentException.class, () -> e.updateResponsiblePhone("12345"));
        assertThrows(IllegalArgumentException.class, () -> e.updateIpAddress("999.1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> e.updateResponsibleEmail("malo@"));
        assertThrows(IllegalArgumentException.class, () -> e.updateIpAssignment("STATICA"));
    }
}
