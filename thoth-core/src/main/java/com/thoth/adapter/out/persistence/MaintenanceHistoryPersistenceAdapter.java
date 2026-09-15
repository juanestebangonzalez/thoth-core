package com.thoth.adapter.out.persistence;

import com.thoth.adapter.out.persistence.entity.MaintenanceHistoryEntity;
import com.thoth.adapter.out.persistence.mapper.MaintenanceHistoryEntityMapper;
import com.thoth.adapter.out.persistence.repository.MaintenanceHistoryJpaRepository;
import com.thoth.application.port.output.MaintenanceHistoryRepositoryPort;
import com.thoth.domain.model.MaintenanceHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MaintenanceHistoryPersistenceAdapter implements MaintenanceHistoryRepositoryPort {

    private final MaintenanceHistoryJpaRepository repository;
    private final MaintenanceHistoryEntityMapper mapper;

    @Override
    public MaintenanceHistory save(MaintenanceHistory maintenance) {
        MaintenanceHistoryEntity entity = mapper.toEntity(maintenance);
        MaintenanceHistoryEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MaintenanceHistory> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MaintenanceHistory> findByEquipmentId(UUID equipmentId) {
        return repository.findByEquipmentIdOrderByPerformedDateDesc(equipmentId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public long countByEquipmentId(UUID equipmentId) {
        return repository.countByEquipmentId(equipmentId);
    }
}