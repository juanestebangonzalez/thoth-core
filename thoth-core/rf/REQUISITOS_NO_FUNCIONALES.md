# THOTH C.O.R.E - Requisitos No Funcionales
## Sistema de Gestión de Equipos de Cómputo - MVP v1.0.0

---

## 📋 INTRODUCCIÓN

Los Requisitos No Funcionales (RNF) definen las propiedades y características del sistema que aseguran su calidad, confiabilidad, seguridad y rendimiento.

---

## 🚀 RENDIMIENTO (PERFORMANCE)

### RNF1 - Tiempo de Respuesta
**Descripción:** El sistema debe responder dentro de tiempos aceptables.

**Especificaciones:**
- Consultas simples (get by ID): < 100 ms (p95)
- Listados (hasta 100 registros): < 500 ms (p95)
- Reportes: < 2 segundos (p95)
- Operaciones de escritura: < 300 ms (p95)

**Métrica:** Percentil 95 (p95) de latencia

**Medición:** APM (Application Performance Monitoring)

---

### RNF2 - Throughput (Capacidad)
**Descripción:** El sistema debe soportar cierta cantidad de transacciones.

**Especificaciones:**
- Mínimo: 100 usuarios concurrentes
- Objetivo: 500 usuarios concurrentes
- Máximo: 1,000 usuarios concurrentes
- Picos: 2,000 requests por segundo

**Métrica:** Transacciones por segundo (TPS)

**Test:** Load testing con Apache JMeter

---

### RNF3 - Caché
**Descripción:** Implementar estrategia de caché.

**Niveles:**
- **L1:** In-memory cache (Spring Cache, 5 minutos)
- **L2:** Redis (cachés distribuido, 1 hora)
- **L3:** Database query optimization

**Datos a Cachear:**
- Catálogos de categorías
- Ubicaciones
- Datos de usuarios (30 minutos)
- Reportes (1 hora)

**Invalidación:**
- Por tiempo
- Por evento (cuando se actualiza el dato)

---

## 🔒 SEGURIDAD

### RNF4 - Autenticación
**Descripción:** Mecanismos seguros de autenticación.

**Especificaciones:**
- OAuth 2.0 con JWT
- HTTPS/TLS 1.2 mínimo (1.3 recomendado)
- Sesiones con timeout de 30 minutos
- Re-autenticación para operaciones críticas

**Tokens JWT:**
- Expiración: 30 minutos
- Refresh token: 7 días
- Algoritmo: RS256

---

### RNF5 - Autorización
**Descripción:** Control de acceso basado en roles (RBAC).

**Niveles:**
- Por módulo
- Por operación (Create, Read, Update, Delete)
- Por datos (filtrado por ubicación/responsable)

**Implementación:**
- Spring Security
- @PreAuthorize con SpEL
- AuditLog para cambios críticos

---

### RNF6 - Encriptación
**Descripción:** Protección de datos sensibles.

**En Tránsito:**
- HTTPS/TLS 1.2+ para todas las comunicaciones
- HSTS header (Strict-Transport-Security)
- Certificate pinning en apps móviles

**En Reposo:**
- Contraseñas: BCrypt con salt (minimum 10 rounds)
- MAC Addresses: No se encriptan (no sensible)
- Serial Numbers: No se encriptan (necesario para búsqueda)
- PII (Personal Identifiable Info): AES-256-GCM

**Gestión de Claves:**
- Vault (HashiCorp) o AWS Secrets Manager
- Rotación cada 90 días

---

### RNF7 - Auditoría y Logging
**Descripción:** Registro de operaciones para seguridad y compliance.

**Eventos Auditados:**
- Creación/modificación/eliminación de equipos
- Cambios de estado
- Acceso a datos sensibles
- Cambios de usuario/contraseña
- Accesos administrativos

**Formato:**
```json
{
  "timestamp": "ISO-8601",
  "userId": "string",
  "action": "CREATE_EQUIPMENT",
  "resourceId": "UUID",
  "changes": {
    "field": "oldValue → newValue"
  },
  "ipAddress": "string",
  "userAgent": "string",
  "status": "SUCCESS|FAILURE"
}
```

**Retención:**
- Logs de aplicación: 30 días
- Logs de auditoría: 1 año
- Backup: Almacenamiento inmutable

**Herramientas:**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk (alternativa empresarial)

---

### RNF8 - Inyección y Validación
**Descripción:** Protección contra inyecciones y datos inválidos.

**Medidas:**
- SQL Injection: JPA con prepared statements
- XSS: Validación de entrada, Content Security Policy
- CSRF: CSRF tokens en formularios
- Command Injection: No ejecutar comandos del usuario
- Path Traversal: Validar rutas de archivos

