# THOTH C.O.R.E - Requisitos Funcionales
## Sistema de Gestión de Equipos de Cómputo - MVP v1.0.0

---

## 📋 INTRODUCCIÓN

**Nombre del Proyecto:** THOTH C.O.R.E  
**Sigla:** C.O.R.E = Computer Operations Resources Environment  
**Versión:** 1.0.0 (MVP)  
**Fecha:** Septiembre 2026  
**Estado:** En Desarrollo

---

## 🎯 OBJETIVO GENERAL

Desarrollar un **sistema integral de gestión de equipos de cómputo** que permita a las organizaciones inventariar, monitorear, mantener y optimizar su parque tecnológico mediante inteligencia artificial y análisis predictivo.

---

## 🔧 REQUISITOS FUNCIONALES POR MÓDULO

### MÓDULO 1: GESTIÓN DE EQUIPOS

#### RF1.1 - Registrar Equipo
**Descripción:** El sistema debe permitir registrar un nuevo equipo en el inventario.

**Actores:** Administrador, Técnico de IT

**Precondiciones:**
- El usuario tiene permisos de creación
- Los datos del equipo son válidos

**Flujo Normal:**
1. Usuario accede a "Registrar Equipo"
2. Ingresa datos del equipo (nombre, categoría, S/N, marca, modelo)
3. Ingresa especificaciones (MAC, fecha compra, valor)
4. Selecciona ubicación (edificio, piso, oficina)
5. Asigna a usuario responsable
6. Sistema valida datos
7. Sistema guarda equipo con estado ACTIVE
8. Sistema retorna ID único del equipo

**Datos de Entrada:**
- Nombre (string, 1-255 caracteres, requerido)
- Categoría (enum: DESKTOP_PC, LAPTOP, SERVER, PRINTER, etc., requerido)
- Serial Number (string, único, requerido)
- Marca (string, 0-100 caracteres, opcional)
- Modelo (string, 0-100 caracteres, opcional)
- MAC Address (formato XX:XX:XX:XX:XX:XX, opcional)
- Fecha de Compra (date, no puede ser futura, requerido)
- Valor de Compra (decimal, positivo, requerido)
- Ubicación: Edificio, Piso, Oficina (strings, requeridos)
- Asignado a (string, opcional)
- Creado por (string, requerido)

**Datos de Salida:**
```json
{
  "equipmentId": "UUID",
  "name": "string",
  "category": "string",
  "status": "ACTIVE",
  "location": "string",
  "message": "Equipment registered successfully"
}
```

**Excepciones:**
- E1.1.1: Serial Number duplicado → Lanzar ValidationException
- E1.1.2: MAC Address inválido → Lanzar ValidationException
- E1.1.3: Fecha futura → Lanzar ValidationException
- E1.1.4: Datos incompletos → Lanzar ValidationException

**Prioridad:** CRÍTICA  
**Estimación:** 4 puntos

---

#### RF1.2 - Consultar Equipo
**Descripción:** El sistema debe permitir consultar un equipo específico por ID o Serial Number.

**Actores:** Administrador, Técnico IT, Usuario Final

**Flujo Normal:**
1. Usuario ingresa ID del equipo o Serial Number
2. Sistema busca el equipo
3. Sistema retorna datos completos del equipo
4. Sistema muestra historial de cambios

**Datos de Salida:**
```json
{
  "equipmentId": "UUID",
  "name": "string",
  "category": "string",
  "serialNumber": "string",
  "status": "ACTIVE|MAINTENANCE|INACTIVE|RETIRED",
  "location": "string",
  "assignedTo": "string",
  "purchaseDate": "date",
  "purchaseValue": "decimal",
  "age": "years",
  "createdAt": "datetime",
  "lastModified": "datetime"
}
```

**Excepciones:**
- E1.2.1: Equipo no encontrado → Lanzar EquipmentNotFoundException

**Prioridad:** CRÍTICA  
**Estimación:** 2 puntos

---

#### RF1.3 - Listar Equipos
**Descripción:** El sistema debe listar todos los equipos con filtros y paginación.

**Actores:** Administrador, Técnico IT

**Filtros Disponibles:**
- Por estado (ACTIVE, MAINTENANCE, INACTIVE, RETIRED)
- Por categoría
- Por ubicación
- Por responsable
- Por rango de edad

