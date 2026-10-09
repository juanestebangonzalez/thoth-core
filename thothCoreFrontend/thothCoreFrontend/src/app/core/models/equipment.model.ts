export interface Equipment {
  equipmentId: string;
  name: string;
  category: string;
  serialNumber: string;
  inventoryNumber?: string;
  macAddress: string;
  macAddress2?: string;
  brand: string;
  model: string;
  status: string;
  purchaseDate: string;
  purchaseValue: number;
  location: Location;
  assignedTo: string;
  createdBy: string;
  ownershipType?: string;
  costCenter?: string;
  rentalInfo?: RentalInfo;
  hardware?: Hardware;
  nextMaintenanceDate?: string;
  /** WINDOWS | LINUX | MACOS | CHROMEOS | ANDROID | IOS | OTRO | N/A */
  operatingSystem?: string;
  osVersion?: string;
  /** WINDOWS 10 | WINDOWS 11 (solo si operatingSystem = WINDOWS) */
  osEdition?: string;
  /** OEM | RETAIL | VOLUMEN */
  osLicenseType?: string;
  responsiblePosition?: string;
  responsibleDocument?: string;
  responsiblePhone?: string;
  responsibleEmail?: string;
  ipAddress?: string;
  /** DHCP | FIJA */
  ipAssignment?: string;
  associatedEquipmentId?: string;
  associatedEquipmentName?: string;
  associatedEquipmentInventory?: string;
  usefulLife?: UsefulLife;
  /** ALTA | MEDIA | BAJA */
  criticality?: string;
  createdAt?: string;
}

export interface UsefulLife {
  years?: number;
  ageYears?: number;
  consumedPercent?: number;
  remainingYears?: number;
  estimated?: boolean;
}

/** Monitor asociado a un PC (GET /equipment/{id}/monitors). */
export interface MonitorSummary {
  equipmentId: string;
  name: string;
  inventoryNumber?: string;
  brand?: string;
  model?: string;
  serialNumber?: string;
  status?: string;
  ownershipType?: string;
  rentalCompany?: string;
  monthlyValue?: number;
}

/** Periferico de un equipo (GET /equipment/{id}/peripherals). */
export interface Peripheral {
  id: string;
  type: string;
  brand?: string;
  createdAt?: string;
  createdBy?: string;
}

/** Respuesta de GET /equipment/{id}/hoja-vida. */
export interface HojaVida {
  generatedAt: string;
  equipment: Equipment;
  peripherals: Peripheral[];
  monitors: MonitorSummary[];
  maintenances: {
    performedDate?: string; maintenanceType?: string; reason?: string; description?: string;
    technicianName?: string; signedBy?: string;
    parts?: { partName?: string; partSerialNumber?: string; reason?: string }[];
  }[];
  transfers: {
    date?: string; fromBuilding?: string; fromOffice?: string; toBuilding?: string; toOffice?: string;
    reason?: string; performedBy?: string;
  }[];
  documents: { fileName?: string; documentType?: string; uploadedAt?: string; uploadedBy?: string }[];
  baja: null | { date?: string; reason?: string };
  software?: SoftwareLicencia[];
}

/** Fila de software y licenciamiento de la hoja de vida. */
export interface SoftwareLicencia {
  software?: string; version?: string; licenseType?: string; licenseKey?: string; expiration?: string;
}

/** True si la categoria corresponde a un monitor. */
export function esMonitor(category: string | null | undefined): boolean {
  return (category || '').toUpperCase().includes('MONITOR');
}

/** Texto de vida util: "X anos · consumida Y% · restan Z anos (estimada)". */
export function textoVidaUtil(u: UsefulLife | null | undefined): string {
  if (!u || u.years == null) return '-';
  const n = (v: number | undefined) => v == null ? '-' : String(Math.round(Number(v) * 10) / 10);
  return `${n(u.years)} anos · consumida ${n(u.consumedPercent)}% · restan ${n(u.remainingYears)} anos` + (u.estimated ? ' (estimada)' : '');
}

/** Valores de sistema operativo aceptados por el backend. */
export const SISTEMAS_OPERATIVOS: { value: string; label: string }[] = [
  { value: 'WINDOWS', label: 'Windows' },
  { value: 'LINUX', label: 'Linux' },
  { value: 'MACOS', label: 'macOS' },
  { value: 'CHROMEOS', label: 'ChromeOS' },
  { value: 'ANDROID', label: 'Android' },
  { value: 'IOS', label: 'iOS' },
  { value: 'OTRO', label: 'Otro' },
  { value: 'N/A', label: 'N/A' }
];

/** Ediciones de Windows aceptadas por el backend (osEdition). */
export const OS_EDITIONS: { value: string; label: string }[] = [
  { value: 'WINDOWS 10', label: 'Windows 10' },
  { value: 'WINDOWS 11', label: 'Windows 11' }
];

/** Versiones de Windows aceptadas por el backend (osVersion cuando operatingSystem = WINDOWS). */
export const OS_VERSIONES_WINDOWS: string[] = ['26H2', '26H1', '25H2', '24H2', '23H2'];

/** Tipos de licencia del sistema operativo (osLicenseType). */
export const OS_LICENSE_TYPES: { value: string; label: string }[] = [
  { value: 'OEM', label: 'OEM' },
  { value: 'RETAIL', label: 'Retail' },
  { value: 'VOLUMEN', label: 'Volumen' }
];

/** Etiqueta legible de un valor de osEdition / osLicenseType. */
export function etiquetaSoftware(v: string | null | undefined): string {
  if (!v) return '';
  const all = [...OS_EDITIONS, ...OS_LICENSE_TYPES];
  return all.find(o => o.value === v)?.label || v;
}

export interface Location {
  building: string;
  floor: string;
  office: string;
  description?: string;
  fullAddress?: string;
}

export interface RentalInfo {
  rentalCompany?: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
  startDate?: string;
  endDate?: string;
  contractNumber?: string;
  monthlyValue?: number;
  contractFileUrl?: string;
  notes?: string;
  isExpired?: boolean;
  isExpiringSoon?: boolean;
  daysUntilExpiry?: number;
}

export interface Hardware {
  processor?: string;
  ramSizeGb?: number;
  ramType?: string;
  diskType?: string;
  diskSizeGb?: number;
  diskHealthPercent?: number;
  diskTemperatureCelsius?: number;
  diskHealthStatus?: string;
  diskTemperatureStatus?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateEquipmentRequest {
  name: string;
  category: string;
  serialNumber?: string;
  inventoryNumber?: string;
  brand?: string;
  model?: string;
  macAddress?: string;
  macAddress2?: string;
  location?: Location;
  purchaseDate?: string;
  purchaseValue?: number;
  assignedTo?: string;
  ownershipType?: string;
  costCenter?: string;
  rentalInfo?: RentalInfo;
  hardware?: Hardware;
  nextMaintenanceDate?: string;
  operatingSystem?: string;
  osVersion?: string;
  /** WINDOWS 10 | WINDOWS 11 (solo si operatingSystem = WINDOWS) */
  osEdition?: string;
  /** OEM | RETAIL | VOLUMEN */
  osLicenseType?: string;
  responsiblePosition?: string;
  responsibleDocument?: string;
  responsiblePhone?: string;
  responsibleEmail?: string;
  ipAddress?: string;
  ipAssignment?: string;
  associatedEquipmentId?: string;
}