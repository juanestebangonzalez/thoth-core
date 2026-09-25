export interface Equipment {
  equipmentId: string;
  name: string;
  category: string;
  serialNumber: string;
  inventoryNumber?: string;
  macAddress: string;
  brand: string;
  model: string;
  status: string;
  purchaseDate: string;
  purchaseValue: number;
  location: Location;
  assignedTo: string;
  createdBy: string;
  ownershipType?: string;
  rentalInfo?: RentalInfo;
  hardware?: Hardware;
  nextMaintenanceDate?: string;
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
  location: Location;
  purchaseDate?: string;
  purchaseValue?: number;
  assignedTo?: string;
  ownershipType?: string;
  rentalInfo?: RentalInfo;
  hardware?: Hardware;
  nextMaintenanceDate?: string;
}