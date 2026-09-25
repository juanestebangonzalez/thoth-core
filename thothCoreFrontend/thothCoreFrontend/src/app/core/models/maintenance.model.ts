export interface PartReplaced {
  partName: string;
  partSerialNumber?: string;
  reason?: string;
  purchaseDate?: string;
  ticketNumber?: string;
}

export interface MaintenanceHistory {
  maintenanceId: string;
  equipmentId: string;
  maintenanceType: string;
  performedDate: string;
  technicianName: string;
  technicianId?: string;
  reason: string;
  description?: string;
  nextScheduledDate?: string;
  partsReplaced: PartReplaced[];
  partsCount: number;
  createdBy: string;
  createdAt: string;
  signatureBase64?: string;
  signedBy?: string;
}

export interface CreateMaintenanceRequest {
  equipmentId: string;
  maintenanceType: string;
  technicianName: string;
  technicianId?: string;
  reason: string;
  description?: string;
  nextScheduledDate?: string;
  partsReplaced?: PartReplaced[];
}