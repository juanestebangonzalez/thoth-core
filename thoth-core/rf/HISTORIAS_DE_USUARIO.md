# THOTH C.O.R.E - Historias de Usuario (User Stories)
## Sistema de Gestión de Equipos de Cómputo - MVP v1.0.0

---

## 📋 FORMATO DE HISTORIA DE USUARIO

```
Como [rol]
Quiero [acción/funcionalidad]
Para [beneficio/valor]

Criterios de Aceptación:
- [ ] Criterio 1
- [ ] Criterio 2
- [ ] Criterio 3

Notas Técnicas:
- ...

Puntos: X
Prioridad: CRÍTICA/ALTA/MEDIA
```

---

## 🔧 SPRINT 1: FUNCIONALIDADES CORE

### US-001: Registrar Equipos en el Inventario

**Como** Administrador del IT  
**Quiero** registrar un nuevo equipo en el sistema  
**Para** mantener un inventario actualizado de los equipos de la organización

**Criterios de Aceptación:**
- [ ] Puedo ingresar nombre, categoría, serial number, marca y modelo del equipo
- [ ] El sistema valida que el serial number sea único
- [ ] Puedo ingresar la fecha de compra y el valor
- [ ] Puedo especificar la ubicación (edificio, piso, oficina)
- [ ] Puedo asignar el equipo a un usuario responsable
- [ ] El equipo se crea con estado ACTIVE automáticamente
- [ ] El sistema retorna un ID único para el equipo
- [ ] Recibo confirmación de registro exitoso
- [ ] Los datos se guardan en la BD

**Notas Técnicas:**
- Validar formato de MAC Address (XX:XX:XX:XX:XX:XX)
- Fecha de compra no puede ser futura
- Valor debe ser positivo
- Usar UUID para equipmentId

**Puntos:** 5  
**Prioridad:** CRÍTICA  
**Sprint:** 1

---

### US-002: Buscar Equipo por ID o Serial Number

**Como** Técnico de IT  
**Quiero** buscar un equipo específico por su ID o Serial Number  
**Para** obtener rápidamente información completa del equipo

**Criterios de Aceptación:**
- [ ] Puedo buscar por UUID del equipo
- [ ] Puedo buscar por Serial Number
- [ ] El sistema retorna todos los datos del equipo
- [ ] Se muestra el estado actual del equipo
- [ ] Se muestra la ubicación en formato legible
- [ ] Se muestra la antigüedad en años
- [ ] Recibo error si el equipo no existe
- [ ] La búsqueda responde en < 100ms

**Notas Técnicas:**
- Caché de resultados (5 minutos)
- Índices en id y serialNumber en BD

**Puntos:** 3  
**Prioridad:** CRÍTICA  
**Sprint:** 1

---

### US-003: Ver Lista de Todos los Equipos

**Como** Administrador  
**Quiero** ver un listado paginado de todos los equipos  
**Para** tener una visión general del inventario

**Criterios de Aceptación:**
- [ ] Veo un listado de equipos (20 por defecto)
- [ ] Puedo paginar entre resultados
- [ ] Puedo filtrar por estado (ACTIVE, MAINTENANCE, etc)
- [ ] Puedo filtrar por categoría
- [ ] Puedo filtrar por ubicación
- [ ] Puedo ordenar por nombre, estado, fecha de compra
- [ ] El listado muestra: ID, Nombre, Categoría, Estado, Ubicación
- [ ] Responde en < 500ms
- [ ] Puedo exportar a CSV

**Notas Técnicas:**
- Paginación: pageNumber, pageSize (máx 100)
- Caché de listados frecuentes
- Lazy loading en UI

**Puntos:** 4  
**Prioridad:** CRÍTICA  
**Sprint:** 1

---

### US-004: Actualizar Datos del Equipo

**Como** Técnico de IT  
**Quiero** actualizar información de un equipo  
**Para** mantener los datos del inventario correctos y actualizados

**Criterios de Aceptación:**
- [ ] Puedo editar el nombre del equipo
- [ ] Puedo cambiar la ubicación
- [ ] Puedo cambiar el responsable asignado
- [ ] NO puedo cambiar el Serial Number (protegido)
- [ ] NO puedo cambiar la fecha de compra
- [ ] El sistema valida los datos ingresados
- [ ] Los cambios se guardan inmediatamente
- [ ] Recibo confirmación del cambio
- [ ] Se registra quién y cuándo realizó el cambio (auditoría)

**Notas Técnicas:**
- Usar UpdateEquipmentCommand
- Registrar en tabla de auditoría
- Invalidar caché después de actualizar

**Puntos:** 3  
**Prioridad:** ALTA  
**Sprint:** 1

---

### US-005: Cambiar Estado del Equipo

