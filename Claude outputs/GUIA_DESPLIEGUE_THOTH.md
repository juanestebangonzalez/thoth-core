# THOTH C.O.R.E. — Guía de Despliegue Completo

## Paso 0: Subir el código a GitHub (desde tu PC Windows)

El commit con todos los cambios no se pudo pushear desde la nube. Tienes dos opciones:

### Opción A — Usando el patch (recomendada)

1. Descarga el archivo `thoth-core-latest.patch` de esta conversación
2. Abre **Git Bash** en tu PC donde tengas el repo clonado
3. Ejecuta:

```bash
cd /ruta/a/tu/thoth-core
git am < /ruta/al/thoth-core-latest.patch
git push origin main
```

### Opción B — Usando el bundle (si NO tienes el repo en tu PC)

1. Descarga `thoth-core-full.bundle`
2. En Git Bash:

```bash
cd ~/Desktop
git clone thoth-core-full.bundle thoth-core
cd thoth-core
git remote set-url origin https://github.com/juanestebangonzalez/thoth-core.git
git push origin main --force
```

---

## Paso 1: Preparar el Servidor (VM Oracle Cloud)

Conéctate por SSH:

```bash
ssh -i "C:\Users\JuanEstebanGonzalez\Downloads\Nueva carpeta (2)\ssh-key-2026-01-17.key" ubuntu@129.148.44.87
```

### 1.1 Instalar Java 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
java -version
```

### 1.2 Clonar el repositorio

```bash
cd ~
git clone https://github.com/juanestebangonzalez/thoth-core.git
cd thoth-core
```

### 1.3 Compilar el JAR

```bash
cd ~/thoth-core/thoth-core
chmod +x ../gradlew
../gradlew bootJar
```

El JAR queda en: `~/thoth-core/thoth-core/build/libs/thoth-core-0.0.1-SNAPSHOT.jar`

---

## Paso 2: Configurar la Base de Datos

Las tablas principales se crean automáticamente con Flyway al iniciar la app. Pero necesitas verificar que PostgreSQL esté corriendo y la DB exista:

```bash
# Verificar que PostgreSQL corre
sudo systemctl status postgresql

# Verificar la base de datos (como usuario postgres)
sudo -u postgres psql -c "\l" | grep thoth_core
```

Si la DB **no existe**, créala:

```bash
sudo -u postgres psql <<'EOF'
CREATE DATABASE thoth_core;
CREATE USER thoth_app WITH PASSWORD 'Th0th_Pr0d_2026!';
GRANT ALL PRIVILEGES ON DATABASE thoth_core TO thoth_app;
ALTER DATABASE thoth_core OWNER TO thoth_app;
\c thoth_core
GRANT ALL ON SCHEMA public TO thoth_app;
EOF
```

Si PostgreSQL **no está instalado**:

```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql
# Luego ejecuta los comandos de arriba para crear DB y usuario
```

---

## Paso 3: Crear el archivo de variables de entorno

```bash
sudo mkdir -p /etc/thoth
sudo tee /etc/thoth/thoth.env > /dev/null <<'EOF'
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/thoth_core
SPRING_DATASOURCE_USERNAME=thoth_app
SPRING_DATASOURCE_PASSWORD=Th0th_Pr0d_2026!
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
JWT_SECRET=D56M2RfckHjXEN3MwRv6jkCEB0BhPEW1BeZac711JAcjy7ZQiDrMNDMuC4cxbr
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://thoth-core.netlify.app
SPRINGDOC_SWAGGER_UI_ENABLED=false
SERVER_PORT=8080
EOF

