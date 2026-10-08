package com.thoth.domain.valueobject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Vida util del equipo (valor calculado, no se persiste).
 *
 * - Anos de vida util segun la categoria (coincidencia por "contiene", sin distinguir mayusculas).
 * - Antiguedad desde la fecha de compra; si no existe, desde el inicio del alquiler y si tampoco,
 *   desde la fecha de registro. En esos dos ultimos casos la antiguedad es estimada.
 */
public final class UsefulLife {

    public static final int DEFAULT_YEARS = 5;

    private static final Pattern WORD_AP = Pattern.compile("(^|[^A-Z0-9])AP([^A-Z0-9]|$)");
    private static final Pattern WORD_RED = Pattern.compile("(^|[^A-Z0-9])RED([^A-Z0-9]|$)");

    private final int years;
    private final double ageYears;
    private final int consumedPercent;
    private final double remainingYears;
    private final boolean estimated;

    private UsefulLife(int years, double ageYears, int consumedPercent, double remainingYears, boolean estimated) {
        this.years = years;
        this.ageYears = ageYears;
        this.consumedPercent = consumedPercent;
        this.remainingYears = remainingYears;
        this.estimated = estimated;
    }

    /** Anos de vida util recomendados para la categoria. */
    public static int yearsForCategory(String category) {
        if (category == null || category.isBlank()) return DEFAULT_YEARS;
        String c = category.trim().toUpperCase(Locale.ROOT);
        if (c.contains("LAPTOP") || c.contains("PORTATIL") || c.contains("PORTÁTIL")) return 4;
        if (c.contains("DESKTOP") || c.contains("ESCRITORIO") || c.contains("TODO EN UNO") || c.contains("ALL IN ONE")) return 5;
        if (c.contains("SERVIDOR") || c.contains("SERVER")) return 6;
        if (c.contains("IMPRESORA") || c.contains("PRINTER") || c.contains("ESCANER") || c.contains("ESCÁNER")
                || c.contains("SCANNER")) return 5;
        if (c.contains("MONITOR")) return 6;
        if (c.contains("SWITCH") || c.contains("ROUTER") || WORD_AP.matcher(c).find() || WORD_RED.matcher(c).find()
                || c.contains("NETWORK") || c.contains("ACCESS POINT")) return 6;
        if (c.contains("UPS")) return 5;
        if (c.contains("TELEFONO") || c.contains("TELÉFONO")) return 5;
        if (c.contains("TABLET")) return 3;
        return DEFAULT_YEARS;
    }

    /** Fecha desde la que se mide la antiguedad: compra, inicio del alquiler o registro (en ese orden). */
    public static LocalDate startDate(LocalDate purchaseDate, LocalDate rentalStartDate, LocalDateTime createdAt) {
        if (purchaseDate != null) return purchaseDate;
        if (rentalStartDate != null) return rentalStartDate;
        return createdAt != null ? createdAt.toLocalDate() : null;
    }

    /** Antiguedad en anos (sin redondear). Fechas futuras o nulas cuentan como 0. */
    public static double ageInYears(LocalDate start, LocalDate today) {
        if (start == null || today == null || start.isAfter(today)) return 0;
        return ChronoUnit.DAYS.between(start, today) / 365.0;
    }

    public static UsefulLife calculate(String category, LocalDate purchaseDate, LocalDate rentalStartDate,
                                       LocalDateTime createdAt, LocalDate today) {
        int years = yearsForCategory(category);
        LocalDate start = startDate(purchaseDate, rentalStartDate, createdAt);
        double age = ageInYears(start, today != null ? today : LocalDate.now());
        int consumed = (int) Math.round(age / years * 100.0);
        return new UsefulLife(years, round1(age), Math.max(0, consumed), round1(years - age), purchaseDate == null);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public int getYears() { return years; }
    public double getAgeYears() { return ageYears; }
    public int getConsumedPercent() { return consumedPercent; }
    public double getRemainingYears() { return remainingYears; }
    public boolean isEstimated() { return estimated; }

    @Override
    public String toString() {
        return "UsefulLife{years=" + years + ", ageYears=" + ageYears + ", consumedPercent=" + consumedPercent
            + ", remainingYears=" + remainingYears + ", estimated=" + estimated + "}";
    }
}
