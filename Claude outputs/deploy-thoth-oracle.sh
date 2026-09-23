#!/bin/bash
# ============================================================
# THOTH C.O.R.E. - Script de Despliegue en Oracle Cloud
# Ejecutar como: sudo bash deploy-thoth-oracle.sh
# ============================================================
set -e

echo "=========================================="
echo "  THOTH C.O.R.E. - Despliegue Oracle VM"
echo "=========================================="

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ============================================================
# 1. ACTUALIZAR SISTEMA
# ============================================================
echo -e "${GREEN}[1/8] Actualizando sistema...${NC}"
apt-get update -qq
apt-get upgrade -y -qq
apt-get install -y -qq curl wget git unzip software-properties-common

# ============================================================
# 2. INSTALAR JAVA 21
# ============================================================
echo -e "${GREEN}[2/8] Instalando Java 21...${NC}"
# Detectar arquitectura (ARM o x86)
ARCH=$(dpkg --print-architecture)
echo "  Arquitectura detectada: $ARCH"

if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "aarch64" ]; then
    # ARM - usar repositorio de Ubuntu
    apt-get install -y -qq openjdk-21-jdk-headless
else
    apt-get install -y -qq openjdk-21-jdk-headless
fi

java -version
echo -e "${GREEN}  ✓ Java 21 instalado${NC}"

# ============================================================
# 3. INSTALAR POSTGRESQL 16
# ============================================================
echo -e "${GREEN}[3/8] Instalando PostgreSQL 16...${NC}"
sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc | gpg --dearmor -o /etc/apt/trusted.gpg.d/postgresql.gpg
apt-get update -qq
apt-get install -y -qq postgresql-16

systemctl enable postgresql
systemctl start postgresql
echo -e "${GREEN}  ✓ PostgreSQL 16 instalado${NC}"

# ============================================================
# 4. CONFIGURAR BASE DE DATOS
# ============================================================
echo -e "${GREEN}[4/8] Configurando base de datos...${NC}"

# Generar contraseña segura
DB_PASSWORD=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 20)
JWT_SECRET=$(openssl rand -base64 48 | tr -dc 'a-zA-Z0-9' | head -c 64)

sudo -u postgres psql <<SQL
-- Crear usuario
CREATE USER thoth_app WITH PASSWORD '${DB_PASSWORD}';

-- Crear base de datos
CREATE DATABASE thoth_core OWNER thoth_app;

-- Permisos
GRANT ALL PRIVILEGES ON DATABASE thoth_core TO thoth_app;
\c thoth_core
GRANT ALL ON SCHEMA public TO thoth_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO thoth_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO thoth_app;
SQL

echo -e "${GREEN}  ✓ Base de datos thoth_core creada${NC}"
echo -e "${YELLOW}  DB Password: ${DB_PASSWORD}${NC}"

# ============================================================
# 5. INSTALAR NGINX
# ============================================================
echo -e "${GREEN}[5/8] Instalando Nginx...${NC}"
apt-get install -y -qq nginx
systemctl enable nginx
echo -e "${GREEN}  ✓ Nginx instalado${NC}"

# ============================================================
# 6. CLONAR Y COMPILAR EL BACKEND
# ============================================================
echo -e "${GREEN}[6/8] Clonando y compilando el backend...${NC}"

# Crear usuario para la app
useradd -r -m -s /bin/bash thoth 2>/dev/null || true

cd /opt
git clone https://github.com/juanestebangonzalez/thoth-core.git thoth-app 2>/dev/null || {
    cd /opt/thoth-app && git pull origin main
}
cd /opt/thoth-app/thoth-core

# Dar permisos al gradlew
chmod +x gradlew

# Compilar
echo "  Compilando (esto puede tomar unos minutos)..."
./gradlew bootJar --no-daemon -q 2>&1 | tail -5

