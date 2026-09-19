import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MaintenanceAlert {
  equipmentId: string;
  name: string;
  category: string;
  serialNumber: string;
  nextMaintenanceDate: string;
  daysUntil: number;
  severity: string;
  message: string;
}

export interface HardwareAlert {
  equipmentId: string;
  name: string;
  category: string;
  serialNumber: string;
  issues: string[];
  severity: string;
}

export interface RentalAlert {
  equipmentId: string;
  name: string;
  rentalCompany: string;
  rentalEndDate: string;
  daysUntil: number;
  severity: string;
  message: string;
}

export interface AlertSummary {
  upcomingMaintenance: number;
  hardwareCritical: number;
  rentalExpiring: number;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class AlertService {
  private apiUrl = `${environment.apiUrl}/alerts`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<AlertSummary> {
    return this.http.get<AlertSummary>(`${this.apiUrl}/summary`);
  }

  getUpcomingMaintenance(): Observable<MaintenanceAlert[]> {
    return this.http.get<MaintenanceAlert[]>(`${this.apiUrl}/upcoming-maintenance`);
  }

  getHardwareCritical(): Observable<HardwareAlert[]> {
    return this.http.get<HardwareAlert[]>(`${this.apiUrl}/hardware-critical`);
  }

  getRentalExpiring(): Observable<RentalAlert[]> {
    return this.http.get<RentalAlert[]>(`${this.apiUrl}/rental-expiring`);
  }
}