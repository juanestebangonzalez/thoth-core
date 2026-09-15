# THOTH C.O.R.E. — Security Tests

Inventario de las pruebas relevantes a seguridad y arquitectura, qué
vulnerabilidad/regla cubre cada una, y cómo ejecutarlas. Ver `SECURITY.md`
para el contexto de cada control.

## Cómo ejecutar

```bash
./gradlew test                          # toda la suite (85 tests)
./gradlew check                         # test + ArchUnit + piso de cobertura JaCoCo
./gradlew jacocoTestReport               # reporte HTML en build/reports/jacoco/test/html/index.html
```

## Autenticación / SEC-002 / SEC-007 / SEC-008 — `AuthControllerTest`

| Test | Cubre |
|---|---|
| `register_ignoresRoleFieldAndAlwaysAssignsUserRole` | SEC-002: el registro público ignora un `role` arbitrario en el payload; el usuario creado queda con rol `USER` sin importar lo enviado. |
| `register_duplicateUsername_returnsBadRequestWithoutCreatingAccount` | Registro duplicado no crea una segunda cuenta. |
| `register_existingUsername_returnsSpecificMessage` | SEC-007: documenta el comportamiento aceptado (mensaje específico de username duplicado). |
| `login_afterExceedingRateLimit_returns429` | SEC-008: tras exceder el umbral configurado, `/api/v1/auth/login` responde `429` sin importar las credenciales. |

## JWT / SEC-004 — `JwtTokenProviderTest`

| Test | Cubre |
|---|---|
| `validateToken_rejectsTokenSignedWithFormerHardcodedDefaultSecret` | Un JWT firmado con el antiguo secreto público hardcodeado es rechazado contra un secreto real distinto. |
| `validateToken_acceptsTokenSignedWithConfiguredSecret` | Caso positivo: un token firmado con el secreto configurado se valida correctamente. |

## Equipment — Autorización / SEC-001 — `EquipmentControllerSecurityTest`

| Test | Cubre |
|---|---|
| `listEquipment_withoutAuthentication_returns401` | `GET` sin JWT → 401. |
| `listEquipment_authenticatedViewer_returns200` | `GET` con cualquier rol autenticado (incl. `VIEWER`) → 200. |
| `createEquipment_withoutAuthentication_returns401` | `POST` sin JWT → 401. |
| `createEquipment_withViewerRole_returns403` | `POST` con rol insuficiente (`VIEWER`) → 403. |
| `createEquipment_withTechnicianRole_returns201` | `POST` con rol autorizado (`TECHNICIAN`) → 201. |
| `updateEquipment_withoutAuthentication_returns401` | `PUT` sin JWT → 401. |
| `updateEquipment_withViewerRole_returns403` | `PUT` con rol insuficiente → 403. |
| `deleteEquipment_withoutAuthentication_returns401` | `DELETE` sin JWT → 401. |
| `deleteEquipment_withUserRole_returns403` | `DELETE` con rol insuficiente (`USER`) → 403. |
| `deleteEquipment_withAdminRole_onMissingEquipment_returns404` | `DELETE` con rol autorizado llega al caso de uso (404 por no existir, no 401/403). |

## Permisos / SEC-003 — `PermissionControllerSecurityTest`

| Test | Cubre |
|---|---|
| `getUserPermissions_withoutAuthentication_returns401` | Sin JWT → 401. |
| `getUserPermissions_withViewerRole_returns403` | Rol no-ADMIN → 403 (`@PreAuthorize`). |
| `getUserPermissions_withAdminRole_returns200` | Rol `ADMIN` → 200. |
| `setUserPermissions_withViewerRole_cannotSelfEscalate_returns403` | Un `VIEWER` no puede auto-asignarse permisos de `USERS` → 403. |

## Documentos / SEC-005 — `DocumentControllerSecurityTest`

| Test | Cubre |
|---|---|
| `download_withoutAuthentication_returns401` | Descarga sin JWT → 401 (antes era pública). |
| `download_withViewerRole_returns403` | Rol sin permiso `DOCUMENTS:VIEW` → 403. |
| `download_withUserRole_onMissingDocument_returns404` | Rol con permiso llega al lookup real (404 por no existir el documento). |

