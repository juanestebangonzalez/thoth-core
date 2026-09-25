-- V10 (DT-13, parcial): indice sobre inventory_number
--
-- RegisterEquipmentUseCaseImpl verifica que el numero de inventario no
-- exista ya antes de registrar un equipo. Sin indice, esa verificacion
-- recorre la tabla completa en cada alta.
--
-- Los otros dos indices de DT-13 (next_maintenance_date y rental_end_date)
-- se crean junto con DT-19, cuando las alertas dejen de cargar la tabla
-- entera en memoria: antes de eso solo anadirian costo de escritura.

CREATE INDEX IF NOT EXISTS idx_equipment_inventory_number
    ON equipment(inventory_number)
    WHERE inventory_number IS NOT NULL;