**Parámetros:**
- pageNumber (int, default: 0)
- pageSize (int, default: 20, max: 100)
- sortBy (string: name, status, purchaseDate, age)
- direction (ASC, DESC)

**Datos de Salida:**
```json
{
  "content": [
    {
      "equipmentId": "UUID",
      "name": "string",
      "category": "string",
      "status": "string",
      "location": "string"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

**Prioridad:** CRÍTICA  
**Estimación:** 3 puntos

---

#### RF1.4 - Actualizar Equipo
**Descripción:** El sistema debe permitir actualizar datos del equipo.

**Actores:** Administrador, Técnico IT

**Campos Actualizables:**
- Nombre
- Responsable asignado
- Ubicación
- Observaciones

**Campos NO Actualizables:**
- ID del equipo
- Serial Number
- Fecha de compra
- Valor de compra

**Precondiciones:**
- Equipo existe
- Usuario tiene permisos
- Equipo NO está RETIRED

**Excepciones:**
- E1.4.1: Equipo no encontrado → EquipmentNotFoundException
- E1.4.2: Intenta cambiar S/N → ValidationException
- E1.4.3: Equipo retirado → InvalidStatusTransitionException

**Prioridad:** ALTA  
**Estimación:** 3 puntos

---

#### RF1.5 - Cambiar Estado de Equipo
**Descripción:** El sistema debe permitir cambiar el estado del equipo.

**Actores:** Administrador, Técnico IT

**Transiciones de Estado Permitidas:**
```
ACTIVE ──→ MAINTENANCE
ACTIVE ──→ INACTIVE
MAINTENANCE ──→ ACTIVE
MAINTENANCE ──→ INACTIVE
INACTIVE ──→ ACTIVE
INACTIVE ──→ MAINTENANCE

