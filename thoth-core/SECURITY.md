# THOTH C.O.R.E. — Security

Documento vivo de la arquitectura de seguridad del backend, resultado de la
auditoría de seguridad ejecutada por fases (ver `SECURITY_TESTS.md` para el
detalle de pruebas). Refleja el estado real del código en este commit — no
inventa controles que no existan.

## 1. Arquitectura de seguridad

- Arquitectura hexagonal: `domain` (puro, sin Spring/JPA), `application`
  (casos de uso + puertos), `adapter.in.rest` (controllers), `adapter.out`
  (persistencia JPA, otros sistemas), `config` (Spring Security, JWT, CORS).
- El dominio no depende de Spring ni de JPA — blindado con ArchUnit
  (`ArchitectureTest`).
- **Corregido en esta pasada**: `SedeController` y `DocumentController` ya no
  devuelven entidades JPA directamente — se agregaron `SedeDTO` y
  `DocumentResponseDTO` (este último deliberadamente **no** expone
  `fileName`, el nombre interno con el que el archivo vive en disco — ver
  §11). `RegisterEquipmentUseCaseImpl` y `UpdateEquipmentUseCaseImpl` ya no
  inyectan `EquipmentJpaRepository` directamente: se agregó
  `EquipmentRepositoryPort.findByInventoryNumber(...)`, implementado en
  `EquipmentRepositoryAdapter`, y ambos casos de uso pasaron a depender solo
  del puerto. Blindado con ArchUnit
  (`equipmentUseCasesMustNotDependOnJpaRepositoriesDirectly`). Se eliminaron
  además `application.dto.CreateEquipmentRequest`/`UpdateEquipmentRequest`,
  confirmados como código muerto duplicado (0 referencias fuera de sí
  mismos) tras búsqueda exhaustiva.
- **Conocido, no corregido en esta pasada**: `AlertController`,
  `ReportsController`, `QrCodeController`, `AIController`, y las escrituras
  de `SedeController`/`DocumentController` (que sí siguen usando
  `SedeRepository`/`DocumentRepository` para leer/guardar, aunque ya no
  devuelven la entidad) siguen inyectando el repositorio de persistencia
  directamente en vez de pasar por una capa `application`/puerto dedicada.
  A diferencia del caso de Equipment, estos módulos no tienen hoy ningún
  caso de uso ni puerto — corregirlo significa **crear una capa hexagonal
  nueva** (dominio + puerto + adaptador) para cada uno, no ajustar una
  existente. Es una violación arquitectónica de bajo riesgo en sí misma
  (ya no hay fuga de datos ni de autorización — ver SEC-013/SEC-014 más
  abajo, que sí eran fugas reales y quedaron corregidas), y tocar la lógica
  de negocio de varios controladores que no había leído en profundidad
  antes sin ese diseño agregaría riesgo de romper funcionalidad — la regla
  explícita del proceso ("no rompas funcionalidad"). Recomendado como
  iniciativa aparte, con alcance y prioridad acordados explícitamente.

## 2. Autenticación

- JWT stateless (`SessionCreationPolicy.STATELESS`), sin sesiones de servidor
  ni cookies de autenticación.
- `POST /api/v1/auth/register` y `POST /api/v1/auth/login` son públicos por
  diseño; el registro **siempre** asigna rol `USER` (SEC-002 — ver más abajo),
  nunca acepta un rol arbitrario del cliente.
- Enumeración de usuarios (SEC-007): el registro devuelve un mensaje
  específico si el username/email ya existe. Es una decisión de UX aceptada
  y documentada en `AuthService.register()`; el riesgo se mitiga con rate
  limiting (ver §6), no ocultando el mensaje.

## 3. Autorización

- `SecurityConfig` define una matriz explícita por ruta y verbo HTTP (no un
  `permitAll()` amplio). En particular:
  - `/api/v1/equipment/**`: `GET` requiere solo estar autenticado; `POST` /
    `PUT` / `PATCH` / `DELETE` requieren rol `ADMIN` o `TECHNICIAN`.
  - `/api/v1/users/**`: solo `ADMIN`.
  - `/api/v1/permissions/**`: solo `ADMIN` (`@PreAuthorize` a nivel de clase
    en `PermissionController`).
  - `/api/v1/documents/*/download`: requiere autenticación + permiso
    `DOCUMENTS:VIEW`.
  - `/api/v1/maintenance-history/**` (lectura): requiere autenticación +
    permiso `MAINTENANCE:VIEW`.
  - `/api/v1/alerts/**`: requiere autenticación (SEC-013 — antes público).
  - `/api/v1/ai/**`: requiere autenticación (SEC-014 — antes público).
