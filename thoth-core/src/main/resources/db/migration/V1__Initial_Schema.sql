CREATE TABLE equipment (
    equipment_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    brand VARCHAR(100),
    model VARCHAR(100),
    mac_address VARCHAR(17),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    location_building VARCHAR(100) NOT NULL,
    location_floor VARCHAR(50),
    location_office VARCHAR(50),
    assigned_to VARCHAR(255),
    purchase_date DATE NOT NULL,
    purchase_value NUMERIC(12, 2) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE', 'RETIRED'))
);

CREATE INDEX idx_equipment_serial ON equipment(serial_number);
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_category ON equipment(category);
CREATE INDEX idx_equipment_building ON equipment(location_building);
CREATE INDEX idx_equipment_assigned_to ON equipment(assigned_to);

CREATE TABLE maintenance_record (
    maintenance_id UUID PRIMARY KEY,
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id),
    type VARCHAR(50) NOT NULL,
    description TEXT,
    severity VARCHAR(20),
    scheduled_date DATE,
    completed_date DATE,
    result VARCHAR(50),
    notes TEXT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_maintenance_type CHECK (type IN ('PREVENTIVE', 'CORRECTIVE', 'EMERGENCY')),
    CONSTRAINT chk_maintenance_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_maintenance_equipment ON maintenance_record(equipment_id);
CREATE INDEX idx_maintenance_type ON maintenance_record(type);
CREATE INDEX idx_maintenance_created_at ON maintenance_record(created_at);

CREATE TABLE audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_status CHECK (status IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_action ON audit_log(action);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_resource ON audit_log(resource_type, resource_id);