# Encontrar el JAR
JAR_FILE=$(ls build/libs/*.jar 2>/dev/null | grep -v plain | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No se encontró el JAR compilado"
    exit 1
fi
echo -e "${GREEN}  ✓ Backend compilado: $JAR_FILE${NC}"

# Copiar JAR a ubicación final
mkdir -p /opt/thoth
cp "$JAR_FILE" /opt/thoth/thoth-core.jar
chown -R thoth:thoth /opt/thoth

# Crear directorio para uploads
mkdir -p /opt/thoth/uploads
chown thoth:thoth /opt/thoth/uploads

# ============================================================
# 7. CREAR ARCHIVO DE CONFIGURACIÓN Y SERVICIO
# ============================================================
echo -e "${GREEN}[7/8] Configurando servicio...${NC}"

# Archivo .env para el servicio
cat > /opt/thoth/.env <<ENV
DB_HOST=localhost
DB_PORT=5432
DB_NAME=thoth_core
DB_USERNAME=thoth_app
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET}
CORS_ORIGINS=*
SPRING_PROFILES_ACTIVE=prod
ENV

chown thoth:thoth /opt/thoth/.env
chmod 600 /opt/thoth/.env

# Servicio systemd
cat > /etc/systemd/system/thoth-core.service <<SERVICE
[Unit]
Description=THOTH C.O.R.E. Backend
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=thoth
Group=thoth
WorkingDirectory=/opt/thoth
EnvironmentFile=/opt/thoth/.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/thoth/thoth-core.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=thoth-core

[Install]
WantedBy=multi-user.target
SERVICE

systemctl daemon-reload
systemctl enable thoth-core
systemctl start thoth-core

echo -e "${GREEN}  ✓ Servicio thoth-core creado y arrancado${NC}"

# ============================================================
# 8. CONFIGURAR NGINX REVERSE PROXY
# ============================================================
echo -e "${GREEN}[8/8] Configurando Nginx...${NC}"

# Obtener IP pública
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || echo "TU_IP")

cat > /etc/nginx/sites-available/thoth-core <<NGINX
server {
    listen 80;
    server_name ${PUBLIC_IP} _;

    # Tamaño máximo de upload (para documentos)
    client_max_body_size 10M;

    # Headers de seguridad
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # CORS headers para el frontend
        if (\$request_method = 'OPTIONS') {
            add_header 'Access-Control-Allow-Origin' '*' always;
            add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, PATCH, DELETE, OPTIONS' always;
            add_header 'Access-Control-Allow-Headers' 'Authorization, Content-Type' always;
            add_header 'Access-Control-Max-Age' 3600;
            return 204;
        }
    }

    # Health check
    location /health {
        proxy_pass http://localhost:8080/actuator/health;
    }
}
NGINX

# Activar el sitio
ln -sf /etc/nginx/sites-available/thoth-core /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

echo -e "${GREEN}  ✓ Nginx configurado${NC}"

# ============================================================
# ABRIR PUERTOS EN IPTABLES (Oracle Ubuntu)
# ============================================================
echo -e "${GREEN}  Abriendo puertos en firewall...${NC}"
iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
netfilter-persistent save 2>/dev/null || iptables-save > /etc/iptables/rules.v4 2>/dev/null || true

# ============================================================
# RESUMEN FINAL
# ============================================================
echo ""
echo "=========================================="
echo -e "${GREEN}  ¡DESPLIEGUE COMPLETADO!${NC}"
echo "=========================================="
echo ""
echo "  IP Pública:     ${PUBLIC_IP}"
echo "  Backend API:    http://${PUBLIC_IP}/api/v1"
echo "  Health Check:   http://${PUBLIC_IP}/health"
echo ""
echo "  Base de datos:"
echo "    Host:     localhost"
echo "    DB:       thoth_core"
echo "    Usuario:  thoth_app"
echo "    Password: ${DB_PASSWORD}"
echo ""
echo "  JWT Secret: ${JWT_SECRET}"
echo ""
echo "  Usuario admin por defecto:"
echo "    Username: admin"
echo "    Password: ThothAdmin2026!"
echo "    (Se pedirá cambio de contraseña al primer login)"
echo ""
echo "  Comandos útiles:"
echo "    Ver logs:     sudo journalctl -u thoth-core -f"
echo "    Reiniciar:    sudo systemctl restart thoth-core"
echo "    Estado:       sudo systemctl status thoth-core"
echo ""
echo -e "${YELLOW}  ⚠ IMPORTANTE: Guarda las contraseñas de arriba en un lugar seguro${NC}"
echo -e "${YELLOW}  ⚠ Luego actualiza el frontend con la URL: http://${PUBLIC_IP}${NC}"
echo "=========================================="
