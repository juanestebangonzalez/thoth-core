import { Pipe, PipeTransform } from '@angular/core';

/**
 * Traduce codigos internos (en ingles o en mayusculas) a etiquetas en espanol
 * para mostrarlas en pantalla y en exportaciones. Si no hay traduccion,
 * devuelve el valor tal cual.
 *
 * Uso: {{ equipo.status | etiqueta:'estado' }}
 */
export type TipoEtiqueta = 'estado' | 'propiedad' | 'accion' | 'modulo' | 'mantenimiento' | 'so';

export const ETIQUETAS: Record<TipoEtiqueta, Record<string, string>> = {
  estado: {
    ACTIVE: 'Activo',
    MAINTENANCE: 'En mantenimiento',
    INACTIVE: 'Inactivo / En bodega',
    RETIRED: 'Retirado'
  },
  propiedad: {
    OWNED: 'Propio',
    RENTED: 'Alquilado'
  },
  accion: {
    CREATE: 'Crear',
    UPDATE: 'Actualizar',
    DELETE: 'Eliminar',
    TRANSFER: 'Traslado',
    IMPORT: 'Importacion',
    LOGIN: 'Inicio de sesion',
    LOGOUT: 'Cierre de sesion',
    UPLOAD: 'Carga de documento',
    CHANGE_STATUS: 'Cambio de estado',
    CHANGE_ROLE: 'Cambio de rol',
    CHANGE_EMAIL: 'Cambio de correo',
    RESET_PASSWORD: 'Restablecer contrasena',
    PASSWORD_RESET: 'Contrasena restablecida',
    PASSWORD_RESET_REQUEST: 'Solicitud de restablecimiento',
    UPDATE_PERMISSIONS: 'Cambio de permisos',
    TOGGLE_ENABLED: 'Activar/desactivar usuario',
    DELETE_USER: 'Eliminar usuario',
    SCHEDULED_JOB: 'Tarea programada',
    SCHEDULED_JOB_ERROR: 'Error en tarea programada'
  },
  modulo: {
    EQUIPMENT: 'Equipos',
    MAINTENANCE: 'Mantenimiento',
    SEDE: 'Sedes',
    AREA: 'Areas',
    COST_CENTER: 'Centros de costo',
    DEVICE_TYPE: 'Tipos de dispositivo',
    MAINTENANCE_CATEGORY: 'Tipos de mantenimiento',
    DOCUMENT: 'Documentos',
    USERS: 'Usuarios',
    PERMISSIONS: 'Permisos',
    AUTH: 'Autenticacion',
    LOCATION: 'Ubicacion',
    CATALOGS: 'Catalogos',
    IMPORT: 'Importacion',
    AUDIT: 'Auditoria',
    RENTALS: 'Alquilados'
  },
  /** Tipos de mantenimiento: solo se traducen los codigos viejos en ingles. */
  mantenimiento: {
    PREVENTIVE: 'PREVENTIVO',
    CORRECTIVE: 'CORRECTIVO'
  },
  so: {
    WINDOWS: 'Windows',
    LINUX: 'Linux',
    MACOS: 'macOS',
    CHROMEOS: 'ChromeOS',
    ANDROID: 'Android',
    IOS: 'iOS',
    OTRO: 'Otro',
    'N/A': 'N/A'
  }
};

/** Version funcional del pipe, para usar en TypeScript (exportaciones, etc). */
export function etiqueta(valor: string | null | undefined, tipo: TipoEtiqueta): string {
  if (valor === null || valor === undefined || valor === '') return valor ?? '';
  const mapa = ETIQUETAS[tipo];
  return mapa?.[valor] ?? mapa?.[String(valor).toUpperCase()] ?? String(valor);
}

/** Clase CSS para un tipo de mantenimiento: preventive, corrective u other. */
export function claseTipoMantenimiento(tipo: string | null | undefined): string {
  const t = (tipo || '').toUpperCase();
  if (t === 'PREVENTIVE' || t === 'PREVENTIVO') return 'preventive';
  if (t === 'CORRECTIVE' || t === 'CORRECTIVO') return 'corrective';
  return 'other';
}

@Pipe({ name: 'etiqueta', standalone: true })
export class EtiquetaPipe implements PipeTransform {
  transform(valor: string | null | undefined, tipo: TipoEtiqueta): string {
    return etiqueta(valor, tipo);
  }
}

/**
 * Normaliza el estado de un equipo al codigo del backend
 * (ACTIVE | MAINTENANCE | INACTIVE | RETIRED). Acepta tambien valores viejos
 * en espanol o en minusculas. Devuelve '' si no se reconoce.
 */
export function codigoEstado(status: string | null | undefined): string {
  const s = (status || '').trim().toUpperCase();
  if (s === 'ACTIVE' || s === 'ACTIVO') return 'ACTIVE';
  if (s === 'MAINTENANCE' || s.includes('MANTENIMIENTO')) return 'MAINTENANCE';
  if (s === 'INACTIVE' || s === 'INACTIVO') return 'INACTIVE';
  if (s === 'RETIRED' || s === 'RETIRADO') return 'RETIRED';
  return s;
}
