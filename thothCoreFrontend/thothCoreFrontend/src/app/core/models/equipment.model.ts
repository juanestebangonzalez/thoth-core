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
}