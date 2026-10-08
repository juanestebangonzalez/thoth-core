package com.thoth.domain.valueobject;

import com.thoth.domain.model.Equipment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vida util y criticidad")
class UsefulLifeTest {

    private static final LocalDate HOY = LocalDate.of(2026, 1, 1);

    @Test
    @DisplayName("Anos de vida util segun la categoria (contiene, sin distinguir mayusculas)")
    void yearsByCategory() {
        assertEquals(4, UsefulLife.yearsForCategory("laptop"));
        assertEquals(4, UsefulLife.yearsForCategory("PORTATIL GAMER"));
        assertEquals(5, UsefulLife.yearsForCategory("Desktop"));
        assertEquals(5, UsefulLife.yearsForCategory("PC TODO EN UNO"));
        assertEquals(5, UsefulLife.yearsForCategory("ALL IN ONE"));
        assertEquals(6, UsefulLife.yearsForCategory("SERVIDOR"));
        assertEquals(5, UsefulLife.yearsForCategory("IMPRESORA LASER"));
        assertEquals(5, UsefulLife.yearsForCategory("SCANNER"));
        assertEquals(6, UsefulLife.yearsForCategory("monitor"));
        assertEquals(6, UsefulLife.yearsForCategory("SWITCH"));
        assertEquals(6, UsefulLife.yearsForCategory("ROUTER"));
        assertEquals(6, UsefulLife.yearsForCategory("AP"));
        assertEquals(6, UsefulLife.yearsForCategory("EQUIPO DE RED"));
        assertEquals(5, UsefulLife.yearsForCategory("UPS"));
        assertEquals(5, UsefulLife.yearsForCategory("TELEFONO IP"));
        assertEquals(3, UsefulLife.yearsForCategory("TABLET"));
        assertEquals(5, UsefulLife.yearsForCategory("OTRA COSA"));
        assertEquals(5, UsefulLife.yearsForCategory(null));
    }

    @Test
    @DisplayName("Con fecha de compra: antiguedad real, no estimada")
    void withPurchaseDate() {
        UsefulLife life = UsefulLife.calculate("LAPTOP", HOY.minusDays(730), null, null, HOY);
        assertEquals(4, life.getYears());
        assertEquals(2.0, life.getAgeYears());
        assertEquals(50, life.getConsumedPercent());
        assertEquals(2.0, life.getRemainingYears());
        assertFalse(life.isEstimated());
    }

    @Test
    @DisplayName("Vida util superada: porcentaje mayor a 100 y anos restantes negativos")
    void exceeded() {
        UsefulLife life = UsefulLife.calculate("TABLET", HOY.minusDays(365 * 6), null, null, HOY);
        assertEquals(200, life.getConsumedPercent());
        assertEquals(-3.0, life.getRemainingYears());
    }

    @Test
    @DisplayName("Sin fecha de compra usa el inicio del alquiler y luego la fecha de registro (estimada)")
    void fallbackDates() {
        UsefulLife byRental = UsefulLife.calculate("DESKTOP", null, HOY.minusDays(365), null, HOY);
        assertEquals(1.0, byRental.getAgeYears());
        assertTrue(byRental.isEstimated());

        UsefulLife byCreated = UsefulLife.calculate("DESKTOP", null, null, HOY.minusDays(365 * 2).atStartOfDay(), HOY);
        assertEquals(2.0, byCreated.getAgeYears());
        assertTrue(byCreated.isEstimated());

        UsefulLife none = UsefulLife.calculate("DESKTOP", null, null, null, HOY);
        assertEquals(0.0, none.getAgeYears());
        assertEquals(0, none.getConsumedPercent());
        assertEquals(5.0, none.getRemainingYears());
        assertTrue(none.isEstimated());
    }

    @Test
    @DisplayName("La fecha de compra tiene prioridad sobre el alquiler y el registro")
    void startDatePriority() {
        LocalDate compra = LocalDate.of(2020, 1, 1);
        assertEquals(compra, UsefulLife.startDate(compra, LocalDate.of(2022, 1, 1), LocalDateTime.of(2023, 1, 1, 0, 0)));
        assertEquals(LocalDate.of(2022, 1, 1), UsefulLife.startDate(null, LocalDate.of(2022, 1, 1), LocalDateTime.of(2023, 1, 1, 0, 0)));
        assertEquals(LocalDate.of(2023, 1, 1), UsefulLife.startDate(null, null, LocalDateTime.of(2023, 1, 1, 10, 0)));
    }

    @Test
    @DisplayName("Criticidad por centro de costo")
    void criticality() {
        assertEquals("ALTA", Criticality.fromCostCenter("ASISTENCIAL"));
        assertEquals("ALTA", Criticality.fromCostCenter("asistencial"));
        assertEquals("MEDIA", Criticality.fromCostCenter("ADMINISTRATIVO"));
        assertEquals("BAJA", Criticality.fromCostCenter("OTRO"));
        assertEquals("BAJA", Criticality.fromCostCenter(""));
        assertEquals("BAJA", Criticality.fromCostCenter(null));
    }

    @Test
    @DisplayName("El equipo expone vida util y criticidad calculadas")
    void equipmentCalculations() {
        Equipment equipment = Equipment.create("PC", "LAPTOP", null, null, null, null,
            LocalDate.now().minusDays(365), null, null, null, "admin");
        equipment.updateCostCenter("administrativo");
        assertEquals(4, equipment.calculateUsefulLife().getYears());
        assertEquals(1.0, equipment.calculateUsefulLife().getAgeYears());
        assertEquals("MEDIA", equipment.calculateCriticality());
    }
}
