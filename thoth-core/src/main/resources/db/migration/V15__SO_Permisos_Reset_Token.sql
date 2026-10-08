-- V15: Sistema operativo del equipo, token de restablecimiento de contrasena,
--      normalizacion de tipos de mantenimiento y permisos de los nuevos modulos.
-- Idempotente: en produccion se ejecuta manualmente con psql (se puede correr mas de una vez).

-- 1) Sistema operativo del equipo
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS operating_system VARCHAR(30);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS os_version VARCHAR(100);

-- 2) Tokens de restablecimiento de contrasena (solo se guarda el hash SHA-256 en hex)
CREATE TABLE IF NOT EXISTS password_reset_token (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash  VARCHAR(128) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_password_reset_token_user_id ON password_reset_token(user_id);

-- 3) Tipos de mantenimiento historicos en ingles -> codigos del catalogo en espanol
UPDATE maintenance_history SET maintenance_type = 'PREVENTIVO' WHERE maintenance_type = 'PREVENTIVE';
UPDATE maintenance_history SET maintenance_type = 'CORRECTIVO' WHERE maintenance_type = 'CORRECTIVE';

-- 4) Permisos por defecto de los nuevos modulos (CATALOGS, IMPORT, AUDIT, RENTALS)
--    Solo para usuarios que aun no tienen ningun permiso en estos modulos.
INSERT INTO user_permission (id, user_id, module, action)
SELECT gen_random_uuid(), u.id, d.module, d.action
FROM app_user u
JOIN (VALUES
    ('ADMIN', 'CATALOGS', 'VIEW'), ('ADMIN', 'CATALOGS', 'CREATE'), ('ADMIN', 'CATALOGS', 'EDIT'), ('ADMIN', 'CATALOGS', 'DELETE'),
    ('ADMIN', 'IMPORT', 'VIEW'),   ('ADMIN', 'IMPORT', 'CREATE'),   ('ADMIN', 'IMPORT', 'EDIT'),   ('ADMIN', 'IMPORT', 'DELETE'),
    ('ADMIN', 'AUDIT', 'VIEW'),    ('ADMIN', 'AUDIT', 'CREATE'),    ('ADMIN', 'AUDIT', 'EDIT'),    ('ADMIN', 'AUDIT', 'DELETE'),
    ('ADMIN', 'RENTALS', 'VIEW'),  ('ADMIN', 'RENTALS', 'CREATE'),  ('ADMIN', 'RENTALS', 'EDIT'),  ('ADMIN', 'RENTALS', 'DELETE'),
    ('TECHNICIAN', 'CATALOGS', 'VIEW'),
    ('TECHNICIAN', 'IMPORT', 'VIEW'), ('TECHNICIAN', 'IMPORT', 'CREATE'),
    ('TECHNICIAN', 'AUDIT', 'VIEW'),
    ('TECHNICIAN', 'RENTALS', 'VIEW'),
    ('USER', 'RENTALS', 'VIEW'),
    ('VIEWER', 'RENTALS', 'VIEW')
) AS d(role, module, action) ON d.role = u.role
WHERE NOT EXISTS (
    SELECT 1 FROM user_permission p
    WHERE p.user_id = u.id AND p.module IN ('CATALOGS', 'IMPORT', 'AUDIT', 'RENTALS')
)
ON CONFLICT (user_id, module, action) DO NOTHING;
