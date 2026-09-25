-- DT-17: Eliminar tabla muerta maintenance_record
-- Esta tabla nunca fue escrita por la aplicación. El ReportsController ahora
-- obtiene los datos de "Planeados vs Cumplidos" desde maintenance_history.
DROP TABLE IF EXISTS maintenance_record;
