#!/usr/bin/env bash
#
# DT-01 - Restauracion de THOTH C.O.R.E.
#
# Un respaldo que nunca se ha restaurado no es un respaldo, es una suposicion.
# Este script debe ejecutarse al menos una vez por trimestre contra un entorno
# de pruebas, no contra produccion.
#
# USO
#   ./restaurar.sh /var/backups/thoth/20260925_033000
#
# ANTES DE EMPEZAR
#   1. Detener la aplicacion: sin esto, escribira sobre la base a medio restaurar.
#   2. Confirmar que DB_NAME apunta a donde se quiere restaurar. Este script
#      BORRA Y RECREA esa base de datos.

set -euo pipefail

DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "${DIR_SCRIPT}/.env" ]]; then
    set -a; source "${DIR_SCRIPT}/.env"; set +a
fi

ORIGEN="${1:-}"
if [[ -z "${ORIGEN}" ]]; then
    echo "USO: $0 <directorio-del-respaldo>" >&2
    echo "Ejemplo: $0 /var/backups/thoth/20260925_033000" >&2
    exit 1
fi

if [[ ! -f "${ORIGEN}/thoth_core.dump" ]]; then
    echo "ERROR: no se encontro ${ORIGEN}/thoth_core.dump" >&2
    exit 1
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-thoth_core}"
DB_USERNAME="${DB_USERNAME:-thoth_app}"
UPLOAD_DIR="${UPLOAD_DIR:-/opt/thoth/uploads}"

if [[ -z "${PGPASSWORD:-}" ]]; then
    echo "ERROR: PGPASSWORD no esta definida." >&2
    exit 1
fi

echo "=========================================================="
echo " RESTAURACION DE THOTH C.O.R.E."
echo "=========================================================="
cat "${ORIGEN}/MANIFIESTO.txt" 2>/dev/null || echo "(sin manifiesto)"
echo "=========================================================="
echo " DESTINO: base de datos '${DB_NAME}' en ${DB_HOST}:${DB_PORT}"
echo " ESTA BASE DE DATOS SERA BORRADA Y RECREADA."
echo "=========================================================="
read -r -p "Escribe RESTAURAR para continuar: " CONFIRMACION
if [[ "${CONFIRMACION}" != "RESTAURAR" ]]; then
    echo "Cancelado."
    exit 0
fi

echo "[$(date -Is)] Recreando la base de datos..."
dropdb   --host="${DB_HOST}" --port="${DB_PORT}" --username="${DB_USERNAME}" --if-exists "${DB_NAME}"
createdb --host="${DB_HOST}" --port="${DB_PORT}" --username="${DB_USERNAME}" "${DB_NAME}"

echo "[$(date -Is)] Restaurando el volcado..."
pg_restore \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --username="${DB_USERNAME}" \
    --dbname="${DB_NAME}" \
    --no-owner \
    --no-privileges \
    "${ORIGEN}/thoth_core.dump"

if [[ -f "${ORIGEN}/documentos.tar.gz" ]]; then
    echo "[$(date -Is)] Restaurando documentos en ${UPLOAD_DIR}..."
    mkdir -p "$(dirname "${UPLOAD_DIR}")"
    tar -xzf "${ORIGEN}/documentos.tar.gz" -C "$(dirname "${UPLOAD_DIR}")"
else
    echo "[$(date -Is)] AVISO: el respaldo no incluye documentos."
fi

echo "[$(date -Is)] Restauracion completada."
echo ""
echo "VERIFICACION OBLIGATORIA - la restauracion no cuenta hasta hacerla:"
echo "  1. Arrancar la aplicacion y comprobar que inicia sin errores de Flyway."
echo "  2. Iniciar sesion."
echo "  3. Abrir el listado de equipos y comprobar que los datos estan."
echo "  4. Abrir un equipo con documentos y descargar uno."
