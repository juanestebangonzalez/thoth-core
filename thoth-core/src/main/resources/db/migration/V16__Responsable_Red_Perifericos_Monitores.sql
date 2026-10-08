-- V16: Datos del responsable, red, perifericos por equipo y monitores asociados.
-- Idempotente: en produccion se ejecuta manualmente con psql (se puede correr mas de una vez).

-- 1) Responsable del equipo
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS responsible_position VARCHAR(100);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS responsible_document VARCHAR(30);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS responsible_phone    VARCHAR(10);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS responsible_email    VARCHAR(150);

-- 2) Red
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS ip_address    VARCHAR(45);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS ip_assignment VARCHAR(10);

-- 3) Monitor asociado a un equipo (FK a equipment; si se borra el equipo queda en NULL)
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS associated_equipment_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_equipment_associated_equipment'
    ) THEN
        ALTER TABLE equipment
            ADD CONSTRAINT fk_equipment_associated_equipment
            FOREIGN KEY (associated_equipment_id) REFERENCES equipment(equipment_id) ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_equipment_associated_equipment_id ON equipment(associated_equipment_id);

-- 4) Catalogo de tipos de periferico
CREATE TABLE IF NOT EXISTS peripheral_type (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO peripheral_type (name) VALUES ('TECLADO')                ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('MOUSE')                  ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('DIADEMA')                ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('USB')                    ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('ADAPTADOR DE RED USB')   ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('UNIDAD DE CD EXTERNA')   ON CONFLICT (name) DO NOTHING;
INSERT INTO peripheral_type (name) VALUES ('OTROS')                  ON CONFLICT (name) DO NOTHING;

-- 5) Perifericos de cada equipo
CREATE TABLE IF NOT EXISTS equipment_peripheral (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    equipment_id UUID NOT NULL REFERENCES equipment(equipment_id) ON DELETE CASCADE,
    type         VARCHAR(100) NOT NULL,
    brand        VARCHAR(100),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(100)
);
CREATE INDEX IF NOT EXISTS idx_equipment_peripheral_equipment_id ON equipment_peripheral(equipment_id);

-- 6) Los perifericos ya no se registran como tipo de dispositivo (se conservan los equipos existentes)
UPDATE device_type SET active = false WHERE UPPER(name) IN ('PERIFERICO', 'PERIFÉRICO', 'PERIPHERAL');
