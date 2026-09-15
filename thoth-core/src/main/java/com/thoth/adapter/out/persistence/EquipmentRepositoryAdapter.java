package com.thoth.adapter.out.persistence;

import com.thoth.adapter.out.persistence.entity.EquipmentEntity;
import com.thoth.adapter.out.persistence.repository.EquipmentJpaRepository;
import com.thoth.adapter.out.persistence.mapper.EquipmentEntityMapper;
import com.thoth.application.port.output.EquipmentRepositoryPort;
import com.thoth.domain.model.Equipment;
import com.thoth.domain.valueobject.EquipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EquipmentRepositoryAdapter implements EquipmentRepositoryPort {
    
    private final EquipmentJpaRepository jpaRepository;
    private final EquipmentEntityMapper mapper;
    
    @Override
    public Equipment save(Equipment equipment) {
        EquipmentEntity entity = mapper.toEntity(equipment);
        EquipmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Equipment> findById(UUID equipmentId) {
        return jpaRepository.findById(equipmentId)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Equipment> findBySerialNumber(String serialNumber) {
        return jpaRepository.findBySerialNumber(serialNumber)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Equipment> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Equipment> findByStatus(EquipmentStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Optional<Equipment> findByInventoryNumber(String inventoryNumber) {
        return jpaRepository.findByInventoryNumber(inventoryNumber)
            .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID equipmentId) {
        jpaRepository.deleteById(equipmentId);
    }
}