package com.thoth.adapter.out.persistence.mapper;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRecordEntityMapper {
    
    public MaintenanceRecordEntity toEntity(Object domain) {
        return null;
    }
    
    public Object toDomain(MaintenanceRecordEntity entity) {
        return entity;
    }
}