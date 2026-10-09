-- V17: Software y licenciamiento del sistema operativo (hoja de vida).
--      os_edition: WINDOWS 10 | WINDOWS 11 (solo con operating_system = 'WINDOWS').
--      os_license_type: OEM | RETAIL | VOLUMEN.
-- Idempotente: en produccion se ejecuta manualmente con psql (se puede correr mas de una vez).

ALTER TABLE equipment ADD COLUMN IF NOT EXISTS os_edition VARCHAR(30);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS os_license_type VARCHAR(20);

-- Datos existentes: deducir la edicion desde el texto libre de os_version
-- (se evalua '11' antes que '10'; solo filas WINDOWS sin edicion asignada).
UPDATE equipment
   SET os_edition = 'WINDOWS 11'
 WHERE operating_system = 'WINDOWS'
   AND os_edition IS NULL
   AND os_version ~ '11';

UPDATE equipment
   SET os_edition = 'WINDOWS 10'
 WHERE operating_system = 'WINDOWS'
   AND os_edition IS NULL
   AND os_version ~ '10';

-- Si la version contiene un codigo del catalogo (26H2, 26H1, 25H2, 24H2, 23H2), dejar solo ese codigo.
UPDATE equipment
   SET os_version = substring(upper(os_version) from '(26H2|26H1|25H2|24H2|23H2)')
 WHERE operating_system = 'WINDOWS'
   AND upper(os_version) ~ '(26H2|26H1|25H2|24H2|23H2)'
   AND upper(os_version) <> substring(upper(os_version) from '(26H2|26H1|25H2|24H2|23H2)');
