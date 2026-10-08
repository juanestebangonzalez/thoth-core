package com.thoth.application.dto;

/**
 * Vida util calculada del equipo.
 * years: anos recomendados por categoria; ageYears: antiguedad (1 decimal);
 * consumedPercent: porcentaje consumido (puede superar 100); remainingYears: anos restantes
 * (1 decimal, negativo si ya se supero); estimated: true si no hay fecha de compra.
 */
public record UsefulLifeDTO(
    int years,
    double ageYears,
    int consumedPercent,
    double remainingYears,
    boolean estimated
) {}