**Como** Técnico de IT  
**Quiero** cambiar el estado de un equipo  
**Para** reflejar su disponibilidad (activo, en mantenimiento, inactivo, retirado)

**Criterios de Aceptación:**
- [ ] Puedo cambiar de ACTIVE a MAINTENANCE
- [ ] Puedo cambiar de MAINTENANCE a ACTIVE
- [ ] Puedo cambiar a INACTIVE (no disponible temporalmente)
- [ ] Puedo cambiar a RETIRED (fuera de servicio permanentemente)
- [ ] Una vez RETIRED, no puede volver a otro estado
- [ ] El sistema valida que la transición sea válida
- [ ] Recibo error si intento transición inválida
- [ ] Se registra la razón del cambio (opcional)
- [ ] Se publica evento de cambio de estado
- [ ] El cambio se audita

**Notas Técnicas:**
- State Machine pattern
- Validar transiciones en Equipment.java
- Publicar evento EquipmentStatusChanged

**Puntos:** 4  
**Prioridad:** CRÍTICA  
**Sprint:** 1

---

### US-006: Registrar Evento de Mantenimiento

**Como** Técnico de IT  
**Quiero** registrar cuando un equipo entra en mantenimiento  
**Para** llevar histórico de mantenimientos y trabajos realizados

**Criterios de Aceptación:**
- [ ] Puedo registrar mantenimiento PREVENTIVO (programado)
- [ ] Puedo registrar mantenimiento CORRECTIVO (por falla)
- [ ] Puedo registrar mantenimiento EMERGENCY (urgente)
- [ ] Puedo indicar la severidad (LOW, MEDIUM, HIGH, CRITICAL)
- [ ] Puedo agregar descripción del problema/trabajo
- [ ] Puedo especificar fecha programada
- [ ] El equipo se marca automáticamente como MAINTENANCE
- [ ] Se registra quién creó el mantenimiento
- [ ] Se genera ID único para el mantenimiento

**Notas Técnicas:**
- Crear entidad MaintenanceRecord
- Cambiar estado de equipo automáticamente
- Publicar evento MaintenanceRegistered

**Puntos:** 4  
**Prioridad:** ALTA  
**Sprint:** 1

---

### US-007: Completar Registro de Mantenimiento

**Como** Técnico de IT  
**Quiero** marcar un mantenimiento como completado  
**Para** actualizar el historial y devolver el equipo a estado ACTIVE

**Criterios de Aceptación:**
- [ ] Puedo completar un mantenimiento registrado
- [ ] Ingreso fecha de finalización
- [ ] Ingreso resultado del mantenimiento (éxito, parcial, fallo)
- [ ] Puedo agregar notas adicionales
- [ ] El equipo vuelve a estado ACTIVE automáticamente
- [ ] Se registra el tiempo total de mantenimiento
- [ ] Se publica evento MaintenanceCompleted
- [ ] El registro se audita

**Notas Técnicas:**
- Calcular duración = completedDate - scheduledDate
- Revertir estado del equipo a ACTIVE

**Puntos:** 3  
**Prioridad:** ALTA  
**Sprint:** 1

---

## 📊 SPRINT 2: ANÁLISIS E INTELIGENCIA

### US-008: Análisis Predictivo de Fallos

**Como** Administrador  
**Quiero** que el sistema prediga qué equipos podrían fallar pronto  
**Para** programar mantenimiento preventivo antes de que fallen

**Criterios de Aceptación:**
- [ ] El sistema analiza la antigüedad del equipo
- [ ] Considera el historial de mantenimientos
- [ ] Evalúa el tipo de equipo y su durabilidad típica
- [ ] Retorna nivel de riesgo: LOW, MEDIUM, HIGH, CRITICAL
- [ ] Indica una fecha estimada de fallo
- [ ] Proporciona un nivel de confianza (0-100%)
- [ ] Recomienda una acción (MONITOR, PREVENTIVE, REPLACE)
- [ ] El análisis se ejecuta automáticamente cada noche
- [ ] Recibo alertas para equipos de alto riesgo

**Notas Técnicas:**
- Integración con LangChain4j para IA
- Ejecutar con @Scheduled nightly
- Guardar resultados en tabla FailurePrediction
- Implementar algoritmo de scoring

**Puntos:** 8  
**Prioridad:** MEDIA  
**Sprint:** 2

---

### US-009: Recomendaciones de Reemplazo

**Como** Gerente de IT  
**Quiero** recibir recomendaciones de equipos que necesitan ser reemplazados  
**Para** planificar compras y presupuesto

**Criterios de Aceptación:**
- [ ] El sistema identifica equipos con > 5 años
- [ ] Identifica equipos con costo de mant > 40% del valor
- [ ] Verifica si el fabricante ya no da soporte
- [ ] Considera capacidad insuficiente
- [ ] Genera puntuación de "obsolescencia"
- [ ] Agrupa recomendaciones por urgencia
- [ ] Estima costo de reemplazo
- [ ] Puedo exportar lista de recomendaciones a Excel

