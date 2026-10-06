-- V14: Catalogo de centros de costo, centro de costo en equipos y valor mensual de alquiler.
-- Idempotente: en produccion se ejecuta manualmente con psql.

-- 1) Catalogo de centros de costo
CREATE TABLE IF NOT EXISTS cost_center (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO cost_center (name) VALUES ('ASISTENCIAL')    ON CONFLICT (name) DO NOTHING;
INSERT INTO cost_center (name) VALUES ('ADMINISTRATIVO') ON CONFLICT (name) DO NOTHING;
INSERT INTO cost_center (name) VALUES ('OTRO')           ON CONFLICT (name) DO NOTHING;

-- 2) Centro de costo del equipo (nombre del catalogo)
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS cost_center VARCHAR(100);

-- 3) Valor mensual del alquiler
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS rental_monthly_value NUMERIC(12,2);
