-- V5: Make purchase_date and purchase_value optional, seed device_type table

-- 1. Drop NOT NULL constraints on purchase fields
ALTER TABLE equipment ALTER COLUMN purchase_date DROP NOT NULL;
ALTER TABLE equipment ALTER COLUMN purchase_value DROP NOT NULL;

-- 2. Seed device_type table with standard categories (skip if already exist)
INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'DESKTOP', 'Computador de escritorio', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'DESKTOP');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'LAPTOP', 'Computador portátil', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'LAPTOP');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'SERVER', 'Servidor', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'SERVER');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'PRINTER', 'Impresora', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'PRINTER');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'NETWORK', 'Dispositivo de red', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'NETWORK');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'PERIPHERAL', 'Periférico', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'PERIPHERAL');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'STORAGE', 'Almacenamiento', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'STORAGE');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'MONITOR', 'Monitor', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'MONITOR');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'UPS', 'UPS / Sistema de alimentación ininterrumpida', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'UPS');

INSERT INTO device_type (id, name, description, active, created_at, updated_at)
SELECT gen_random_uuid(), 'OTHER', 'Otro tipo de equipo', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM device_type WHERE UPPER(name) = 'OTHER');
