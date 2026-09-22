package com.thoth.application.dto;

public record LocationDTO(
    String building,
    String floor,
    String office,
    String description
) {
    public String getFullAddress() {
        return String.format("%s, Piso %s, Oficina %s", building, floor, office);
    }
}