- **SEC-013 (HIGH, encontrado en auditoría final)**: `AlertController`
  consulta `EquipmentJpaRepository` directamente y devolvía en 4 endpoints
  (`upcoming-maintenance`, `hardware-critical`, `rental-expiring`,
  `summary`) el inventario completo de equipos — nombres, números de
  serie, salud de hardware, empresa de alquiler — sin ninguna
  autenticación. Evadía por completo la protección de SEC-001 sobre
  `/api/v1/equipment/**`, ya que consultaba el mismo dato por otra ruta.
  Corregido exigiendo autenticación.
- **SEC-014 (HIGH, encontrado en auditoría final)**: `AIController` permitía
  a cualquiera, sin autenticar, disparar análisis de IA (con costo probable
  por llamada a un LLM externo vía `AIAgentPort`) sobre cualquier
  `equipmentId`, filtrando indirectamente datos del equipo y habilitando
  abuso de costos/DoS. Corregido exigiendo autenticación.
- Permisos granulares por módulo/acción viven en `PermissionService` +
  `UserPermissionEntity`. `PermissionService.requireModulePermission(...)`
  es el guard reutilizable para controllers; lanza `AccessDeniedException`
  (→ 403 vía `GlobalExceptionHandler`).
- `UserService.changeRole()` re-siembra los permisos por defecto del nuevo
  rol al promover/degradar un usuario, para que las guardas de permiso no
  dejen a un ADMIN recién promovido sin acceso.
- `AuthenticationEntryPoint` explícito (`HttpStatusEntryPoint(401)`) distingue
  "no autenticado" (401) de "autenticado sin permiso" (403).

## 4. JWT

- `JwtTokenProvider` firma con HS256 usando `jwt.secret`, **sin valor por
  defecto en el código** (antes tenía un secreto público hardcodeado —
  SEC-004, corregido). Si `JWT_SECRET` no está definido en producción, la
  aplicación falla al arrancar en vez de usar un secreto conocido.
- `dev` y `test` usan secretos propios, distintos entre sí y del antiguo
  valor público, definidos en `application-dev.yml` / `application-test.yml`.
- Expiración configurable (`jwt.expiration`, default 24h).

## 5. Roles y permisos

- Roles: `ADMIN`, `TECHNICIAN`, `USER`, `VIEWER` (`UserEntity.UserRole`).
- Permisos por módulo/acción (`PermissionService.ALL_MODULES` /
  `ALL_ACTIONS`), con defaults por rol en `getDefaultPermissions()`.

## 6. Rate limiting (SEC-008)

- `RateLimitingFilter` (ventana fija en memoria, por IP + ruta) protege
  `/api/v1/auth/login`, `/register`, `/change-password`. Excede el umbral →
  `429 Too Many Requests`.
- Configurable vía `security.rate-limit.max-attempts` /
  `security.rate-limit.window-ms` (default: 15 intentos / 60s).
- Limitación conocida: el estado es en memoria por instancia — en un
  despliegue con múltiples réplicas cada instancia lleva su propio contador.
  Para multi-instancia real, migrar a un backend compartido (Redis) es la
  mejora recomendada.

## 7. Secretos (SEC-004, SEC-006)

- `.env` (raíz y `thoth-core/`) están en `.gitignore` — antes no lo estaban.
- `application-prod.yml` no tiene defaults para `JWT_SECRET` ni `DB_PASSWORD`
  — deben inyectarse por entorno.
- La app ya NO se conecta a PostgreSQL como superusuario: hay un rol
  dedicado `thoth_app` con privilegios limitados al esquema de la app,
  creado por `db/init/01-create-app-role.sh` (docker-entrypoint-initdb.d).
  El superusuario `postgres` solo se usa para el bootstrap del contenedor.

## 8. PostgreSQL / SQL Injection

