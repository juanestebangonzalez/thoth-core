-- V13: Equipos con campos opcionales (solo nombre y categoria obligatorios),
--      segunda MAC para portatiles y catalogo de areas.
-- Es idempotente: algunos de estos cambios ya se aplicaron manualmente en produccion.

-- 1) Columnas de equipment opcionales
ALTER TABLE equipment ALTER COLUMN serial_number     DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN purchase_date     DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN purchase_value    DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN location_building DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN location_floor    DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN location_office   DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN brand             DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN model             DROP NOT NULL;

-- 1b) Area (antes "oficina"): se amplia a 255 para admitir nombres largos del catalogo
ALTER TABLE equipment        ALTER COLUMN location_office TYPE VARCHAR(255);
ALTER TABLE location_history ALTER COLUMN from_office     TYPE VARCHAR(255);
ALTER TABLE location_history ALTER COLUMN to_office       TYPE VARCHAR(255);

-- 1c) Cadenas vacias -> NULL en columnas con unicidad (evita choques del UNIQUE de serial_number)
UPDATE equipment SET serial_number    = NULL WHERE serial_number    IS NOT NULL AND TRIM(serial_number)    = '';
UPDATE equipment SET inventory_number = NULL WHERE inventory_number IS NOT NULL AND TRIM(inventory_number) = '';

-- 2) CHECK constraints heredados de enums antiguos (los valores ahora vienen de catalogos)
ALTER TABLE equipment DROP CONSTRAINT IF EXISTS equipment_category_check;
ALTER TABLE equipment DROP CONSTRAINT IF EXISTS equipment_status_check;
ALTER TABLE equipment DROP CONSTRAINT IF EXISTS equipment_ownership_type_check;
ALTER TABLE equipment DROP CONSTRAINT IF EXISTS equipment_hardware_disk_type_check;
ALTER TABLE equipment DROP CONSTRAINT IF EXISTS equipment_hardware_ram_type_check;
ALTER TABLE maintenance_history DROP CONSTRAINT IF EXISTS maintenance_history_maintenance_type_check;

-- 3) Columnas de part_replaced (agregadas a mano en produccion)
ALTER TABLE part_replaced ADD COLUMN IF NOT EXISTS purchase_date DATE;
ALTER TABLE part_replaced ADD COLUMN IF NOT EXISTS ticket_number VARCHAR(255);

-- 4) Segunda MAC (WiFi) para portatiles
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS mac_address_2 VARCHAR(255);

-- 5) Catalogo de areas
CREATE TABLE IF NOT EXISTS area (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

-- Sembrar areas con los valores de oficina ya usados en equipos existentes
INSERT INTO area (name)
SELECT DISTINCT UPPER(TRIM(location_office))
FROM equipment
WHERE location_office IS NOT NULL AND TRIM(location_office) <> ''
ON CONFLICT (name) DO NOTHING;
