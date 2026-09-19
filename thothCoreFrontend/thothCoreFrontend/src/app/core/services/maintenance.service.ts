import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MaintenanceHistory, CreateMaintenanceRequest } from '../models/maintenance.model';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private apiUrl = `${environment.apiUrl}/maintenance-history`;

  constructor(private http: HttpClient) {}

  create(request: CreateMaintenanceRequest): Observable<MaintenanceHistory> {
    return this.http.post<MaintenanceHistory>(this.apiUrl, request);
  }

  getByEquipment(equipmentId: string): Observable<MaintenanceHistory[]> {
    return this.http.get<MaintenanceHistory[]>(`${this.apiUrl}/equipment/${equipmentId}`);
  }

  countByEquipment(equipmentId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/equipment/${equipmentId}/count`);
  }

  getById(maintenanceId: string): Observable<MaintenanceHistory> {
    return this.http.get<MaintenanceHistory>(`${this.apiUrl}/${maintenanceId}`);
  }
}