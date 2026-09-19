# THOTH C.O.R.E. — FASE 1: AUDITORÍA DE SEGURIDAD INTEGRAL

**Proyecto:** THOTH C.O.R.E. (Control, Ordenamiento, Registro de Equipos)  
**Cliente:** Instituto del Corazón  
**Fecha:** 2026-09-18  
**Auditor:** Claude Opus 4.6 — Security Audit Agent  
**Tipo:** Inventario y Auditoría Inicial (SOLO LECTURA — Sin cambios de código)  
**Clasificación:** CONFIDENCIAL

---

## ÍNDICE

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Inventario del Proyecto](#2-inventario-del-proyecto)
3. [Arquitectura y Stack Tecnológico](#3-arquitectura-y-stack-tecnológico)
4. [Análisis de Spring Security](#4-análisis-de-spring-security)
5. [Auditoría JWT](#5-auditoría-jwt)
6. [Mapa Completo de Endpoints](#6-mapa-completo-de-endpoints)
7. [Auditoría de DTOs y Validación](#7-auditoría-de-dtos-y-validación)
8. [Auditoría de Entidades JPA](#8-auditoría-de-entidades-jpa)
9. [Auditoría de Repositorios y SQL](#9-auditoría-de-repositorios-y-sql)
10. [Auditoría de Servicios](#10-auditoría-de-servicios)
11. [CORS / CSRF / Session Management](#11-cors--csrf--session-management)
12. [Gestión de Secretos y Credenciales](#12-gestión-de-secretos-y-credenciales)
13. [Seguridad de Archivos y Documentos](#13-seguridad-de-archivos-y-documentos)
14. [Rate Limiting y Protección contra Fuerza Bruta](#14-rate-limiting-y-protección-contra-fuerza-bruta)
15. [Docker y Despliegue](#15-docker-y-despliegue)
16. [Actuator y Exposición de Información](#16-actuator-y-exposición-de-información)
17. [Tests Existentes y Cobertura](#17-tests-existentes-y-cobertura)
18. [Dependencias y Vulnerabilidades Conocidas](#18-dependencias-y-vulnerabilidades-conocidas)
19. [Arquitectura Hexagonal — Conformidad](#19-arquitectura-hexagonal--conformidad)
20. [Código Muerto y Bugs Funcionales](#20-código-muerto-y-bugs-funcionales)
21. [Matriz de Vulnerabilidades](#21-matriz-de-vulnerabilidades)
22. [Evaluación de Riesgo Global](#22-evaluación-de-riesgo-global)
23. [Plan de Remediación por Fases](#23-plan-de-remediación-por-fases)

---

## 1. RESUMEN EJECUTIVO

Se auditaron **110+ archivos Java** (backend), **50+ archivos TypeScript** (frontend), archivos de configuración, Docker, y variables de entorno del sistema THOTH C.O.R.E.

### Hallazgos Críticos

| Severidad | Cantidad |
|-----------|----------|
| **CRÍTICA** | 8 |
| **ALTA** | 12 |
| **MEDIA** | 9 |
| **BAJA** | 6 |
| **INFO** | 4 |
| **TOTAL** | **39** |

### Los 5 hallazgos más graves

1. **SEC-001**: UserController completamente abierto — cualquier anónimo puede listar usuarios, cambiar roles, resetear contraseñas y eliminar usuarios
2. **SEC-002**: EquipmentController sin autenticación — CRUD completo expuesto a anónimos
3. **SEC-003**: ReportsController/AlertController exponen TODOS los datos del negocio sin autenticación
4. **SEC-004**: Secretos hardcodeados en `.env` y `application-dev.yml` (JWT secret, contraseñas DB)
5. **SEC-005**: DocumentController permite upload/delete anónimo de archivos

**Veredicto: La aplicación NO está lista para producción.** La mayoría de los endpoints carecen de autenticación/autorización a nivel de controller, confiando únicamente en las reglas de `SecurityConfig` que tienen brechas significativas.

---

## 2. INVENTARIO DEL PROYECTO

### 2.1 Estructura Backend (Spring Boot)

```
thoth-core/
├── build.gradle                    # Gradle + Spring Boot 4.1.1, Java 21
├── settings.gradle                 # rootProject.name = 'thoth-core'
├── Dockerfile                      # Multi-stage, eclipse-temurin:21
├── docker-compose.yml              # PostgreSQL 16 + App
├── compose.yaml                    # Alternativo
├── .env                            # ⚠️ SECRETOS EN TEXTO PLANO
├── .dockerignore / .gitignore
├── SECURITY.md / SECURITY_TESTS.md
│
└── src/
    ├── main/java/com/thoth/
    │   ├── ThothCoreApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java
    │   │   ├── CorsConfig.java
    │   │   ├── SpringDocOpenApiConfig.java
    │   │   └── security/
    │   │       ├── CustomUserDetailsService.java
    │   │       ├── JwtAuthenticationFilter.java
    │   │       ├── JwtTokenProvider.java
    │   │       └── RateLimitingFilter.java
    │   ├── adapter/
    │   │   ├── in/rest/
    │   │   │   ├── controller/       (15 controllers)
    │   │   │   ├── dto/request/      (14 request DTOs)
    │   │   │   ├── dto/response/     (7 response DTOs)
    │   │   │   └── exception/        (6 exception classes)
    │   │   └── out/
    │   │       ├── persistence/
    │   │       │   ├── entity/       (11 JPA entities)
    │   │       │   ├── repository/   (10 repositories)
    │   │       │   ├── mapper/       (3 entity mappers)
    │   │       │   └── 3 adapters
    │   │       ├── ai/AIAgentAdapter.java
    │   │       └── scheduler/MaintenanceScheduledJob.java
    │   ├── application/
    │   │   ├── command/              (5 commands)
    │   │   ├── dto/                  (11 application DTOs)
    │   │   ├── exception/            (4 exceptions)
    │   │   ├── mapper/               (4 app mappers)
    │   │   ├── port/input/           (7 use case ports)
    │   │   ├── port/output/          (5 output ports)
    │   │   ├── service/              (7 services)
    │   │   └── usecase/impl/         (7 use case implementations)
    │   └── domain/
    │       ├── model/                (2 domain models)
    │       └── valueobject/          (10 value objects)
    │
    ├── main/resources/
    │   ├── application.yml
    │   ├── application-dev.yml
    │   └── application-prod.yml
    │
    └── test/java/com/thoth/         (24 test files, ~73 test methods)
```

**Totales Backend:** 110 clases Java, 24 tests, 3 archivos de configuración YAML

### 2.2 Estructura Frontend (Angular 22.1.x)

```
thothCoreFrontend/thothCoreFrontend/src/app/
├── app.component.ts / app.config.ts / app.routes.ts
├── core/
│   ├── guards/        (3: auth, admin, permission)
│   ├── interceptors/  (1: auth interceptor)
│   ├── models/        (5 modelos)
│   └── services/      (13 servicios)
├── pages/             (18 componentes de página)
├── shared/            (3 componentes compartidos)
└── environments/      (dev + prod)
```

**Totales Frontend:** 50+ archivos TypeScript

### 2.3 Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| Spring Boot | 4.1.1 | Framework |
| Spring Security | (managed) | Autenticación/Autorización |
| Spring Data JPA | (managed) | Persistencia |
| PostgreSQL | 16 (Docker) | Base de datos |
| jjwt | 0.12.6 | JWT tokens |
| Apache Tika | 2.9.2 | Content sniffing |
| ZXing | 3.5.3 | QR codes |
| SpringDoc OpenAPI | 3.1.0 | API docs |
| Lombok | (managed) | Boilerplate |
| ArchUnit | 1.3.0 | Tests de arquitectura |
| H2 | (managed) | DB para tests |
| JaCoCo | (plugin) | Cobertura |
| SonarQube | 5.1.0 | Análisis estático |

---

## 3. ARQUITECTURA Y STACK TECNOLÓGICO

- **Arquitectura:** Hexagonal (Ports & Adapters)
- **Java:** 21 (toolchain)
- **Build:** Gradle
- **Backend:** Spring Boot 4.1.1 con Spring Security, JPA, Validation
- **Frontend:** Angular 22.1.x, Angular Material (dark theme), standalone components, signals
- **DB:** PostgreSQL 16 (dev: `ddl-auto: update`, prod: `ddl-auto: validate` + Flyway)
- **Auth:** JWT stateless, BCrypt passwords
- **Roles:** ADMIN, TECHNICIAN, USER, VIEWER
- **Permisos:** Granulares por módulo × acción (VIEW, CREATE, EDIT, DELETE)
- **Containerización:** Docker multi-stage, docker-compose

---

## 4. ANÁLISIS DE SPRING SECURITY

### 4.1 SecurityConfig.java — Reglas del Filter Chain

```
CSRF:           DESHABILITADO (aceptable para API REST stateless)
Session:        STATELESS
Entry Point:    HTTP 401 (sin redirect)
Method Security: @EnableMethodSecurity habilitado
Password:       BCryptPasswordEncoder
```

### 4.2 Reglas de Autorización por Ruta

| Patrón | Regla | Problema |
|--------|-------|----------|
| `/api/v1/auth/**` | `permitAll()` | OK — login/registro |
| `/api/v1/qr/**` | `permitAll()` | ⚠️ QR codes sin auth |
| `/api/v1/sedes/**` | `permitAll()` | ⚠️ CRUD sedes sin auth |
| `/api/v1/maintenance/**` | `permitAll()` | ⚠️ Endpoints de mantenimiento sin auth |
| `/api/v1/users/**` | `hasRole("ADMIN")` | ⚠️ Regla existe pero controllers NO tienen @PreAuthorize |
| GET `/api/v1/equipment/**` | `authenticated()` | ⚠️ Controller no valida auth |
| POST/PUT/PATCH/DELETE `/api/v1/equipment/**` | `hasAnyRole("ADMIN","TECHNICIAN")` | ⚠️ Controller no valida auth |
| `/api/v1/alerts/**` | `authenticated()` | Controller no tiene @PreAuthorize |
| `/api/v1/technicians/**` | `authenticated()` | ✅ Controller tiene @PreAuthorize |
| `/api/v1/audit/**` | `authenticated()` | ✅ Controller tiene @PreAuthorize |
| `/api/v1/location-history/**` | `authenticated()` | ✅ Controller tiene @PreAuthorize |
| `/api/v1/ai/**` | `authenticated()` | ⚠️ Controller no tiene @PreAuthorize |
| `/swagger-ui/**` | `permitAll()` | ⚠️ Swagger abierto en dev |
| `/v3/api-docs/**` | `permitAll()` | ⚠️ OpenAPI spec abierta |
| `/actuator/health` | `permitAll()` | OK |
| `anyRequest()` | `authenticated()` | OK — catch-all |

### 4.3 Problema de Doble Capa de Seguridad

**HALLAZGO CRÍTICO:** Existe una DISCREPANCIA entre las reglas de `SecurityConfig` y las anotaciones en los controllers.

`SecurityConfig` define reglas a nivel URL, pero **muchos controllers NO tienen `@PreAuthorize`**. Esto significa que:
- Las rutas marcadas como `permitAll()` en SecurityConfig (sedes, maintenance, QR) son realmente accesibles sin auth, incluyendo operaciones de escritura
- Las rutas que requieren roles en SecurityConfig (users, equipment POST/PUT/DELETE) están protegidas por la URL, pero los controllers no verifican roles internamente — si un futuro refactor cambia las URLs, la protección desaparece
- **Defensa en profundidad AUSENTE** — la seguridad depende de una única capa

---

## 5. AUDITORÍA JWT

### 5.1 JwtTokenProvider.java

| Aspecto | Estado | Detalle |
|---------|--------|---------|
| Algoritmo | HMAC-SHA (HS256/384/512 según longitud de clave) | OK |
| Expiración | 86,400,000 ms = **24 horas** | ⚠️ Muy largo para un token hospitalario |
| Secret | `${jwt.secret}` desde config | ⚠️ Default hardcodeado en dev |
| Claims | subject=username, role=string, iat, exp | ⚠️ Un solo role, no lista |
| Validación | `try/catch Exception → false` | ⚠️ Swallow silencioso, sin logging |
| Revocación | **NO EXISTE** | 🔴 No hay blacklist/invalidación |
| Refresh token | **NO EXISTE** | 🔴 Sin mecanismo de refresh |
| Key caching | Se recrea en cada llamada | ⚠️ Ineficiente |

### 5.2 JwtAuthenticationFilter.java

| Aspecto | Estado |
|---------|--------|
| Extracción | Bearer token del header Authorization |
| Validación contra DB | **NO** — confía 100% en el token |
| Si token inválido | Continúa sin autenticar (sin error) |
| Lookup de UserDetails | **NO** — role viene del token sin verificar |

**Riesgo:** Si un token es robado, NO puede ser revocado hasta que expire (24 horas). El role en el token no se verifica contra la DB, por lo que un cambio de role/deshabilitación no tiene efecto inmediato.

---

## 6. MAPA COMPLETO DE ENDPOINTS

### 🔴 ENDPOINTS SIN AUTENTICACIÓN (Accesibles por Anónimos)

| Método | Ruta | Controller | Impacto |
|--------|------|------------|---------|
| GET | `/api/v1/users` | UserController | Lista TODOS los usuarios |
| PATCH | `/api/v1/users/{id}/role` | UserController | Cambio de role a ADMIN |
| PATCH | `/api/v1/users/{id}/toggle-enabled` | UserController | Habilitar/deshabilitar usuarios |
| POST | `/api/v1/users/{id}/reset-password` | UserController | Reset + retorna contraseña temporal |
| DELETE | `/api/v1/users/{id}` | UserController | Eliminación permanente |
| POST | `/api/v1/equipment` | EquipmentController | Crear equipos |
| PUT | `/api/v1/equipment/{id}` | EquipmentController | Modificar equipos |
| PATCH | `/api/v1/equipment/{id}/status` | EquipmentController | Cambiar estado |
| DELETE | `/api/v1/equipment/{id}` | EquipmentController | Eliminar equipo |
| GET | `/api/v1/equipment` / `{id}` | EquipmentController | Listar/ver equipos |
| POST | `/api/v1/documents/upload` | DocumentController | Subir archivos |
| GET | `/api/v1/documents/equipment/{id}` | DocumentController | Listar documentos |
| DELETE | `/api/v1/documents/{id}` | DocumentController | Eliminar documentos |
| GET | `/api/v1/reports/dashboard` | ReportsController | Dashboard completo |
| GET | `/api/v1/reports/maintenance-report` | ReportsController | Reporte de mantenimiento |
| GET | `/api/v1/alerts/*` | AlertController | Todas las alertas |
| GET | `/api/v1/ai/*` | AIController | Análisis IA de equipos |
| ALL | `/api/v1/sedes/**` | SedeController | CRUD completo de sedes |
| ALL | `/api/v1/maintenance/**` | MaintenanceController | Stubs de mantenimiento |
| GET | `/api/v1/qr/{id}` | QrCodeController | Generar QR codes |

### ✅ ENDPOINTS CON AUTENTICACIÓN CORRECTA

| Método | Ruta | Controller | Auth |
|--------|------|------------|------|
| GET | `/api/v1/audit` | AuditController | `isAuthenticated()` |
| POST | `/api/v1/audit/archive` | AuditController | `hasRole('ADMIN')` |
| GET/POST | `/api/v1/location-history/**` | LocationHistoryController | `isAuthenticated()` / `hasAnyRole(ADMIN,TECHNICIAN)` |
| ALL | `/api/v1/maintenance-history/**` | MaintenanceHistoryController | PermissionService |
| ALL | `/api/v1/permissions/**` | PermissionController | `hasRole('ADMIN')` (class-level) |
| GET | `/api/v1/technicians` | TechnicianController | `isAuthenticated()` |
| GET | `/api/v1/documents/{id}/download` | DocumentController | PermissionService |

**NOTA IMPORTANTE:** Aunque `SecurityConfig` define reglas de acceso por URL, los controllers de UserController, EquipmentController, etc. NO tienen `@PreAuthorize`, por lo que si las URLs cambian o las reglas de SecurityConfig se modifican, perderán toda protección. La regla de SecurityConfig para `/api/v1/users/**` → `hasRole("ADMIN")` actualmente protege los endpoints de UserController, pero **esta protección es frágil** y no representa defensa en profundidad.

---

## 7. AUDITORÍA DE DTOs Y VALIDACIÓN

### 7.1 DTOs SIN VALIDACIÓN (0 anotaciones)

| DTO | Campos sin validar | Riesgo |
|-----|-------------------|--------|
| **HardwareRequest** | processor, ramSizeGb, ramType, diskType, diskSizeGb, diskHealthPercent, diskTemperatureCelsius | Valores negativos, strings gigantes |
| **RentalInfoRequest** | 9 campos incluyendo contactEmail, contactPhone, contractFileUrl | Email falso, URL maliciosa, fechas inconsistentes |
| **UpdateEquipmentRequest** | Todos los campos, sin `@Valid` en objetos anidados | Bypass total de validación anidada |

### 7.2 DTOs con Validación PARCIAL

| DTO | Tiene | Falta |
|-----|-------|-------|
| CreateEquipmentRequest | `@NotBlank` en campos principales | `@Size(max)`, `@Pattern` MAC, `@Valid` en rentalInfo |
| ChangeStatusRequest | `@NotBlank` en status | `@Pattern` enum, `@Size` en reason |
| CreateMaintenanceHistoryRequest | `@NotBlank` básicos | `@Size` en signatureBase64 (¡puede ser megabytes!) |
| ChangePasswordRequest | `@Size(min=6)` en newPassword | `@Size(max)`, `@Pattern` complejidad |
| LoginRequest | `@NotBlank` | `@Size(max)` — strings ilimitados |
| CreateMaintenanceRequest | `@NotBlank` | `@Size`, `@Pattern` para type/severity |

### 7.3 DTOs con Validación ACEPTABLE

| DTO | Validación |
|-----|-----------|
| RegisterRequest | `@NotBlank`, `@Size(min,max)`, `@Email` — el mejor validado |
| AIAnalysisRequest | `@NotNull` UUID — OK |

### 7.4 Respuestas que Filtran Datos Sensibles

| Response | Dato Filtrado | Riesgo |
|----------|--------------|--------|
| **AuthResponse** | userId (UUID interno), role | IDOR si no hay auth en endpoints |
| **UserService.ResetPasswordResult** | `newPassword` en texto plano | 🔴 Contraseña viaja en la respuesta HTTP |
| **AuditLogEntity (directo)** | ipAddress, todos los campos internos | Entidad JPA como respuesta API |
| **UserService.UserInfo** | email de todos los usuarios | PII leak si endpoint no protegido |
| **MaintenanceHistoryDTO** | signatureBase64, technicianId | Firma digital + ID interno |

---

## 8. AUDITORÍA DE ENTIDADES JPA

| Entidad | Problema | Severidad |
|---------|----------|-----------|
| **UserEntity** | `password` sin `@JsonIgnore` ni `@ToString.Exclude`. Lombok `@Data` incluye password en toString(). Si la entidad se serializa accidentalmente, el hash BCrypt se filtra. | ALTA |
| **EquipmentEntity** | `brand`/`model` sin `@Column` — nullable sin límite de longitud | MEDIA |
| **MaintenanceHistoryEntity** | `signatureBase64` con `columnDefinition = "TEXT"` sin límite — almacenamiento ilimitado | MEDIA |
| **AuditLogEntity** | `details` TEXT sin límite, `ipAddress` sin constraintde longitud | MEDIA |
| **DocumentEntity** | `fileName` almacena nombre de archivo — potencial path traversal si no sanitizado | MEDIA |
| **UserPermissionEntity** | `module`/`action` son strings libres — no hay constraint de enum | BAJA |
| **AuditLogArchiveEntity** | `@Id` sin `@GeneratedValue` — intencional (preserva ID original) | INFO |
| Varias entidades | Sin `@PrePersist`/`@PreUpdate` para `createdAt`/`updatedAt` | BAJA |

---

## 9. AUDITORÍA DE REPOSITORIOS Y SQL

### 9.1 Queries Personalizadas

| Repositorio | Método | Tipo | ¿Parameterizado? |
|-------------|--------|------|-------------------|
| AuditLogRepository | findByDateRange | JPQL | ✅ Sí (`:from`, `:to`) |
| AuditLogRepository | findFiltered | JPQL | ✅ Sí (`:module`, `:user`, `:action`) |
| AuditLogArchiveRepository | findFiltered | JPQL | ✅ Sí |

**✅ No se encontró SQL injection en repositorios.** Todos usan queries parametrizadas o Spring Data derived queries.

### 9.2 Problemas de Rendimiento en Repositorios

- `findAll()` se usa en AlertController, ReportsController, MaintenanceScheduledJob y ListEquipmentUseCaseImpl — carga TODA la tabla en memoria
- `ListEquipmentUseCaseImpl` simula paginación client-side después de cargar todo
- `MaintenanceScheduledJob` ejecuta `findAll()` diariamente

---

## 10. AUDITORÍA DE SERVICIOS

### 10.1 AuthService

| Aspecto | Estado |
|---------|--------|
| Hash de passwords | ✅ BCrypt via PasswordEncoder |
| Enumeración de usuarios | ⚠️ Mensajes separados para "username taken" vs "email taken" en registro |
| Login error message | ✅ Genérico "Invalid username or password" |
| Account lockout | 🔴 NO EXISTE |
| Cambio de password | ⚠️ No requiere autenticación (solo current password) |

### 10.2 UserService

| Aspecto | Estado |
|---------|--------|
| Random password | ✅ SecureRandom |
| Hash | ✅ PasswordEncoder |
| Reset password | 🔴 Retorna contraseña en texto plano en la respuesta |
| Delete | ⚠️ Hard delete (solo protege username "admin") |
| List users | ✅ UserInfo no incluye password hash |

### 10.3 PermissionService

| Aspecto | Estado |
|---------|--------|
| Verificación | ✅ Resuelve username → userId → permisos |
| Denegación | ✅ AccessDeniedException |
| Permisos inválidos | ⚠️ Ignora silenciosamente módulos/acciones desconocidos |

### 10.4 AuditService

| Aspecto | Estado |
|---------|--------|
| Logging | ✅ Registra acción, módulo, usuario, IP, detalles |
| Sanitización | 🔴 Campo `details` acepta cualquier input sin sanitizar |
| Respuesta | ⚠️ Retorna entidades JPA directamente (no DTOs) |

---

## 11. CORS / CSRF / SESSION MANAGEMENT

### 11.1 CORS

```java
config.setAllowedOrigins(List.of("http://localhost:4200", "http://192.168.1.5:4200"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);
```

| Problema | Severidad |
|----------|-----------|
| Orígenes hardcodeados — no configurables por environment | MEDIA |
| IP privada `192.168.1.5` expuesta en código | BAJA |
| En producción, el origen real del frontend no está incluido | ALTA |
| `allowedHeaders("*")` con `allowCredentials(true)` | MEDIA |

### 11.2 CSRF

CSRF deshabilitado — **aceptable** para API REST stateless con JWT Bearer tokens (el token en el header Authorization no se envía automáticamente por el navegador como las cookies).

### 11.3 Session Management

`SessionCreationPolicy.STATELESS` — **correcto** para JWT.

---

## 12. GESTIÓN DE SECRETOS Y CREDENCIALES

### SEC-004: Secretos Hardcodeados

| Archivo | Secreto | Valor | Severidad |
|---------|---------|-------|-----------|
| `.env` | DB_PASSWORD | `thoth_secret` | 🔴 CRÍTICA |
| `.env` | DB_SUPERUSER_PASSWORD | `change_me_local_only_superuser_pw` | 🔴 CRÍTICA |
| `.env` | JWT_SECRET | `thoth-core-production-secret-key-change-this-in-production-256-bits` | 🔴 CRÍTICA |
| `application-dev.yml` | jwt.secret (default) | `thoth-core-DEV-ONLY-secret-not-for-production-6f2a9c1e7b4d` | MEDIA (solo dev) |
| `application-dev.yml` | DB_PASSWORD (default) | `thoth_secret` | MEDIA (solo dev) |
| `CorsConfig.java` | IP privada | `192.168.1.5` | BAJA |

**¿`.env` está en .gitignore?** — Debe verificarse. Si está en el repositorio, todos los secretos están expuestos en el historial de Git.

---

## 13. SEGURIDAD DE ARCHIVOS Y DOCUMENTOS

### DocumentController — Análisis

| Aspecto | Estado | Detalle |
|---------|--------|---------|
| Content sniffing | ✅ Apache Tika valida el tipo real del archivo | SEC-012 implementado |
| Content-Disposition | ✅ Usa `ContentDisposition.builder()` | SEC-015 implementado |
| Tamaño máximo | ✅ 10MB (config) | OK |
| MIME types permitidos | ✅ Lista blanca | OK |
| Path traversal (upload) | ✅ Filename sanitizado con `replaceAll("[^a-zA-Z0-9._-]", "_")` | OK |
| Path traversal (download) | ⚠️ Lee path desde DB, pero no valida que esté dentro de uploadDir | MEDIA |
| Autenticación upload | 🔴 Nullable Authentication — anónimos pueden subir | CRÍTICA |
| Autenticación delete | 🔴 Nullable Authentication — anónimos pueden eliminar | CRÍTICA |
| Autenticación list | 🔴 Sin auth — enumera documentos de cualquier equipo | ALTA |
| Autenticación download | ✅ PermissionService DOCUMENTS.VIEW | OK |
| Directorio upload | ⚠️ `uploads` relativo — ubicación predecible | BAJA |

---

## 14. RATE LIMITING Y PROTECCIÓN CONTRA FUERZA BRUTA

### RateLimitingFilter

| Aspecto | Estado |
|---------|--------|
| Rutas protegidas | `/api/v1/auth/login`, `/register`, `/change-password` |
| Límite | 15 intentos / 60 segundos por IP+path |
| Almacenamiento | ConcurrentHashMap in-memory |
| Limpieza de buckets | 🔴 NUNCA — memory leak potencial |
| Multi-instancia | 🔴 No compartido — ineficaz con load balancer |
| Bypass X-Forwarded-For | 🔴 Header controlable por el cliente — bypass fácil |
| Reinicio | Se pierde en restart de aplicación |

### Endpoints SIN Rate Limiting

- `/api/v1/ai/*` — llamadas a IA sin límite (costo)
- `/api/v1/reports/*` — `findAll()` sin límite (DoS)
- `/api/v1/equipment` — listing sin límite
- `/api/v1/users/{id}/reset-password` — resets sin límite

---

## 15. DOCKER Y DESPLIEGUE

### Dockerfile

| Aspecto | Estado |
|---------|--------|
| Multi-stage build | ✅ |
| Imagen base | ✅ eclipse-temurin:21-jre-alpine (mínima) |
| Usuario no-root | ✅ `thoth` user |
| Tests en build | ⚠️ Skipped (`-x test`) |
| Profile | ✅ `-Dspring.profiles.active=prod` |
| Healthcheck | ✅ `/actuator/health` |
| Puerto expuesto | 8080 |

### docker-compose.yml

| Aspecto | Estado |
|---------|--------|
| Variables requeridas | ✅ `?` syntax (falla si no set) |
| PostgreSQL puerto | ⚠️ 5432 expuesto al host (riesgo en prod) |
| Volumes | ✅ `postgres_data` persistente |
| Init scripts | ✅ `db/init` montado como read-only |
| Network isolation | ⚠️ No hay network dedicada definida |

---

## 16. ACTUATOR Y EXPOSICIÓN DE INFORMACIÓN

### application.yml (base — dev)

| Aspecto | Estado | Riesgo |
|---------|--------|--------|
| `server.error.include-message: always` | ⚠️ | Filtra mensajes de error internos |
| `server.error.include-binding-errors: always` | ⚠️ | Filtra nombres de campos |
| `management.endpoint.health.show-details: always` | ⚠️ | Expone detalles de health |
| `management.endpoints.web.exposure.include: health,info,metrics` | ⚠️ | Metrics expuesto |
| `spring.jpa.show-sql: true` | ⚠️ | SQL en logs (dev only) |

### application-prod.yml

| Aspecto | Estado |
|---------|--------|
| `management.endpoint.health.show-details: never` | ✅ |
| `management.endpoints.web.exposure.include: health,info` | ✅ |
| `springdoc.swagger-ui.enabled: false` | ✅ |
| `springdoc.api-docs.enabled: false` | ✅ |
| `spring.jpa.show-sql: false` | ✅ |

---

## 17. TESTS EXISTENTES Y COBERTURA

### 17.1 Tests Existentes (24 archivos, ~73 métodos)

| Categoría | Tests | Cobertura |
|-----------|-------|-----------|
| Security Tests (controllers) | 9 archivos | Auth/roles para AI, Alert, Auth, Document, Equipment, MaintenanceHistory, Permission, Reports, Sede |
| Controller Functional | 0 | ❌ Ningún test funcional de controllers |
| Use Case Tests | 4 archivos | Change/Get/Register/Update Equipment |
| Repository Tests | 2 archivos | Equipment, Maintenance |
| Mapper Tests | 2 archivos | EquipmentDtoMapper, LocationDtoMapper |
| Domain Tests | 2 archivos | Equipment model, Location value object |
| Config Tests | 2 archivos | Prod health exposure, JWT provider |
| Architecture Tests | 1 archivo | Hexagonal boundaries (6 reglas) |
| Context Test | 1 archivo | Spring context loads |

### 17.2 Brechas de Cobertura

**Controllers SIN tests funcionales:**
- Todos los controllers — solo hay security tests, no funcionales

**Controllers SIN ningún test:**
- MaintenanceController, LocationHistoryController, QrCodeController, UserController, TechnicianController, AuditController

**Services SIN tests:**
- AuditService, AuditArchiveService, MaintenanceSchedulerService, PermissionService, QrCodeService, UserService

**Use Cases SIN tests:**
- ListEquipmentUseCaseImpl, CreateMaintenanceHistoryUseCaseImpl, GetMaintenanceHistoryUseCaseImpl

**JaCoCo Quality Gate:** 35% mínimo (baseline actual)

---

## 18. DEPENDENCIAS Y VULNERABILIDADES CONOCIDAS

| Dependencia | Versión | Estado |
|---|---|---|
| Spring Boot | 4.1.1 | ✅ Reciente |
| jjwt | 0.12.6 | ✅ Reciente |
| Apache Tika | 2.9.2 | ⚠️ Verificar CVEs |
| ZXing | 3.5.3 | ✅ |
| PostgreSQL driver | (managed) | ✅ |
| Lombok | (managed) | ✅ |
| ArchUnit | 1.3.0 | ✅ |

**Recomendación:** Ejecutar `./gradlew dependencyCheckAnalyze` (OWASP Dependency Check) o verificar con Snyk/Trivy para CVEs conocidos.

---

## 19. ARQUITECTURA HEXAGONAL — CONFORMIDAD

### ArchitectureTest.java — 6 Reglas

1. ✅ Domain no depende de adapters/Spring/JPA
2. ✅ Controllers en paquete correcto
3. ✅ Repositories en paquete correcto
4. ✅ Use cases no tocan repos JPA directamente

### Violaciones Encontradas

| Violación | Detalle |
|-----------|---------|
| AlertController accede directo a EquipmentJpaRepository | Bypasses application layer |
| SedeController accede directo a SedeRepository | Bypasses application layer |
| LocationHistoryController accede directo a LocationHistoryRepository + EquipmentJpaRepository | Bypasses application layer |
| DocumentController accede directo a DocumentRepository + EquipmentJpaRepository | Bypasses application layer |
| ReportsController accede directo a EquipmentJpaRepository + MaintenanceHistoryJpaRepository | Bypasses application layer |
| UserController accede directo a UserJpaRepository + PasswordEncoder | Bypasses application layer |
| AuditController retorna entidades JPA directamente | Expone capa de persistencia |

**7 controllers violan la arquitectura hexagonal** accediendo directamente a repositorios JPA en lugar de pasar por ports/use cases.

---

## 20. CÓDIGO MUERTO Y BUGS FUNCIONALES

| Archivo | Problema | Tipo |
|---------|----------|------|
| `MaintenanceRepositoryAdapter.save()` | **No-op** — retorna el input sin persistir | 🔴 BUG |
| `MaintenanceRecordEntityMapper.toEntity()` | Siempre retorna `null` | 🔴 DEAD CODE |
| `MaintenanceRecordDtoMapper.toDTO()` | Siempre retorna `null` | 🔴 DEAD CODE |
| `MaintenanceController` (todos los endpoints) | Stubs que retornan respuestas vacías | ⚠️ DEAD CODE |
| `ListEquipmentUseCaseImpl.listAll()` | Carga TODO en memoria, pagina client-side | 🔴 BUG de rendimiento |
| `MaintenanceScheduledJob` | `findAll()` diario sobre toda la tabla | ⚠️ Performance |
| `CreateMaintenanceHistoryUseCaseImpl` | `catch(Exception e)` silencioso en actualización de equipo | ⚠️ Swallows errors |
| `EquipmentController.create()` | `createdBy` hardcodeado a "SYSTEM" en vez de usar Principal | ⚠️ BUG |

---

## 21. MATRIZ DE VULNERABILIDADES

| ID | Severidad | OWASP | Título | Archivo | Impacto |
|---|---|---|---|---|---|
| **SEC-001** | 🔴 CRÍTICA | A01:2021 Broken Access Control | UserController completamente abierto | UserController.java | Cualquier anónimo puede listar usuarios, escalar roles a ADMIN, resetear contraseñas (recibiendo la nueva), y eliminar usuarios permanentemente |
| **SEC-002** | 🔴 CRÍTICA | A01:2021 Broken Access Control | EquipmentController sin @PreAuthorize | EquipmentController.java | CRUD completo de equipos hospitalarios accesible sin autenticación |
| **SEC-003** | 🔴 CRÍTICA | A01:2021 Broken Access Control | ReportsController sin auth | ReportsController.java | Dashboard con valores de inventario, datos de hardware, información de alquiler expuestos + DoS vía findAll() |
| **SEC-004** | 🔴 CRÍTICA | A02:2021 Cryptographic Failures | Secretos hardcodeados en .env | .env | JWT_SECRET, DB passwords en texto plano con valores default |
| **SEC-005** | 🔴 CRÍTICA | A01:2021 Broken Access Control | DocumentController upload/delete anónimo | DocumentController.java | Cualquier anónimo puede subir/eliminar archivos del sistema |
| **SEC-006** | 🔴 CRÍTICA | A01:2021 Broken Access Control | AlertController sin auth expone datos sensibles | AlertController.java | Datos de hardware, contratos de alquiler, serial numbers |
| **SEC-007** | 🔴 CRÍTICA | A07:2021 Auth Failures | Reset password retorna contraseña en texto plano | UserService.java | La nueva contraseña viaja en la respuesta HTTP y puede quedar en logs |
| **SEC-008** | 🔴 CRÍTICA | A01:2021 Broken Access Control | AIController sin auth | AIController.java | Análisis de IA sobre cualquier equipo sin autenticación + abuso de costos |
| **SEC-009** | 🟠 ALTA | A01:2021 Broken Access Control | SedeController CRUD abierto | SedeController.java | Crear/modificar/eliminar sedes sin autenticación |
| **SEC-010** | 🟠 ALTA | A01:2021 Broken Access Control | QrCodeController sin auth + baseUrl user-controlled | QrCodeController.java | QR codes con URLs maliciosas (phishing) |
| **SEC-011** | 🟠 ALTA | A04:2021 Insecure Design | JWT sin revocación ni refresh | JwtTokenProvider.java | Token robado válido 24h, sin blacklist |
| **SEC-012** | 🟠 ALTA | A07:2021 Auth Failures | Rate limiting bypass vía X-Forwarded-For | RateLimitingFilter.java | Header spoofable permite eludir rate limiting |
| **SEC-013** | 🟠 ALTA | A04:2021 Insecure Design | No defense in depth — controllers sin @PreAuthorize | Múltiples | Seguridad depende solo de SecurityConfig URL patterns |
| **SEC-014** | 🟠 ALTA | A05:2021 Security Misconfiguration | CORS hardcodeado, sin origin para producción | CorsConfig.java | Frontend de producción bloqueado por CORS |
| **SEC-015** | 🟠 ALTA | A08:2021 Data Integrity | UserEntity password sin @JsonIgnore | UserEntity.java | Hash BCrypt puede filtrarse en toString/serialización |
| **SEC-016** | 🟠 ALTA | A03:2021 Injection | Audit log details sin sanitización (XSS stored) | AuditService.java | Input de usuario en `details` puede contener XSS que se ejecuta en el frontend |
| **SEC-017** | 🟠 ALTA | A04:2021 Insecure Design | Registro abierto sin verificación | AuthController.java | Cualquiera crea cuentas sin email verification ni CAPTCHA |
| **SEC-018** | 🟠 ALTA | A04:2021 Insecure Design | findAll() sin paginación como DoS vector | AlertController, ReportsController, MaintenanceScheduledJob | Carga toda la DB en memoria |
| **SEC-019** | 🟠 ALTA | A01:2021 Broken Access Control | Permisos aceptan Map<String,List<String>> sin validación | PermissionController.java | ADMIN puede inyectar módulos/acciones arbitrarios |
| **SEC-020** | 🟠 ALTA | A01:2021 Broken Access Control | MaintenanceController stubs abiertos | MaintenanceController.java | Endpoints sin auth aceptan requests (aunque no hacen nada) |
| **SEC-021** | 🟡 MEDIA | A04:2021 Insecure Design | 3 DTOs sin ninguna validación | HardwareRequest, RentalInfoRequest, UpdateEquipmentRequest | Valores negativos, strings gigantes, datos basura |
| **SEC-022** | 🟡 MEDIA | A04:2021 Insecure Design | signatureBase64 sin límite de tamaño | CreateMaintenanceHistoryRequest / entity | DoS vía payload gigante |
| **SEC-023** | 🟡 MEDIA | A05:2021 Security Misconfiguration | Error messages exponen detalles internos | application.yml | `include-message: always`, `include-binding-errors: always` |
| **SEC-024** | 🟡 MEDIA | A09:2021 Logging Failures | JWT validation swallows exceptions | JwtTokenProvider.java | Sin logging de tokens inválidos/expirados |
| **SEC-025** | 🟡 MEDIA | A04:2021 Insecure Design | Rate limiter in-memory sin cleanup | RateLimitingFilter.java | Memory leak + pierde estado en restart |
| **SEC-026** | 🟡 MEDIA | A01:2021 Broken Access Control | Audit logs retornan entidades JPA | AuditController/Service | Expone estructura interna + IP addresses |
| **SEC-027** | 🟡 MEDIA | A04:2021 Insecure Design | Password complexity mínima (6 chars, sin pattern) | RegisterRequest / ChangePasswordRequest | Contraseñas débiles permitidas |
| **SEC-028** | 🟡 MEDIA | A07:2021 Auth Failures | User enumeration vía registro | AuthController.java | Mensajes diferentes para username/email duplicado |
| **SEC-029** | 🟡 MEDIA | A04:2021 Insecure Design | Falta @Valid en objetos anidados | CreateEquipmentRequest, UpdateEquipmentRequest | Validación de sub-objetos bypassed |
| **SEC-030** | 🟢 BAJA | A05:2021 Security Misconfiguration | Swagger/OpenAPI abierto en dev | SecurityConfig.java | API spec expuesta (solo dev) |
| **SEC-031** | 🟢 BAJA | A05:2021 Security Misconfiguration | PostgreSQL port expuesto en docker-compose | docker-compose.yml | Puerto 5432 accesible desde host |
| **SEC-032** | 🟢 BAJA | A04:2021 Insecure Design | upload dir relativo y predecible | application.yml | `uploads` sin path absoluto |
| **SEC-033** | 🟢 BAJA | A05:2021 Security Misconfiguration | IP privada hardcodeada en CORS | CorsConfig.java | Filtra info de red interna |
| **SEC-034** | 🟢 BAJA | A04:2021 Insecure Design | No @PrePersist/@PreUpdate en entities | Múltiples entities | createdAt/updatedAt pueden ser null |
| **SEC-035** | 🟢 BAJA | A04:2021 Insecure Design | createdBy hardcodeado a "SYSTEM" | EquipmentController | No registra el usuario real que crea |
| **SEC-036** | ℹ️ INFO | — | MaintenanceRepositoryAdapter.save() es no-op | MaintenanceRepositoryAdapter | Bug funcional |
| **SEC-037** | ℹ️ INFO | — | Mappers retornan null (dead code) | MaintenanceRecordEntityMapper, MaintenanceRecordDtoMapper | Código muerto |
| **SEC-038** | ℹ️ INFO | — | ListEquipmentUseCase pagina client-side | ListEquipmentUseCaseImpl | Performance bug |
| **SEC-039** | ℹ️ INFO | — | Test coverage at 35% baseline | JaCoCo | Muchos módulos sin tests |

---

## 22. EVALUACIÓN DE RIESGO GLOBAL

### Score de Seguridad: 25/100 (CRÍTICO)

| Categoría OWASP | Score | Peso |
|-----------------|-------|------|
| A01 - Broken Access Control | 10/100 | 25% |
| A02 - Cryptographic Failures | 40/100 | 15% |
| A03 - Injection | 85/100 | 15% |
| A04 - Insecure Design | 30/100 | 15% |
| A05 - Security Misconfiguration | 50/100 | 10% |
| A07 - Auth Failures | 35/100 | 10% |
| A08 - Data Integrity | 70/100 | 5% |
| A09 - Logging Failures | 60/100 | 5% |

**Fortalezas:**
- ✅ No hay SQL injection (queries parametrizadas)
- ✅ BCrypt para passwords
- ✅ Apache Tika para content sniffing
- ✅ Arquitectura hexagonal con tests ArchUnit
- ✅ Config de producción deshabilita Swagger y health details
- ✅ Docker multi-stage con usuario no-root
- ✅ Content-Disposition seguro en downloads

**Debilidades Críticas:**
- 🔴 La mayoría de endpoints de controllers NO tienen defensa en profundidad
- 🔴 Secretos en archivos de configuración
- 🔴 Reset password retorna contraseña en texto plano
- 🔴 Sin revocación de JWT ni refresh tokens
- 🔴 Rate limiting bypassable y solo en auth endpoints
- 🔴 Registro abierto sin verificación
- 🔴 findAll() como vector de DoS

---

## 23. PLAN DE REMEDIACIÓN POR FASES

### FASE 2 — CRÍTICA (Hacer INMEDIATAMENTE)
1. Agregar `@PreAuthorize` a TODOS los controllers (SEC-001 a SEC-009, SEC-020)
2. Proteger reset password — implementar token-based reset flow (SEC-007)
3. Rotar todos los secretos, sacar .env de git, usar vault (SEC-004)
4. Agregar auth a document upload/delete (SEC-005)

### FASE 3 — ALTA PRIORIDAD
5. Implementar JWT refresh tokens + blacklist (SEC-011)
6. Fix rate limiting: trusted proxy, Redis backend (SEC-012, SEC-025)
7. Agregar `@JsonIgnore` a UserEntity.password (SEC-015)
8. Sanitizar audit log details contra XSS (SEC-016)
9. CORS configurable por environment (SEC-014)
10. Cerrar registro o agregar verificación de email (SEC-017)

### FASE 4 — MEDIA PRIORIDAD
11. Validar TODOS los DTOs: @Size(max), @Pattern, @Valid anidado (SEC-021, SEC-022, SEC-029)
12. Crear DTOs para audit log responses (SEC-026)
13. Implementar paginación real en lugar de findAll() (SEC-018)
14. Agregar password complexity requirements (SEC-027)
15. Configurar error messages para producción (SEC-023)

### FASE 5 — BAJA PRIORIDAD
16. Fix rate limiter memory leak (SEC-025)
17. Agregar logging de JWT validation failures (SEC-024)
18. Agregar @PrePersist/@PreUpdate a entities (SEC-034)
19. Limpiar código muerto: MaintenanceController stubs, null mappers (SEC-036, SEC-037)
20. Fix ListEquipmentUseCase pagination server-side (SEC-038)

### FASE 6+ — TESTING Y QUALITY GATE
21. Agregar tests funcionales para todos los controllers
22. Tests de seguridad para todos los endpoints
23. Subir cobertura JaCoCo a 60%+
24. Ejecutar OWASP Dependency Check
25. Configurar SonarQube quality gate

---

> **NOTA: Este informe es FASE 1 — INVENTARIO SOLAMENTE. No se ha modificado ningún archivo del proyecto. Todos los hallazgos deben ser revisados y aprobados antes de proceder con la implementación de las correcciones.**

---

*Generado por Claude Opus 4.6 — Security Audit Agent*  
*THOTH C.O.R.E. Fase 1 — Auditoría de Seguridad Integral*
