# THOTH C.O.R.E. — Guía de Despliegue

## Arquitectura Recomendada (Gratuita)

| Componente | Plataforma | Tier |
|-----------|-----------|------|
| **Frontend** (Angular 22 SPA) | Netlify | Free (300 créditos/mes) |
| **Backend** (Spring Boot 4.1) | Oracle Cloud | Always Free (4 ARM cores, 24 GB RAM) |
| **Base de datos** (PostgreSQL) | Oracle Cloud VM | Always Free (en la misma VM) |
| **Alternativa Backend** | Render | Free (512 MB, DB expira cada 90 días) |

---

## 1. FRONTEND — Netlify

### Prerrequisitos
- Cuenta Netlify (gratis, sin tarjeta)
- Repositorio en GitHub/GitLab

### Pasos

1. **Configurar environment.prod.ts** con la URL real del backend:
   ```typescript
   // src/environments/environment.prod.ts
   export const environment = {
     production: true,
     apiUrl: 'https://TU-BACKEND-URL/api/v1'
   };
   ```

2. **El archivo `netlify.toml` ya está creado** en la raíz del frontend con:
   - Build command configurado
   - SPA redirect (/* → /index.html)
   - Headers de seguridad
   - Cache de assets estáticos

3. **Conectar repositorio en Netlify:**
   - Ir a [app.netlify.com](https://app.netlify.com)
   - "Add new site" → "Import an existing project"
   - Seleccionar repositorio de GitHub
   - Base directory: `thothCoreFrontend/thothCoreFrontend`
   - Build command: `npm ci && npx ng build --configuration production`
   - Publish directory: `dist/thothCoreFrontend/browser`
   - Deploy!

4. **Dominio personalizado** (opcional):
   - Site settings → Domain management → Add custom domain

### Límites Free Tier Netlify
- ~15 GB bandwidth/mes
- ~300 builds/mes
- 1 build concurrente
- Sin SSR (funciones serverless consumen créditos)

---

## 2. BACKEND — Oracle Cloud (Recomendado)

### Por qué Oracle Cloud
- **4 CPU ARM + 24 GB RAM** — 48x más RAM que Render gratuito
- **200 GB almacenamiento** — suficiente para PostgreSQL + app
- **10 TB bandwidth/mes** — más que suficiente
- **Verdaderamente permanente** — sin expiración, sin spin-down
- Requiere tarjeta de crédito al registrarse (no cobra)

### Pasos

#### 2.1. Crear VM

1. Registrarse en [cloud.oracle.com](https://cloud.oracle.com) (Always Free)
2. Compute → Instances → Create Instance
3. Configurar:
   - **Shape**: VM.Standard.A1.Flex (ARM)
   - **OCPU**: 2-4 (máx 4 gratis)
   - **RAM**: 12-24 GB (máx 24 gratis)
   - **Image**: Ubuntu 22.04+ o Oracle Linux 9
   - **Boot volume**: 50 GB
   - Descargar SSH key

> ⚠️ Si dice "Out of host capacity": probar otra región (São Paulo, Osaka) o reintentar más tarde.

#### 2.2. Configurar la VM

```bash
# Conectar por SSH
ssh -i tu-key.pem ubuntu@TU-IP-PUBLICA

# Instalar Java 21
sudo apt update && sudo apt install -y openjdk-21-jre-headless

# Instalar PostgreSQL
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable postgresql

# Configurar DB
sudo -u postgres psql -c "CREATE USER thothcore WITH PASSWORD 'TU-PASSWORD-SEGURO';"
sudo -u postgres psql -c "CREATE DATABASE thothcore OWNER thothcore;"
sudo -u postgres psql -c "GRANT ALL ON DATABASE thothcore TO thothcore;"

# Instalar Nginx (reverse proxy + HTTPS)
sudo apt install -y nginx certbot python3-certbot-nginx
```

#### 2.3. Abrir puertos (AMBOS pasos necesarios)

**En Oracle Cloud Console:**
- Networking → Virtual Cloud Networks → tu VCN → Security Lists
- Add Ingress Rules:
  - Puerto 80 (HTTP): Source 0.0.0.0/0
  - Puerto 443 (HTTPS): Source 0.0.0.0/0

**En la VM (firewall del SO):**
```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save
```

#### 2.4. Desplegar Spring Boot

```bash
# En tu máquina local: compilar y copiar JAR
cd thoth-core
./gradlew bootJar
scp -i tu-key.pem build/libs/thoth-core-*.jar ubuntu@TU-IP:/opt/thoth-core/app.jar

# En la VM: crear servicio systemd
sudo tee /etc/systemd/system/thoth-core.service << 'SVC'
[Unit]
Description=THOTH CORE Backend
After=postgresql.service

[Service]
Type=simple
User=thothcore
ExecStart=/usr/bin/java -Xmx1g -jar /opt/thoth-core/app.jar --spring.profiles.active=prod
WorkingDirectory=/opt/thoth-core
Restart=always
RestartSec=10
Environment=DB_PASSWORD=TU-PASSWORD-SEGURO
Environment=JWT_SECRET=TU-JWT-SECRET-64-CHARS-MINIMO
Environment=CORS_ALLOWED_ORIGINS=https://tu-frontend.netlify.app

[Install]
WantedBy=multi-user.target
SVC

sudo useradd -r -s /bin/false thothcore
sudo mkdir -p /opt/thoth-core
sudo chown thothcore:thothcore /opt/thoth-core
sudo systemctl daemon-reload
sudo systemctl enable --now thoth-core
```

#### 2.5. Configurar Nginx + HTTPS

```bash
sudo tee /etc/nginx/sites-available/thoth-core << 'NGINX'
server {
    listen 80;
    server_name tu-dominio.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name tu-dominio.com;

    client_max_body_size 50M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
NGINX

sudo ln -s /etc/nginx/sites-available/thoth-core /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# SSL con Let's Encrypt
sudo certbot --nginx -d tu-dominio.com
```

#### 2.6. Mantenimiento

```bash
# Backups automáticos de PostgreSQL (cron cada 6 horas)
(crontab -l; echo "0 */6 * * * pg_dump -U thothcore thothcore | gzip > /opt/backups/thothcore_\$(date +\%Y\%m\%d_\%H\%M).sql.gz") | crontab -

# Actualizar app
scp -i tu-key.pem build/libs/thoth-core-*.jar ubuntu@TU-IP:/opt/thoth-core/app.jar
ssh -i tu-key.pem ubuntu@TU-IP 'sudo systemctl restart thoth-core'

# Evitar reclamación por inactividad (keep-alive cron)
(crontab -l; echo "*/30 * * * * curl -s http://localhost:8080/actuator/health > /dev/null") | crontab -
```

---

## 3. BACKEND ALTERNATIVO — Render

> Usar solo si Oracle Cloud no tiene capacidad disponible.

### Pasos

1. **El archivo `render.yaml` ya está creado** en la raíz del proyecto.

2. **Conectar en Render:**
   - Ir a [dashboard.render.com](https://dashboard.render.com)
   - New → Blueprint → seleccionar repositorio
   - Render detecta `render.yaml` y crea web service + DB automáticamente

3. **Variables de entorno** se configuran automáticamente desde render.yaml

### ⚠️ Limitaciones IMPORTANTES de Render Free

| Limitación | Impacto |
|-----------|---------|
| **512 MB RAM total** | JVM consume ~400 MB, deja poco para la app |
| **Spin-down a los 15 min** | Primera petición después de inactividad tarda 30-60s |
| **DB expira cada 90 días** | Se borran TODOS los datos. Hacer backup antes |
| **Sin disco persistente** | Uploads de documentos se pierden en cada deploy |

### Mitigaciones
- `JAVA_TOOL_OPTIONS=-Xmx400m -XX:+UseSerialGC` (ya en render.yaml)
- `spring.main.lazy-initialization=true` (ya en render.yaml)
- Backup de DB antes del día 90: `pg_dump` → guardar en otro sitio
- Para uploads: usar un servicio externo (Cloudinary, S3, etc.)

---

## 4. CHECKLIST PRE-DESPLIEGUE

### Backend
- [ ] Cambiar `jwt.secret` a un secreto seguro de 64+ caracteres
- [ ] Cambiar contraseña de base de datos
- [ ] Verificar `application-prod.yml`: `ddl-auto: validate`, `include-message: never`
- [ ] Configurar CORS con la URL real del frontend
- [ ] Ejecutar migraciones Flyway o hacer `ddl-auto: update` solo la primera vez
- [ ] Verificar que Swagger está deshabilitado en producción

### Frontend
- [ ] Actualizar `environment.prod.ts` con la URL real del backend
- [ ] Ejecutar `ng build --configuration production` localmente para verificar
- [ ] Verificar que el service worker no cachea datos sensibles

### Seguridad
- [ ] JWT secret: mínimo 64 caracteres, generado aleatoriamente
- [ ] HTTPS habilitado en backend (Nginx + Let's Encrypt / Render lo da gratis)
- [ ] CORS: solo permitir el dominio del frontend
- [ ] Rate limiting activo
- [ ] Headers de seguridad en netlify.toml
