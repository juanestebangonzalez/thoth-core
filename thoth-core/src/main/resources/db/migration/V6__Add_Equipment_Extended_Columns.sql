-- V6: Add extended columns to equipment table
-- Adds: inventory_number, ownership/rental fields, hardware specs, next_maintenance_date
-- All columns are nullable to not break existing data
-- Uses IF NOT EXISTS for idempotency

-- ============================================================
-- 1. Inventory number
-- ============================================================
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS inventory_number VARCHAR(100);

-- ============================================================
-- 2. Ownership type (OWNED / RENTED)
-- ============================================================
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS ownership_type VARCHAR(20);

-- ============================================================
-- 3. Rental information
-- ============================================================
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_company VARCHAR(255);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_contact_name VARCHAR(255);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_contact_phone VARCHAR(100);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_contact_email VARCHAR(255);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_start_date DATE;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_end_date DATE;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_contract_number VARCHAR(100);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_contract_file_url VARCHAR(500);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_notes TEXT;

-- ============================================================
-- 4. Hardware specifications
-- ============================================================
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_processor VARCHAR(255);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_ram_size_gb INTEGER;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_ram_type VARCHAR(20);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_disk_type VARCHAR(20);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_disk_size_gb INTEGER;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_disk_health_percent INTEGER;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS hardware_disk_temperature_celsius INTEGER;

-- ============================================================
-- 5. Next maintenance date
-- ============================================================
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS next_maintenance_date DATE;

-- ============================================================
-- 6. Make location_building nullable (was NOT NULL in V1,
--    but new equipment may not have location yet)
-- ============================================================
ALTER TABLE equipment ALTER COLUMN location_building DROP NOT NULL;