(Cualquier estado) ──→ RETIRED (irreversible)
```

**Cambios de Estado NO Permitidos:**
- RETIRED → Cualquier otro estado
- No se pueden hacer saltos invalidos

**Datos de Entrada:**
- Equipment ID (UUID)
- New Status (enum)
- Reason (string, opcional)
- Modified by (string)

**Datos de Salida:**
```json
{
  "equipmentId": "UUID",
  "name": "string",
  "status": "string",
  "message": "Status changed to {status}"
}
```

**Eventos Publicados:**
- EquipmentStatusChanged (pub/sub)

**Excepciones:**
- E1.5.1: Equipo no encontrado
- E1.5.2: Transición inválida → InvalidStatusTransitionException
- E1.5.3: Equipo retirado → InvalidStatusTransitionException

**Prioridad:** CRÍTICA  
**Estimación:** 3 puntos

---

### MÓDULO 2: GESTIÓN DE MANTENIMIENTO

#### RF2.1 - Registrar Mantenimiento
**Descripción:** El sistema debe registrar un evento de mantenimiento.

**Actores:** Técnico IT, Administrador

**Tipos de Mantenimiento:**
- PREVENTIVE: Mantenimiento preventivo programado
- CORRECTIVE: Mantenimiento por falla
- EMERGENCY: Mantenimiento de emergencia

**Datos de Entrada:**
- Equipment ID (UUID)
- Maintenance Type (enum)
- Description (string)
- Severity (enum: LOW, MEDIUM, HIGH, CRITICAL)
- Scheduled Date (date)

**Flujo Normal:**
1. Técnico registra mantenimiento
2. Sistema valida equipo existe
3. Sistema crea registro de mantenimiento
4. Sistema actualiza estado a MAINTENANCE
5. Sistema publica evento

**Prioridad:** ALTA  
**Estimación:** 4 puntos

---

#### RF2.2 - Completar Mantenimiento
**Descripción:** Marcar mantenimiento como completado.

**Datos de Entrada:**
- Maintenance ID (UUID)
- Completion Date (date)
- Result (string)
- Notes (string)

**Flujo:**
1. Técnico ingresa datos de finalización
2. Sistema valida maintenance existe
3. Sistema marca como completado
4. Sistema actualiza estado equipo a ACTIVE
5. Publica evento: MaintenanceCompleted

**Prioridad:** ALTA  
**Estimación:** 2 puntos

---

### MÓDULO 3: ANÁLISIS E INTELIGENCIA ARTIFICIAL

#### RF3.1 - Análisis Predictivo de Fallos
**Descripción:** El sistema debe usar IA para predecir fallos potenciales.

**Factores Considerados:**
- Edad del equipo
- Tipo de equipo
- Historial de mantenimiento
- Horas de uso
- Parámetros de rendimiento

**Datos de Salida:**
```json
{
  "equipmentId": "UUID",
  "riskLevel": "LOW|MEDIUM|HIGH|CRITICAL",
  "predictedFailureDate": "date",
  "confidence": 0.95,
  "recommendations": ["string"],
  "suggestedAction": "MONITOR|PREVENTIVE_MAINTENANCE|IMMEDIATE_REPLACEMENT"
}
```

**Prioridad:** MEDIA  
**Estimación:** 8 puntos

---

#### RF3.2 - Recomendaciones de Reemplazo
**Descripción:** El sistema debe recomendar equipos para reemplazo.

**Criterios:**
- Antigüedad > 5 años
- Costo de mantenimiento > 40% del valor
- Soporte del fabricante vencido
- Insuficiencia de capacidad

**Prioridad:** MEDIA  
**Estimación:** 5 puntos

---

### MÓDULO 4: REPORTES Y ANALYTICS

#### RF4.1 - Reporte de Inventario
**Descripción:** Generar reporte completo del inventario.

**Datos Incluidos:**
- Total equipos por categoría
- Valor total del inventario
- Distribución por ubicación
- Distribución por estado
- Edad promedio de equipos
- Equipos próximos a ser descontinuados

**Formatos:** PDF, CSV, Excel

**Prioridad:** MEDIA  
**Estimación:** 5 puntos

---

#### RF4.2 - Reporte de Mantenimiento
**Descripción:** Reporte de eventos de mantenimiento.

**Datos:**
- Historial de mantenimientos
- Costo total de mantenimiento
- Equipos con más mantenimientos
- Tiempo promedio de reparación

**Prioridad:** MEDIA  
**Estimación:** 4 puntos

---

### MÓDULO 5: USUARIOS Y SEGURIDAD

#### RF5.1 - Autenticación
**Descripción:** Los usuarios deben autenticarse en el sistema.

**Métodos Soportados:**
- Usuario/Contraseña
- OAuth2 (Google, Microsoft)

**Prioridad:** CRÍTICA  
**Estimación:** 5 puntos

---

#### RF5.2 - Autorización por Roles
**Descripción:** Control de acceso basado en roles.

**Roles Disponibles:**
- **ADMIN:** Acceso total
- **TECH:** Gestión de equipos y mantenimiento
- **USER:** Consulta de equipos asignados
- **VIEWER:** Solo lectura

**Prioridad:** CRÍTICA  
**Estimación:** 4 puntos

---

## 📊 RESUMEN DE REQUISITOS FUNCIONALES

| RF | Nombre | Prioridad | Puntos | Estado |
|----|--------|-----------|--------|--------|
| 1.1 | Registrar Equipo | CRÍTICA | 4 | Pendiente |
| 1.2 | Consultar Equipo | CRÍTICA | 2 | Pendiente |
| 1.3 | Listar Equipos | CRÍTICA | 3 | Pendiente |
| 1.4 | Actualizar Equipo | ALTA | 3 | Pendiente |
| 1.5 | Cambiar Estado | CRÍTICA | 3 | Pendiente |
| 2.1 | Registrar Mantenimiento | ALTA | 4 | Pendiente |
| 2.2 | Completar Mantenimiento | ALTA | 2 | Pendiente |
| 3.1 | Análisis Predictivo | MEDIA | 8 | Pendiente |
| 3.2 | Recomendaciones | MEDIA | 5 | Pendiente |
| 4.1 | Reporte Inventario | MEDIA | 5 | Pendiente |
| 4.2 | Reporte Mantenimiento | MEDIA | 4 | Pendiente |
| 5.1 | Autenticación | CRÍTICA | 5 | Pendiente |
| 5.2 | Autorización | CRÍTICA | 4 | Pendiente |

**Total Puntos (MVP):** 53 puntos

---

## 🎯 REQUISITOS FUNCIONALES - VERSIÓN FUTURA (v2.0)

- Integración con sistemas ITSM
- API REST pública
- Sincronización multi-sede
- Movilidad (App móvil)
- Integración IoT para monitoreo real-time
- Análisis financiero avanzado
- Machine Learning para optimización de costos

---

**Documento Versión:** 1.0  
**Última Actualización:** Septiembre 2026  
**Autor:** Equipo de Desarrollo THOTH
