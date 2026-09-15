#!/bin/bash
# SEC-006 fix: the app must never connect as the postgres superuser.
# This runs once, as the bootstrap superuser, only on first init of an empty
# data volume (standard docker-entrypoint-initdb.d behavior), and creates a
# dedicated least-privilege role for the application.
set -e

: "${APP_DB_USER:?APP_DB_USER must be set}"
: "${APP_DB_PASSWORD:?APP_DB_PASSWORD must be set}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${APP_DB_USER}') THEN
            CREATE ROLE "${APP_DB_USER}" LOGIN PASSWORD '${APP_DB_PASSWORD}';
        END IF;
    END
    \$\$;

    GRANT CONNECT ON DATABASE "${POSTGRES_DB}" TO "${APP_DB_USER}";
    GRANT USAGE, CREATE ON SCHEMA public TO "${APP_DB_USER}";
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "${APP_DB_USER}";
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "${APP_DB_USER}";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO "${APP_DB_USER}";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO "${APP_DB_USER}";
EOSQL