sudo chmod 600 /etc/thoth/thoth.env
```

---

## Paso 4: Crear el servicio systemd

```bash
sudo tee /etc/systemd/system/thoth.service > /dev/null <<'EOF'
[Unit]
Description=THOTH C.O.R.E. Backend
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=ubuntu
Group=ubuntu
EnvironmentFile=/etc/thoth/thoth.env
WorkingDirectory=/home/ubuntu/thoth-core/thoth-core
ExecStart=/usr/bin/java -jar /home/ubuntu/thoth-core/thoth-core/build/libs/thoth-core-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  --spring.datasource.url=${SPRING_DATASOURCE_URL} \
  --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
  --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
  --spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO} \
  --spring.flyway.enabled=${SPRING_FLYWAY_ENABLED} \
  --jwt.secret=${JWT_SECRET} \
  --jwt.expiration=${JWT_EXPIRATION} \
  --cors.allowed-origins=${CORS_ALLOWED_ORIGINS} \
  --springdoc.swagger-ui.enabled=${SPRINGDOC_SWAGGER_UI_ENABLED} \
  --server.port=${SERVER_PORT}
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable thoth
```

---

## Paso 5: Iniciar el servicio

```bash
sudo systemctl start thoth
```

### Verificar que arrancó correctamente:

```bash
# Ver logs en tiempo real
sudo journalctl -u thoth -f

# Esperar ~30 segundos y probar
curl http://localhost:8080/api/v1/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"ThothAdmin2026!"}'
```

Deberías recibir un JSON con el token JWT.

---

## Paso 6: Abrir el puerto 8080 en Oracle Cloud

Si aún no lo has hecho, necesitas abrir el puerto en el firewall del servidor Y en las Security Lists de Oracle Cloud:

### Firewall del servidor:

```bash
sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
sudo apt install -y iptables-persistent
sudo netfilter-persistent save
```

### Oracle Cloud Console:

1. Ve a **Networking > Virtual Cloud Networks > tu VCN > Security Lists**
2. Agrega una Ingress Rule:
   - Source CIDR: `0.0.0.0/0`
   - Protocol: TCP
   - Destination Port: `8080`

---

## Paso 7: Desplegar Frontend en Netlify

El frontend ya está configurado en https://thoth-core.netlify.app. Para actualizar:

1. En tu PC, ve a la carpeta del frontend:

```bash
cd thoth-core/thothCoreFrontend/thothCoreFrontend
npm install
npx ng build --configuration=production
```

2. Sube la carpeta `dist/thoth-core-frontend/browser` a Netlify (arrastra y suelta en el dashboard, o usa Netlify CLI).

El archivo `netlify.toml` ya tiene el proxy configurado para redirigir `/api/*` al backend:

```toml
[[redirects]]
  from = "/api/*"
  to = "http://129.148.44.87:8080/api/:splat"
  status = 200
  force = true
```

---

## Comandos Útiles Post-Despliegue

```bash
# Ver estado del servicio
sudo systemctl status thoth

# Ver logs
sudo journalctl -u thoth -n 100

# Reiniciar después de actualizar
cd ~/thoth-core && git pull origin main
cd thoth-core && ../gradlew bootJar
sudo systemctl restart thoth

# Ver tablas en la DB
sudo -u postgres psql -d thoth_core -c "\dt"
```

---

## Credenciales

| Recurso | Usuario | Contraseña |
|---------|---------|------------|
| App Admin | admin | ThothAdmin2026! |
| PostgreSQL | thoth_app | Th0th_Pr0d_2026! |

---

## Resumen de Archivos Nuevos (este commit)

**Backend:**
- Módulo CRUD de Tipos de Dispositivo (`/api/v1/device-types`)
- Módulo CRUD de Categorías de Mantenimiento (`/api/v1/maintenance-categories`)
- Endpoint de recuperación de contraseña (`/api/v1/auth/request-password-reset`)
- Reporte de mantenimiento mejorado con secciones por sede y cumplimiento
- Migración V4 para las nuevas tablas `device_type` y `maintenance_category`

**Frontend:**
- Página de Tipos de Dispositivo con tabla CRUD
- Página de Categorías de Mantenimiento con tabla CRUD
- Flujo "¿Olvidaste tu contraseña?" en el login
- Dashboard de reportes mejorado con tabs de Sede y Cumplimiento + exportar CSV
- Fix de iconos Material y overflow en listas