**Notas Técnicas:**
- Crear vista ReplacementRecommendation
- Scoring algorithm con pesos

**Puntos:** 6  
**Prioridad:** MEDIA  
**Sprint:** 2

---

## 📈 SPRINT 3: REPORTES Y DASHBOARD

### US-010: Reporte de Inventario Ejecutivo

**Como** Director de IT  
**Quiero** un reporte ejecutivo del inventario  
**Para** reportar a la gerencia el estado de los activos

**Criterios de Aceptación:**
- [ ] Veo total de equipos por categoría
- [ ] Veo valor total del inventario
- [ ] Veo distribución por ubicación
- [ ] Veo distribución por estado
- [ ] Veo edad promedio de equipos
- [ ] Veo % de equipos que próximamente serán descontinuados
- [ ] Veo tendencia de inversión (últimos 12 meses)
- [ ] Puedo filtrar por período de fecha
- [ ] Puedo descargar como PDF o Excel

**Notas Técnicas:**
- Usar JasperReports o iReport
- Caché de reporte (24 horas)
- Scheduled generation daily

**Puntos:** 6  
**Prioridad:** MEDIA  
**Sprint:** 3

---

### US-011: Dashboard con KPIs Principales

**Como** Administrador  
**Quiero** un dashboard con los KPIs más importantes  
**Para** monitorear la salud del inventario de un vistazo

**Criterios de Aceptación:**
- [ ] Veo total de equipos activos
- [ ] Veo total de equipos en mantenimiento
- [ ] Veo total de equipos retirados este año
- [ ] Veo inversión total en equipos
- [ ] Veo ROI promedio por equipo
- [ ] Veo número de mantenimientos (este mes)
- [ ] Veo equipment con mayor costo de mant
- [ ] Los KPIs se actualizan en tiempo real (< 1 min)
- [ ] Puedo hacer drill-down en cada KPI

**Notas Técnicas:**
- React/Angular frontend
- WebSockets para actualizaciones real-time
- Chartjs o similar para gráficos

**Puntos:** 7  
**Prioridad:** MEDIA  
**Sprint:** 3

---

## 🔒 SPRINT 4: SEGURIDAD Y USUARIOS

### US-012: Autenticación con Usuario y Contraseña

**Como** Usuario del sistema  
**Quiero** autenticarme con usuario y contraseña  
**Para** acceder al sistema de forma segura

**Criterios de Aceptación:**
- [ ] Puedo ingresar usuario y contraseña
- [ ] La contraseña se valida de forma segura (BCrypt)
- [ ] Recibo error si credenciales son incorrectas
- [ ] Recibo error después de 5 intentos fallidos (lockout 15 min)
- [ ] La sesión expira después de 30 minutos de inactividad
- [ ] Se registra cada acceso exitoso (auditoría)
- [ ] Se registran intentos fallidos
- [ ] Puedo cerrar sesión explícitamente

**Notas Técnicas:**
- Spring Security + JWT tokens
- Bcrypt password encoder (10+ rounds)
- Redis para session store
- Rate limiting (5 intentos)

**Puntos:** 5  
**Prioridad:** CRÍTICA  
**Sprint:** 4

---

### US-013: Autorización por Roles

**Como** Administrador  
**Quiero** asignar roles a los usuarios  
**Para** controlar qué pueden hacer en el sistema

**Criterios de Aceptación:**
- [ ] Existen 4 roles: ADMIN, TECH, USER, VIEWER
- [ ] ADMIN tiene acceso total
- [ ] TECH puede gestionar equipos y mantenimiento
- [ ] USER puede ver solo equipos asignados a ellos
- [ ] VIEWER solo puede ver (sin editar)
- [ ] Puedo asignar múltiples roles a un usuario
- [ ] El cambio de rol es inmediato
- [ ] Se audita cada cambio de rol

**Notas Técnicas:**
- Spring Security @PreAuthorize
- Role-based access control (RBAC)
- Custom annotations

**Puntos:** 4  
**Prioridad:** CRÍTICA  
**Sprint:** 4

---

### US-014: Auditoría Completa del Sistema

**Como** Oficial de Cumplimiento  
**Quiero** ver auditoría completa de todas las acciones  
**Para** cumplir con regulaciones y detectar problemas de seguridad

**Criterios de Aceptación:**
- [ ] Veo quién hizo cada cambio
- [ ] Veo cuándo se realizó el cambio
- [ ] Veo desde qué IP se conectaron
- [ ] Veo qué datos cambiaron (old value → new value)
- [ ] Puedo filtrar por usuario, fecha, tipo de acción
- [ ] Puedo exportar auditoría a CSV
- [ ] Los logs se guardan de forma inmutable
- [ ] Retención de 1 año mínimo
- [ ] Búsqueda en < 1 segundo

