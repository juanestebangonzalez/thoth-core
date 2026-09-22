package com.thoth.application.mapper;

import com.thoth.application.dto.LocationDTO;
import com.thoth.domain.valueobject.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationDtoMapper {
    
    public LocationDTO toDTO(Location domain) {
        if (domain == null) return null;
        
        return new LocationDTO(
            domain.getBuilding(),
            domain.getFloor(),
            domain.getOffice(),
            domain.getDescription()
        );
    }
    
    public Location toDomain(LocationDTO dto) {
        if (dto == null) return null;
        
        return Location.of(
            dto.building(),
            dto.floor(),
            dto.office(),
            dto.description()
        );
    }
}
