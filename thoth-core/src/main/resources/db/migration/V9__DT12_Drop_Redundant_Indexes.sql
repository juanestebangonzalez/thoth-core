-- V9 (DT-12): eliminar indices redundantes
--
-- PostgreSQL ya crea un indice implicito por cada restriccion UNIQUE.
-- Estos tres indices lo duplican: ocupan espacio y ralentizan cada
-- escritura sin aportar nada en lectura.
--
--   equipment.serial_number  -> UNIQUE creada en V1
--   app_user.username        -> UNIQUE creada en V2
--   app_user.email           -> UNIQUE creada en V2

DROP INDEX IF EXISTS idx_equipment_serial;
DROP INDEX IF EXISTS idx_user_username;
DROP INDEX IF EXISTS idx_user_email;