**Validación:**
- Cliente: HTML5 + JavaScript
- Servidor: Bean Validation + custom validators
- API: JSON Schema validation

---

## 📊 DISPONIBILIDAD Y CONFIABILIDAD

### RNF9 - Disponibilidad (Uptime)
**Descripción:** El sistema debe estar disponible la mayor parte del tiempo.

**Especificaciones:**
- Objetivo: 99.5% uptime (SLA)
- Máximo downtime permitido: 3.6 horas/mes
- Ventana de mantenimiento: Domingos 02:00-04:00 UTC

**Medición:**
- Monitoreo 24/7 con Uptime Robot o similar
- Alertas automáticas en caso de caída
- Dashboard público de status

---

### RNF10 - Recuperación ante Desastres (DR)
**Descripción:** Capacidad de recuperarse de fallos.

**Tiempo de Recuperación (RTO):** < 4 horas
**Punto de Recuperación (RPO):** < 15 minutos

**Estrategia:**
- **DB:** Replicación master-slave con failover automático
- **App Server:** Load balancer con múltiples instancias
- **Storage:** Backup incremental diario + snapshots cada 6 horas
- **Pruebas DR:** Mensualmente

**Ubicaciones:**
- Primary: Data Center principal
- Secondary: Data Center de standby (geo-redundancia)

---

### RNF11 - Tolerancia a Fallos
**Descripción:** El sistema debe manejar fallos gracefully.

**Medidas:**
- Circuit breakers para servicios externos
- Retry logic con exponential backoff
- Fallback a valores por defecto
- Graceful degradation

**Implementación:**
- Spring Cloud Circuit Breaker (Resilience4j)
- Timeout en todas las llamadas externas
- Dead letter queues para mensajes fallidos

---

## 🏗️ ESCALABILIDAD

### RNF12 - Escalabilidad Horizontal
**Descripción:** El sistema debe poder escalar añadiendo más servidores.

**Arquitectura Stateless:**
- Sin sesiones en memoria
- Sesiones en Redis distribuido
- Balanceo de carga (nginx/HAProxy)

**Base de Datos:**
- Read replicas para distribución
- Connection pooling (HikariCP: 20-50 conexiones)
- Índices en campos de búsqueda común

**Caching Distribuido:**
- Redis en cluster
- Invalidación coordenada
- Monitoreo de hit ratio > 80%

---

### RNF13 - Escalabilidad Vertical
**Descripción:** Capacidad de crecer dentro del mismo servidor.

**Límites Iniciales:**
- Mínimo: 4 CPU cores, 8 GB RAM
- Máximo: 16 CPU cores, 64 GB RAM
- Disk: SSD con mínimo 500 GB

---

## 🔧 MANTENIBILIDAD

### RNF14 - Código Limpio y Documentación
**Descripción:** El código debe ser mantenible y comprensible.

**Estándares:**
- Google Java Style Guide
- Documentación de clases públicas (Javadoc)
- README en cada módulo
- Ejemplos de uso

**Herramientas:**
- SonarQube para análisis de código
- Checkstyle para estilo
- SpotBugs para bugs potenciales
- Code coverage mínimo: 80%

---

### RNF15 - Versionamiento
**Descripción:** Control de versiones del código y data.

