package com.thoth.adapter.out.persistence;

import com.thoth.adapter.out.persistence.entity.MaintenanceRecordEntity;
import com.thoth.adapter.out.persistence.repository.MaintenanceRecordJpaRepository;
import com.thoth.adapter.out.persistence.mapper.MaintenanceRecordEntityMapper;
import com.thoth.application.port.output.MaintenanceRepositoryPort;
import com.thoth.domain.valueobject.MaintenanceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MaintenanceRepositoryAdapter implements MaintenanceRepositoryPort {
    
    private final MaintenanceRecordJpaRepository jpaRepository;
    private final MaintenanceRecordEntityMapper mapper;
    
    @Override
    public Object save(Object maintenance) {
        return maintenance;
    }
    
    @Override
    public Optional<Object> findById(UUID maintenanceId) {
        return jpaRepository.findById(maintenanceId)
            .map(entity -> (Object) entity);
    }
    
    @Override
    public List<Object> findByEquipmentId(UUID equipmentId) {
        return jpaRepository.findByEquipmentId(equipmentId).stream()
            .map(entity -> (Object) entity)
            .toList();
    }
    
    @Override
    public List<Object> findByType(MaintenanceType type) {
        return jpaRepository.findByType(type.name()).stream()
            .map(entity -> (Object) entity)
            .toList();
    }
}