## Subida de archivos / SEC-012 — `DocumentUploadContentSniffingTest`

| Test | Cubre |
|---|---|
| `upload_withContentTypeMismatchingRealBytes_isRejected` | Un archivo cuyo contenido real no coincide con el `Content-Type` declarado (magic bytes vía Tika) es rechazado. |
| `upload_withContentMatchingDeclaredType_isAccepted` | Caso positivo: contenido real coincide con el tipo declarado → aceptado. |
| `upload_response_doesNotLeakInternalStorageFileName` | Arquitectura/seguridad: la respuesta de `upload` (`DocumentResponseDTO`) no incluye `fileName` (nombre interno en disco); antes se devolvía la entidad JPA completa. |
| `download_withQuoteInOriginalFilename_doesNotBreakContentDispositionHeader` | SEC-015 (encontrado en auditoría final): un nombre de archivo con comillas ya no puede inyectar parámetros extra en el header `Content-Disposition` de la descarga — se usa `ContentDisposition` de Spring (RFC 6266) en vez de concatenar strings. |

## Historial de mantenimiento / SEC-009 — `MaintenanceHistoryControllerSecurityTest`

| Test | Cubre |
|---|---|
| `getByEquipment_withoutAuthentication_returns401` | Sin JWT → 401. |
| `getByEquipment_withViewerRole_returns403` | Rol sin permiso `MAINTENANCE:VIEW` (`VIEWER`) → 403 (antes cualquier autenticado veía cualquier historial — IDOR). |
| `getByEquipment_withUserRole_returns200` | Rol con permiso → 200. |
| `getById_withViewerRole_returns403` | Mismo control aplicado también al endpoint por ID de mantenimiento individual. |

## Configuración / SEC-010 — `ProdHealthExposureConfigTest`

| Test | Cubre |
|---|---|
| `prodProfile_explicitlyDisablesHealthDetailExposure` | `application-prod.yml` fija explícitamente `management.endpoint.health.show-details: never` (antes heredaba `always` del perfil base, exponiendo detalles internos en `/actuator/health` sin autenticación). |

## Dominio / SEC-011 — `EquipmentTest`

| Test | Cubre |
|---|---|
| `testMarkAsActiveThrowsWhenRetired` | Un equipo `RETIRED` ya no puede "resucitarse" a `ACTIVE`; lanza `IllegalStateException`, igual que las demás transiciones protegidas (`markForMaintenance`, `markAsInactive`). |

(El resto de `EquipmentTest` cubre las reglas de negocio generales del
agregado `Equipment`, no solo la parte de seguridad.)

## Arquitectura — `ArchitectureTest` (ArchUnit)

| Test | Cubre |
|---|---|
| `domainMustNotDependOnAdapterOrApplication` | `domain` no importa nada de `adapter`/`application`/`config`. |
| `domainMustNotDependOnSpringFramework` | `domain` no importa `org.springframework.*`. |
| `domainMustNotDependOnPersistenceFramework` | `domain` no importa `jakarta.persistence.*` / `org.hibernate.*`. |
| `controllersMustResideInAdapterInPackage` | Todo `@RestController` vive en `adapter.in.rest.controller`. |
| `repositoriesMustResideInAdapterOutPackage` | Toda interfaz `*JpaRepository` vive en `adapter.out.persistence.repository`. |
| `equipmentUseCasesMustNotDependOnJpaRepositoriesDirectly` | `application.usecase..` nunca depende de un `*JpaRepository` directamente — siempre vía `EquipmentRepositoryPort`. Blinda el fix de `RegisterEquipmentUseCaseImpl`/`UpdateEquipmentUseCaseImpl`. |

## Casos de uso de Equipment — `RegisterEquipmentUseCaseTest`

| Test | Cubre |
|---|---|
| `testRegisterThrowsWhenInventoryNumberAlreadyExists` | La validación de número de inventario duplicado sigue funcionando tras mover el lookup de `EquipmentJpaRepository` directo a `EquipmentRepositoryPort.findByInventoryNumber(...)`. |

## Sedes — `SedeControllerTest` (nuevo)