- Todo el acceso a datos pasa por Spring Data JPA (queries derivadas +
  Hibernate) — no hay `createNativeQuery`, `JdbcTemplate` ni concatenación
  de SQL en el código (`grep` de todo `src/main/java` sin resultados).
  Riesgo de SQL Injection: no identificado.

## 9. XSS / CSRF / CORS

- API REST pura JSON (sin vistas server-side), serializada con Jackson — el
  vector clásico de XSS reflejado/almacenado vía HTML renderizado no aplica
  aquí; el frontend (Angular, repo separado) es responsable de escapar al
  renderizar.
- CSRF deshabilitado (`csrf().disable()`): correcto para una API stateless
  autenticada por header `Authorization: Bearer`, sin cookies de sesión (CSRF
  explota cookies enviadas automáticamente por el navegador).
- CORS (`CorsConfig`): lista explícita de orígenes permitidos (no
  wildcard), `allowCredentials(true)`. **Nota**: los orígenes están
  hardcodeados a un par de IPs/puertos de desarrollo — antes de desplegar a
  un entorno real hay que externalizarlos por perfil/variable de entorno.

## 10. IDOR / BOLA / Mass Assignment

- SEC-002: registro público ya no acepta `role` del cliente.
- SEC-009: `MaintenanceHistoryController` (lectura de historial por
  `equipmentId`) exige permiso `MAINTENANCE:VIEW` — antes cualquier
  autenticado veía el historial de cualquier equipo.

## 11. File Security (SEC-005, SEC-012, SEC-015)

- Descarga de documentos requiere autenticación + permiso `DOCUMENTS:VIEW`
  (antes era pública).
- Subida de documentos valida el `Content-Type` declarado contra los bytes
  reales del archivo (Apache Tika) — ya no confía ciegamente en el header
  del cliente.
- Nombre de archivo sanitizado (`[^a-zA-Z0-9._-]` → `_`) antes de resolver
  la ruta en disco; no se preservan separadores de ruta del nombre original,
  por lo que no hay path traversal vía `originalFilename`.
- `DocumentResponseDTO` (respuesta de `upload`/`listByEquipment`) expone
  `originalName` pero **no** `fileName` (el nombre interno con el que el
  archivo se guarda en disco, con prefijo UUID) — antes se devolvía la
  entidad JPA completa, filtrando ese detalle de implementación.
- **SEC-015 (LOW, encontrado en auditoría final)**: el header
  `Content-Disposition` de la descarga se construía concatenando
  `originalName` (controlado por el cliente al subir el archivo) crudo
  dentro del valor del header (`"attachment; filename=\"" + name + "\""`).
  Un nombre de archivo con una comilla doble podía cerrar el valor
  entrecomillado anticipadamente e inyectar parámetros adicionales en el
  header. Corregido usando `ContentDisposition` de Spring, que codifica el
  valor según RFC 6266 en vez de concatenar strings.

## 12. Security headers

- Spring Security aplica su set de headers por defecto (no se deshabilitó):
  `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`,
  `X-XSS-Protection: 0`, `Cache-Control` restrictivo en respuestas de error.
  Verificable en cualquier respuesta capturada por los tests de MockMvc.

## 13. HTTPS

- No se termina TLS dentro de la aplicación Spring Boot; se asume
  terminación TLS en el reverse proxy / load balancer delante del contenedor
  (patrón estándar en despliegues Docker/Kubernetes). Fuera del alcance del
  código de aplicación.

## 14. Logging

- No se encontró logging de contraseñas, tokens ni secretos
  (`grep` de patrones sensibles en `src/main/java` sin resultados). Sin
  `printStackTrace` ni `System.out`.
- `GlobalExceptionHandler` no filtra stack traces al cliente: los errores no
  mapeados devuelven un mensaje genérico ("An unexpected error occurred").

## 15. Exception handling

- `GlobalExceptionHandler` centraliza el mapeo de excepciones → HTTP status,
  incluyendo `AccessDeniedException` / `AuthorizationDeniedException` → 403
  (antes estas últimas quedaban sin mapear y el `catch-all` las convertía en
  500, ocultando la denegación real).

## 16. Dependencias

- Sin hallazgos de versiones con vulnerabilidades conocidas verificadas en
  esta pasada (no se ejecutó un scanner de CVEs tipo OWASP Dependency-Check
  / Snyk — no disponible en este entorno). **Recomendado como seguimiento**:
  correr un scanner de dependencias en CI.