**Notas Técnicas:**
- Tabla AuditLog
- ELK Stack para análisis
- Inmutable audit trails

**Puntos:** 5  
**Prioridad:** ALTA  
**Sprint:** 4

---

## 📱 SPRINT 5: INTEGRACIONES Y FUTURO

### US-015: Exportar Inventario a Excel

**Como** Administrador  
**Quiero** exportar el inventario completo a Excel  
**Para** analizarlo en Excel o compartirlo

**Criterios de Aceptación:**
- [ ] Puedo exportar todos los equipos
- [ ] Puedo filtrar antes de exportar
- [ ] El archivo Excel tiene formato profesional
- [ ] Incluye todas las columnas relevantes
- [ ] Se genera en < 5 segundos
- [ ] El archivo se descarga automáticamente
- [ ] Puedo importar cambios de Excel (futuro)

**Puntos:** 3  
**Prioridad:** MEDIA  
**Sprint:** 5

---

### US-016: API REST para Integraciones

**Como** Developer de integraciones  
**Quiero** acceder a los datos vía REST API  
**Para** integrar con otros sistemas

**Criterios de Aceptación:**
- [ ] Existen endpoints para CRUD de equipos
- [ ] Existen endpoints para mantenimiento
- [ ] La API retorna JSON
- [ ] Incluye paginación
- [ ] Soporta filtros avanzados
- [ ] Documentación en Swagger/OpenAPI
- [ ] Autenticación vía API Key
- [ ] Rate limiting (100 req/min)
- [ ] Versionado (v1, v2, etc)

**Notas Técnicas:**
- SpringDoc OpenAPI para documentación
- Swagger UI
- API versioning

**Puntos:** 6  
**Prioridad:** MEDIA  
**Sprint:** 5

---

## 📊 RESUMEN DE HISTORIAS DE USUARIO

### Sprint 1 (Core)
| US | Nombre | Puntos | Prioridad |
|----|--------|--------|-----------|
| 001 | Registrar Equipos | 5 | CRÍTICA |
| 002 | Buscar Equipo | 3 | CRÍTICA |
| 003 | Listar Equipos | 4 | CRÍTICA |
| 004 | Actualizar Datos | 3 | ALTA |
| 005 | Cambiar Estado | 4 | CRÍTICA |
| 006 | Registrar Mant. | 4 | ALTA |
| 007 | Completar Mant. | 3 | ALTA |
| **TOTAL** | | **26** | |

### Sprint 2 (IA)
| US | Nombre | Puntos | Prioridad |
|----|--------|--------|-----------|
| 008 | Análisis Predictivo | 8 | MEDIA |
| 009 | Recomendaciones | 6 | MEDIA |
| **TOTAL** | | **14** | |

### Sprint 3 (Reportes)
| US | Nombre | Puntos | Prioridad |
|----|--------|--------|-----------|
| 010 | Reporte Inventario | 6 | MEDIA |
| 011 | Dashboard KPIs | 7 | MEDIA |
| **TOTAL** | | **13** | |

### Sprint 4 (Seguridad)
| US | Nombre | Puntos | Prioridad |
|----|--------|--------|-----------|
| 012 | Autenticación | 5 | CRÍTICA |
| 013 | Autorización | 4 | CRÍTICA |
| 014 | Auditoría | 5 | ALTA |
| **TOTAL** | | **14** | |

### Sprint 5 (Integraciones)
| US | Nombre | Puntos | Prioridad |
|----|--------|--------|-----------|
| 015 | Exportar Excel | 3 | MEDIA |
| 016 | API REST | 6 | MEDIA |
| **TOTAL** | | **9** | |

**TOTAL MVP:** 76 puntos de historia

---

## 🎯 ROADMAP

```
MVP v1.0 (Actual)
├─ Sprint 1: Core CRUD + Mantenimiento (26 pts)
├─ Sprint 2: IA + Predicción (14 pts)
├─ Sprint 3: Reportes + Dashboard (13 pts)
├─ Sprint 4: Seguridad + Auditoría (14 pts)
└─ Sprint 5: Integraciones (9 pts)

v1.1 (Enhancement)
├─ Importación desde Excel
├─ Integración con ITSM
└─ Mobile app nativa

v2.0 (Major Release)
├─ GraphQL API
├─ IoT monitoring
├─ Machine Learning
└─ Multi-sede
```

---

**Documento Versión:** 1.0  
**Última Actualización:** Septiembre 2026  
**Autor:** Equipo de Desarrollo THOTH  
**Sprint Planning:** Cada 2 semanas