| Test | Cubre |
|---|---|
| `create_thenListActive_returnsDtoWithExpectedFields` | `SedeController` responde `SedeDTO` (no `SedeEntity`) con los campos esperados, de punta a punta (no solo que compile). |
| `getById_withUnknownId_returns404` | Comportamiento de no encontrado se mantiene tras el cambio de tipo de retorno. |

## Alertas / SEC-013 (encontrado en auditoría final) — `AlertControllerSecurityTest`

| Test | Cubre |
|---|---|
| `upcomingMaintenance_withoutAuthentication_returns401` | Antes público: dump del inventario completo (nombre, serial, categoría) sin autenticar. |
| `hardwareCritical_withoutAuthentication_returns401` | Antes público: exponía qué equipos tienen hardware en estado crítico. |
| `rentalExpiring_withoutAuthentication_returns401` | Antes público: exponía empresa y fecha de vencimiento de contratos de alquiler. |
| `summary_withoutAuthentication_returns401` | Antes público: resumen agregado de las 3 alertas anteriores. |
| `upcomingMaintenance_authenticated_returns200` | Caso positivo: cualquier rol autenticado (incl. `VIEWER`) sigue pudiendo ver las alertas — no se quitó funcionalidad, solo se exigió login. |

## IA / SEC-014 (encontrado en auditoría final) — `AIControllerSecurityTest`

| Test | Cubre |
|---|---|
| `analyzeMaintenance_withoutAuthentication_returns401` | Antes público: cualquiera podía disparar un análisis de IA (costo por llamada externa) sobre cualquier equipo. |
| `predictFailure_withoutAuthentication_returns401` | Mismo control sobre el endpoint de predicción de fallas. |
| `recommendReplacement_withoutAuthentication_returns401` | Mismo control sobre el endpoint de recomendación de reemplazo. |

## Reportes (verificado, no era vulnerable) — `ReportsControllerSecurityTest`

| Test | Cubre |
|---|---|
| `dashboard_withoutAuthentication_returns401` | Confirma que `/api/v1/reports/dashboard` (KPIs agregados de todo el inventario) ya requería autenticación vía el catch-all de `SecurityConfig` — no era un hallazgo, pero se fija con un test para que no pueda volverse público por accidente. |
| `dashboard_authenticated_returns200` | Caso positivo. |

## Resto de la suite (no específicos de seguridad, pero necesarios para el Quality Gate)

`ChangeEquipmentStatusUseCaseTest`, `EquipmentDtoMapperTest`,
`EquipmentRepositoryTest`, `GetEquipmentUseCaseTest`, `LocationDtoMapperTest`,
`LocationTest`, `MaintenanceRepositoryTest`, `RegisterEquipmentUseCaseTest`,
`UpdateEquipmentUseCaseTest`, `ThothCoreApplicationTests` — cobertura
funcional de mappers, casos de uso y repositorios; se corrigieron en esta
auditoría porque estaban desincronizados de un refactor previo de
`EquipmentEntity`/`EquipmentCategory` (no compilaban) y bloqueaban poder
ejecutar la suite completa para validar los fixes de seguridad.

## Antes vs. después (resumen ejecutivo)

| | Antes | Después |
|---|---|---|
| Vulnerabilidades CRITICAL abiertas | 3 (SEC-001, 002, 003) | 0 |
| Vulnerabilidades HIGH abiertas | 3 (SEC-004, 005, 006) + 2 encontradas después (SEC-013, 014) = 5 | 0 |
| Vulnerabilidades MEDIUM abiertas | 3 (SEC-007, 008, 009) | 0 (SEC-007 mitigado y documentado, no "oculto") |
| Vulnerabilidades LOW abiertas | 3 (SEC-010, 011, 012) + 1 encontrada después (SEC-015) = 4 | 0 |
| Tests totales | 0 ejecutables (5 archivos no compilaban) | 85, 0 fallos |
| ArchUnit | No existía | 6 reglas activas |
| JaCoCo | No configurado | Configurado, piso 35% (medido ~36%) |
| SonarQube | No configurado | Plugin configurado; análisis real pendiente de servidor/token |
| Quality Gate local | N/A | `./gradlew check` (compilación + tests + ArchUnit + cobertura) |
