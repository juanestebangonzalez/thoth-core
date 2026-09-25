-- V11 (DT-14): restricciones de rango en los datos de hardware
--
-- Las alertas de hardware critico se calculan sobre estos valores
-- (salud < 30%, temperatura >= 70C, RAM < 4GB). Un dato fuera de rango
-- genera una alerta falsa o impide que se genere una verdadera.
--
-- Los DTO de entrada ya validan estos rangos, pero la base de datos no,
-- asi que cualquier carga directa por SQL puede insertar basura.
--
-- PASO 1: sanear los datos existentes que violen los rangos.
--         Se ponen a NULL en lugar de recortarse: NULL significa
--         "no registrado", que es honesto; un valor recortado seria
--         un dato inventado.

UPDATE equipment SET hardware_ram_size_gb = NULL
    WHERE hardware_ram_size_gb IS NOT NULL
      AND (hardware_ram_size_gb < 0 OR hardware_ram_size_gb > 1024);

UPDATE equipment SET hardware_disk_size_gb = NULL
    WHERE hardware_disk_size_gb IS NOT NULL
      AND (hardware_disk_size_gb < 0 OR hardware_disk_size_gb > 100000);

UPDATE equipment SET hardware_disk_health_percent = NULL
    WHERE hardware_disk_health_percent IS NOT NULL
      AND (hardware_disk_health_percent < 0 OR hardware_disk_health_percent > 100);

UPDATE equipment SET hardware_disk_temperature_celsius = NULL
    WHERE hardware_disk_temperature_celsius IS NOT NULL
      AND (hardware_disk_temperature_celsius < -50 OR hardware_disk_temperature_celsius > 200);

-- PASO 2: anadir las restricciones. Los rangos coinciden con los que ya
--         declara HardwareRequest, de modo que la BD y la API validan igual.

ALTER TABLE equipment DROP CONSTRAINT IF EXISTS chk_hw_ram_size;
ALTER TABLE equipment ADD CONSTRAINT chk_hw_ram_size
    CHECK (hardware_ram_size_gb IS NULL OR (hardware_ram_size_gb >= 0 AND hardware_ram_size_gb <= 1024));

ALTER TABLE equipment DROP CONSTRAINT IF EXISTS chk_hw_disk_size;
ALTER TABLE equipment ADD CONSTRAINT chk_hw_disk_size
    CHECK (hardware_disk_size_gb IS NULL OR (hardware_disk_size_gb >= 0 AND hardware_disk_size_gb <= 100000));

ALTER TABLE equipment DROP CONSTRAINT IF EXISTS chk_hw_disk_health;
ALTER TABLE equipment ADD CONSTRAINT chk_hw_disk_health
    CHECK (hardware_disk_health_percent IS NULL OR (hardware_disk_health_percent >= 0 AND hardware_disk_health_percent <= 100));

ALTER TABLE equipment DROP CONSTRAINT IF EXISTS chk_hw_disk_temp;
ALTER TABLE equipment ADD CONSTRAINT chk_hw_disk_temp
    CHECK (hardware_disk_temperature_celsius IS NULL OR (hardware_disk_temperature_celsius >= -50 AND hardware_disk_temperature_celsius <= 200));