**Especificaciones:**
- Git como VCS
- Semantic Versioning (Major.Minor.Patch)
- Ramas: main, develop, feature/*, bugfix/*
- Tags para releases
- Database migrations versionadas (Flyway)

---

## 🧪 PRUEBAS Y CALIDAD

### RNF16 - Cobertura de Pruebas
**Descripción:** Mínimo de pruebas automatizadas.

**Especificaciones:**
- Cobertura de código: Mínimo 80%
- Pruebas unitarias: 100% de la lógica de negocio
- Pruebas de integración: APIs principales
- Pruebas de aceptación: Flujos críticos

**Herramientas:**
- JUnit 5 para unitarias
- Mockito para mocks
- TestContainers para integración
- Selenium/Cypress para UI (v2.0)

---

### RNF17 - Calidad de Código
**Descripción:** Métricas de calidad del código.

**Métricas:**
- Ciclomaticidad: < 10
- Deuda técnica: < 5% del tiempo
- Duplicación: < 3%
- Issues críticos: 0

**Gate de Calidad:**
- Build falla si no cumple mínimos
- Pull requests requieren review
- SonarQube como enforcer

---

## 🌍 COMPATIBILIDAD

### RNF18 - Compatibilidad de Plataformas
**Descripción:** El sistema debe funcionar en diferentes plataformas.

**Servidores:**
- Windows Server 2016+
- Linux (CentOS, Ubuntu, Debian)
- Docker containers
- Kubernetes

**Navegadores (v2.0):**
- Chrome/Chromium 90+
- Firefox 88+
- Safari 14+
- Edge 90+

**Bases de Datos:**
- PostgreSQL 12+
- MySQL 8.0+ (futuro)
- Oracle Database (futuro)

---

## 📦 INSTALABILIDAD

### RNF19 - Instalación
**Descripción:** Facilidad de instalación y deployment.

**Métodos:**
- Docker: Multi-stage build, < 500 MB imagen
- WAR: Deployable en app servers
- JAR: Standalone executable
- Kubernetes: Helm charts

**Documentación:**
- Guía de instalación paso a paso
- Configuración automática de BD
- Scripts de inicialización
- Quick start (5 minutos)

---

## 🌐 INTEROPERABILIDAD

### RNF20 - Integración con Sistemas Externos
**Descripción:** Capacidad de conectarse con otros sistemas.

**Interfaces:**
- REST API (v1, v2, ...)
- GraphQL (futuro)
- Webhooks para eventos
- SOAP (para legacy)

**Formatos:**
- JSON (primario)
- XML (soporte)
- CSV (importación)

**Autenticación Externa:**
- OAuth 2.0
- SAML 2.0
- API Keys

---

## 📱 USABILIDAD

### RNF21 - Interfaz de Usuario
**Descripción:** UX y UI de calidad.

**Principios:**
- Mobile-first responsive design
- Accesibilidad WCAG 2.1 AA
- Consistencia de estilos
- Tiempos de carga < 2s

**Herramientas:**
- Material Design 3
- Bootstrap/Tailwind CSS
- Figma para prototipos

---

### RNF22 - Experiencia de Usuario
**Descripción:** Facilidad de uso del sistema.

**Métricas:**
- System Usability Scale (SUS) > 70
- Tareas completadas en < 3 clics
- Errores evitables: 0
- Help/tutorials para 80% de features

---

## 📊 INFORMES Y MONITOREO

### RNF23 - Monitoreo del Sistema
**Descripción:** Visibilidad del estado del sistema.

**Métricas Monitoreadas:**
- CPU, Memoria, Disco
- Conexiones DB
- Request latency
- Error rates
- Cache hit ratio

**Herramientas:**
- Prometheus para métricas
- Grafana para dashboards
- DataDog o New Relic (alternativa)
- Alertas automáticas (PagerDuty)

---

### RNF24 - Reporting
**Descripción:** Generación de reportes.

**Tipos:**
- Operacionales (diario)
- Gerenciales (semanal)
- Estratégicos (mensual)
- Compliance (trimestral)

**Formatos:**
- PDF
- Excel
- CSV
- HTML

---

## 📋 CUMPLIMIENTO NORMATIVO

### RNF25 - Regulaciones y Compliance
**Descripción:** Cumplimiento de normas y regulaciones.

**Estándares:**
- GDPR: Protección de datos (si aplica)
- SOC 2: Controles de seguridad
- ISO 27001: Seguridad de información
- HIPAA: Datos médicos (si aplica)

**Implementación:**
- Políticas de privacidad públicas
- Consentimiento del usuario
- Derecho al olvido
- Data retention policies
- Auditorías anuales

---

## 📝 RESUMEN RNF

| ID | Categoría | RNF | Especificación | Prioridad |
|-----|-----------|-----|----------------|-----------|
| 1 | Performance | Latencia | < 500ms p95 | CRÍTICA |
| 2 | Performance | Throughput | 500 users concurrentes | CRÍTICA |
| 3 | Performance | Caché | L1, L2, L3 | ALTA |
| 4 | Seguridad | Autenticación | OAuth 2.0 + JWT | CRÍTICA |
| 5 | Seguridad | Autorización | RBAC | CRÍTICA |
| 6 | Seguridad | Encriptación | TLS 1.2+, BCrypt | CRÍTICA |
| 7 | Seguridad | Auditoría | Full logging | ALTA |
| 9 | Disponibilidad | Uptime | 99.5% SLA | CRÍTICA |
| 10 | Disponibilidad | DR | RTO < 4h | ALTA |
| 12 | Escalabilidad | Horizontal | Stateless | ALTA |
| 14 | Mantenibilidad | Código Limpio | Google Guide | MEDIA |
| 16 | Calidad | Test Coverage | 80% mínimo | ALTA |
| 18 | Compatibilidad | Plataformas | Windows, Linux, Docker | ALTA |
| 20 | Interoperabilidad | APIs | REST, GraphQL (v2) | MEDIA |
| 21 | Usabilidad | UI/UX | WCAG 2.1 AA | ALTA |
| 23 | Monitoreo | Métricas | Prometheus + Grafana | MEDIA |

---

**Documento Versión:** 1.0  
**Última Actualización:** Septiembre 2026  
**Autor:** Equipo de Desarrollo THOTH
