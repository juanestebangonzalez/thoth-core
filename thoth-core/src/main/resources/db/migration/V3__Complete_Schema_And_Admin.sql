-- V3__Complete_Schema_And_Admin.sql
-- Completes schema for all entities missing from V1/V2 migrations
-- Creates default admin user for first deployment

-- ============================================================
-- 1. Fix audit_log table to match AuditLogEntity
--    V1 schema doesn't match the JPA entity, so we rebuild it
-- ============================================================
DROP TABLE IF EXISTS audit_log CASCADE;

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255),
    entity_name VARCHAR(255),
    details TEXT,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

CREATE INDEX idx_audit_log_module ON audit_log(module);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_performed_by ON audit_log(performed_by);
CREATE INDEX idx_audit_log_performed_at ON audit_log(performed_at);

-- ============================================================
-- 2. Audit log archive table
-- ============================================================
CREATE TABLE audit_log_archive (
    id UUID PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255),
    entity_name VARCHAR(255),
    details TEXT,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    archived_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_archive_performed_at ON audit_log_archive(performed_at);
CREATE INDEX idx_audit_archive_archived_at ON audit_log_archive(archived_at);

-- ============================================================
-- 3. Sede (hospital locations/branches)
-- ============================================================
CREATE TABLE sede (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    phone VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true
);

-- ============================================================
-- 4. Document (equipment documents/attachments)
-- ============================================================
CREATE TABLE document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    document_type VARCHAR(50) NOT NULL,
    description TEXT,
    uploaded_by VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_document_equipment ON document(equipment_id);
CREATE INDEX idx_document_type ON document(document_type);

-- ============================================================
-- 5. Location history (equipment transfers)
-- ============================================================
CREATE TABLE location_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id),
    from_building VARCHAR(100),
    from_floor VARCHAR(50),
    from_office VARCHAR(50),
    to_building VARCHAR(100) NOT NULL,
    to_floor VARCHAR(50),
    to_office VARCHAR(50),
    reason TEXT NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    transferred_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_location_equipment ON location_history(equipment_id);
CREATE INDEX idx_location_transferred ON location_history(transferred_at);

-- ============================================================
-- 6. Maintenance history (detailed maintenance records)
-- ============================================================
CREATE TABLE maintenance_history (
    maintenance_id UUID PRIMARY KEY,
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id),
    maintenance_type VARCHAR(30) NOT NULL,
    performed_date TIMESTAMP NOT NULL,
    technician_name VARCHAR(255) NOT NULL,
    technician_id UUID,
    reason TEXT NOT NULL,
    description TEXT,
    next_scheduled_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    signature_base64 TEXT,
    signed_by VARCHAR(100)
);

CREATE INDEX idx_maint_hist_equipment ON maintenance_history(equipment_id);
CREATE INDEX idx_maint_hist_type ON maintenance_history(maintenance_type);
CREATE INDEX idx_maint_hist_date ON maintenance_history(performed_date);

-- ============================================================
-- 7. Parts replaced during maintenance
-- ============================================================
CREATE TABLE part_replaced (
    part_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maintenance_id UUID NOT NULL REFERENCES maintenance_history(maintenance_id) ON DELETE CASCADE,
    part_name VARCHAR(255) NOT NULL,
    part_serial_number VARCHAR(100),
    reason TEXT
);

CREATE INDEX idx_part_maintenance ON part_replaced(maintenance_id);

-- ============================================================
-- 8. User permissions (granular module-level access)
-- ============================================================
CREATE TABLE user_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    CONSTRAINT uq_user_module_action UNIQUE (user_id, module, action)
);

CREATE INDEX idx_permission_user ON user_permission(user_id);

-- ============================================================
-- 9. Add missing column to app_user (from V2)
-- ============================================================
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS password_change_required BOOLEAN NOT NULL DEFAULT false;

-- ============================================================
-- 10. Default admin user
--     Password: ThothAdmin2026! (BCrypt hash)
--     IMPORTANT: Change this password on first login!
-- ============================================================
INSERT INTO app_user (id, username, password, email, role, enabled, password_change_required, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin',
    '$2b$10$l/n0xxr3rLh8L6fogq9tiOdAOjnvOcZNyrMPZBYhoYIKmibThiONi',
    'admin@thoth-core.local',
    'ADMIN',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;
