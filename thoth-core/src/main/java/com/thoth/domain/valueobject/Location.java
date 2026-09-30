package com.thoth.domain.valueobject;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Location implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String building;
    private String floor;
    private String office;
    private String description;
    
    public static Location of(String building, String floor, String office, String description) {
        // Todos los campos de ubicacion son opcionales: building = sede, office = area.
        // El piso (floor) se conserva por compatibilidad con datos existentes.
        return new Location(
            building != null ? building.trim() : "",
            floor != null ? floor.trim() : "",
            office != null ? office.trim() : "",
            description != null ? description.trim() : ""
        );
    }
    
    public String getFullAddress() {
        return String.format("%s, Piso %s, Oficina %s", building, floor, office);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(building, location.building) &&
               Objects.equals(floor, location.floor) &&
               Objects.equals(office, location.office) &&
               Objects.equals(description, location.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(building, floor, office, description);
    }
}