## 17. Testing / JaCoCo / ArchUnit / SonarQube / Quality Gate

Ver `SECURITY_TESTS.md` para el detalle de qué prueba cada test.

- **JaCoCo**: configurado (`build.gradle`), genera reporte XML+HTML en cada
  `test`, y `jacocoTestCoverageVerification` (enganchado a `check`) exige un
  piso de cobertura por instrucciones del 35% (medido: ~36% al momento de
  escribir esto — piso de no-regresión, no una meta aspiracional).
- **ArchUnit**: `ArchitectureTest` blinda que `domain` no dependa de Spring,
  JPA, `adapter` ni `application`; que los `@RestController` vivan en
  `adapter.in.rest.controller`; que las interfaces `*JpaRepository` vivan en
  `adapter.out.persistence.repository`; y que `application.usecase..` nunca
  dependa de un `*JpaRepository` directamente (siempre vía puerto).
- **SonarQube**: plugin `org.sonarqube` configurado en `build.gradle`
  (`sonar.projectKey`, integración con el XML de JaCoCo). **No se ejecutó**
  `./gradlew sonar` — requiere un servidor SonarQube/SonarCloud real y
  `-Dsonar.host.url` / `-Dsonar.token` (o `SONAR_TOKEN`), que no están
  disponibles en este entorno. Queda listo para ejecutarse en CI o
  localmente por el equipo.
- **Quality Gate**: no hay un Quality Gate de SonarQube real evaluado (por
  lo anterior). El gate local equivalente es `./gradlew check`, que exige:
  compilación limpia + 85 tests en verde + reglas ArchUnit + piso de
  cobertura JaCoCo.

## 18. Riesgos residuales conocidos

| # | Riesgo | Severidad | Por qué no se corrigió aquí |
|---|---|---|---|
| 1 | `AlertController`/`ReportsController`/`QrCodeController`/`AIController`/escrituras de `Sede`/`Document` inyectan el repositorio JPA en vez de pasar por un puerto | Bajo/Arquitectura | No existe hoy capa `application`/puerto para estos módulos; crearla es una iniciativa nueva, no un ajuste — se evitó para no arriesgar romper funcionalidad no auditada en profundidad. **Nota**: los riesgos de *autorización* de `AlertController` y `AIController` (no el de arquitectura) sí se corrigieron — ver SEC-013/SEC-014 |
| 2 | CORS con orígenes hardcodeados a IPs de dev | Bajo | Requiere decidir el/los dominios reales de producción |
| 3 | Rate limiting en memoria (no distribuido) | Bajo | Suficiente para una sola instancia; multi-instancia real necesita Redis u otro store compartido |
| 4 | Sin scanner de CVEs de dependencias en CI | Medio (proceso) | No hay herramienta de scanning disponible en este entorno |
| 5 | SonarQube no ejecutado | Medio (proceso) | Requiere servidor/token que no están disponibles aquí |
| 6 | `MaintenanceController` es un stub sin implementación real | Ninguno (no hay dato que proteger) | Preexistente, fuera del alcance de esta auditoría de seguridad |
| 7 | (informativo, no es un riesgo) `ReportsController`/`QrCodeController` revisados en la auditoría final | N/A | `ReportsController` (`/api/v1/reports/dashboard`, agrega KPIs de todo el inventario) cae bajo `anyRequest().authenticated()` — **no es público**, ya requiere login, confirmado leyendo el controlador completo. `QrCodeController` es `permitAll()` deliberado (códigos QR pensados para escanearse sin login) y solo genera una imagen con una URL — no expone datos sensibles. Ninguno de los dos necesitó corrección. |

**Corregidos en esta pasada** (ya no son riesgos residuales): entidades JPA
devueltas directamente en `SedeController`/`DocumentController`; casos de
uso de Equipment dependiendo del repositorio JPA en vez del puerto; DTOs
duplicados en `application.dto` confirmados como código muerto y eliminados;
**SEC-013** (`/api/v1/alerts/**` público, dump completo del inventario);
**SEC-014** (`/api/v1/ai/**` público, análisis de IA sin autenticar);
**SEC-015** (Content-Disposition de descarga construido por concatenación
de string con el nombre de archivo controlado por el cliente).
