#!/usr/bin/env bash
#
# DT-01 - Respaldo de THOTH C.O.R.E.
#
# Respalda la base de datos PostgreSQL y el directorio de documentos adjuntos.
# Los documentos viven en el sistema de archivos y solo sus metadatos estan en
# la base: un respaldo sin ellos deja registros apuntando a archivos que no
# existen.
#
# USO
#   ./respaldo.sh
#
# CONFIGURACION - variables de entorno (o un archivo .env junto a este script)
#   DB_HOST         servidor de base de datos        (por defecto localhost)
#   DB_PORT         puerto                           (por defecto 5432)
#   DB_NAME         nombre de la base                (por defecto thoth_core)
#   DB_USERNAME     usuario                          (por defecto thoth_app)
#   PGPASSWORD      contrasena - OBLIGATORIA
#   UPLOAD_DIR      directorio de documentos         (por defecto /opt/thoth/uploads)
#   BACKUP_DIR      destino de los respaldos         (por defecto /var/backups/thoth)
#   RETENCION_DIAS  dias de retencion diaria         (por defecto 30)
#
# INSTALACION COMO TAREA DIARIA (3:30 AM)
#   sudo cp respaldo.sh /usr/local/bin/thoth-respaldo
#   sudo chmod +x /usr/local/bin/thoth-respaldo
#   sudo crontab -e
#     30 3 * * * /usr/local/bin/thoth-respaldo >> /var/log/thoth-respaldo.log 2>&1
#
# IMPORTANTE
#   Un respaldo en la misma maquina que la base de datos NO protege contra el
#   fallo de esa maquina. Copiar BACKUP_DIR a otro destino (otro servidor,
#   almacenamiento de objetos) es parte imprescindible de este procedimiento.
#
#   Un respaldo que nunca se ha restaurado no es un respaldo. Ver restaurar.sh.

set -euo pipefail

DIR_SCRIPT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -f "${DIR_SCRIPT}/.env" ]]; then
    set -a; source "${DIR_SCRIPT}/.env"; set +a
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-thoth_core}"
DB_USERNAME="${DB_USERNAME:-thoth_app}"
UPLOAD_DIR="${UPLOAD_DIR:-/opt/thoth/uploads}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/thoth}"
RETENCION_DIAS="${RETENCION_DIAS:-30}"

if [[ -z "${PGPASSWORD:-}" ]]; then
    echo "ERROR: PGPASSWORD no esta definida. Sin ella pg_dump no puede conectarse." >&2
    exit 1
fi

MARCA="$(date +%Y%m%d_%H%M%S)"
DESTINO="${BACKUP_DIR}/${MARCA}"
mkdir -p "${DESTINO}"

echo "[$(date -Is)] Iniciando respaldo -> ${DESTINO}"

# --- 1. Base de datos ---------------------------------------------------
echo "[$(date -Is)] Volcando la base de datos ${DB_NAME}..."
pg_dump \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --username="${DB_USERNAME}" \
    --dbname="${DB_NAME}" \
    --format=custom \
    --file="${DESTINO}/thoth_core.dump"

TAM_BD=$(du -h "${DESTINO}/thoth_core.dump" | cut -f1)
echo "[$(date -Is)] Base de datos respaldada (${TAM_BD})"

# --- 2. Documentos adjuntos ---------------------------------------------
if [[ -d "${UPLOAD_DIR}" ]]; then
    echo "[$(date -Is)] Comprimiendo documentos de ${UPLOAD_DIR}..."
    tar -czf "${DESTINO}/documentos.tar.gz" -C "$(dirname "${UPLOAD_DIR}")" "$(basename "${UPLOAD_DIR}")"
    TAM_DOCS=$(du -h "${DESTINO}/documentos.tar.gz" | cut -f1)
    echo "[$(date -Is)] Documentos respaldados (${TAM_DOCS})"
else
    echo "[$(date -Is)] AVISO: ${UPLOAD_DIR} no existe. No se respaldaron documentos."
    echo "[$(date -Is)] AVISO: si la aplicacion guarda adjuntos, corrige UPLOAD_DIR."
fi

# --- 3. Manifiesto -------------------------------------------------------
cat > "${DESTINO}/MANIFIESTO.txt" <<EOF
Respaldo de THOTH C.O.R.E.
Fecha:            $(date -Is)
Servidor:         $(hostname)
Base de datos:    ${DB_NAME} en ${DB_HOST}:${DB_PORT}
Documentos:       ${UPLOAD_DIR}
Formato del dump: custom (restaurar con pg_restore)

Para restaurar, ver restaurar.sh en el mismo directorio.
EOF

# --- 4. Verificacion minima ---------------------------------------------
# Un dump vacio o truncado pasaria desapercibido sin esto.
if ! pg_restore --list "${DESTINO}/thoth_core.dump" > /dev/null 2>&1; then
    echo "[$(date -Is)] ERROR: el dump no es legible por pg_restore. Respaldo INVALIDO." >&2
    exit 1
fi
echo "[$(date -Is)] Dump verificado: pg_restore puede leerlo"

# --- 5. Retencion --------------------------------------------------------
echo "[$(date -Is)] Eliminando respaldos con mas de ${RETENCION_DIAS} dias..."
find "${BACKUP_DIR}" -maxdepth 1 -type d -name '20*' -mtime "+${RETENCION_DIAS}" -exec rm -rf {} + 2>/dev/null || true

TOTAL=$(du -sh "${DESTINO}" | cut -f1)
echo "[$(date -Is)] Respaldo completado: ${DESTINO} (${TOTAL})"
echo "[$(date -Is)] RECORDATORIO: copiar este respaldo FUERA de este servidor."
