package com.thoth.adapter.out.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IA - regla de sistema operativo sin soporte")
class AIAgentSoSinSoporteTest {

    @Test
    void windows10PorEdicionEsSinSoporte() {
        assertTrue(AIAgentAdapter.esSistemaOperativoSinSoporte("WINDOWS", "24H2", "WINDOWS 10"));
    }

    @Test
    void windows11PorEdicionTieneSoporte() {
        assertFalse(AIAgentAdapter.esSistemaOperativoSinSoporte("WINDOWS", "23H2", "WINDOWS 11"));
    }

    @Test
    void sinEdicionAplicaReglaAntigua() {
        assertTrue(AIAgentAdapter.esSistemaOperativoSinSoporte("WINDOWS", "10 PRO", null));
        assertFalse(AIAgentAdapter.esSistemaOperativoSinSoporte("WINDOWS", "11 PRO", ""));
        assertFalse(AIAgentAdapter.esSistemaOperativoSinSoporte("LINUX", null, "WINDOWS 10"));
    }
}
