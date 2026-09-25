-- ============================================================
-- Reparacion: propiedad de las tablas y historial de Flyway
-- ============================================================
--
-- SINTOMA
--   El despliegue falla con:
--     PSQLException: ERROR: debe ser dueno de la tabla part_replaced
--
-- CAUSA
--   db/init/01-create-app-role.sh concede a thoth_app ALL PRIVILEGES
--   sobre las tablas, pero en PostgreSQL los PRIVILEGIOS NO SON PROPIEDAD.
--   ALTER TABLE exige ser el DUENO de la tabla (o superusuario), no basta
--   con tener todos los privilegios.
--
--   Las tablas quedaron a nombre de 'postgres', mientras que Flyway se
--   conecta como 'thoth_app'. Cualquier migracion que use ALTER TABLE o
--   DROP INDEX falla, no solo la V8.
--
-- EFECTO
--   Este script pone a thoth_app como dueno de todo lo que hay en el
--   esquema public y limpia las migraciones que quedaron marcadas como
--   fallidas, para que Flyway pueda reintentarlas.
--
-- COMO EJECUTARLO (en la VM, como superusuario de PostgreSQL)
--   sudo systemctl stop thoth-core
--   sudo -u postgres psql -d thoth_core -f reparar-propiedad-bd.sql
--   sudo systemctl start thoth-core
--
-- Es idempotente: se puede ejecutar varias veces sin efecto adicional.

\echo '=== ANTES: duenos actuales en el esquema public ==='
SELECT tablename AS tabla, tableowner AS dueno
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tableowner, tablename;

\echo ''
\echo '=== Traspasando la propiedad a thoth_app ==='

DO $$
DECLARE
    r        record;
    n_tablas integer := 0;
    n_secs   integer := 0;
    n_vistas integer := 0;
BEGIN
    FOR r IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
        EXECUTE format('ALTER TABLE public.%I OWNER TO thoth_app', r.tablename);
        n_tablas := n_tablas + 1;
    END LOOP;

    -- Los indices heredan el dueno de su tabla, no hay que tocarlos aparte.

    FOR r IN SELECT sequencename FROM pg_sequences WHERE schemaname = 'public' LOOP
        EXECUTE format('ALTER SEQUENCE public.%I OWNER TO thoth_app', r.sequencename);
        n_secs := n_secs + 1;
    END LOOP;

    FOR r IN SELECT viewname FROM pg_views WHERE schemaname = 'public' LOOP
        EXECUTE format('ALTER VIEW public.%I OWNER TO thoth_app', r.viewname);
        n_vistas := n_vistas + 1;
    END LOOP;

    RAISE NOTICE 'Tablas: %  Secuencias: %  Vistas: %', n_tablas, n_secs, n_vistas;
END
$$;

\echo ''
\echo '=== Migraciones marcadas como fallidas ==='
SELECT installed_rank, version, description, success
FROM flyway_schema_history
WHERE success = false;

-- Flyway se niega a continuar mientras haya una migracion fallida en el
-- historial. Como PostgreSQL ejecuta las migraciones dentro de una
-- transaccion, una migracion fallida no dejo cambios a medias: basta con
-- borrar su registro para que se reintente en el proximo arranque.
DELETE FROM flyway_schema_history WHERE success = false;

\echo ''
\echo '=== DESPUES: duenos en el esquema public ==='
SELECT tablename AS tabla, tableowner AS dueno
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;

\echo ''
\echo '=== Migraciones aplicadas ==='
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;

\echo ''
\echo 'Listo. Arranca la aplicacion: sudo systemctl start thoth-core'
