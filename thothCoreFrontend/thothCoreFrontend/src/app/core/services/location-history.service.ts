import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LocationHistory {
  id: string;
  equipmentId: string;
  fromBuilding: string;
  fromFloor: string;
  fromOffice: string;
  toBuilding: string;
  toFloor: string;
  toOffice: string;
  reason: string;
  performedBy: string;
  transferredAt: string;
}

export interface TransferRequest {
  toBuilding: string;
  toFloor?: string;
  toOffice?: string;
  reason: string;
}

@Injectable({ providedIn: 'root' })
export class LocationHistoryService {
  private apiUrl = `${environment.apiUrl}/location-history`;

  constructor(private http: HttpClient) {}

  getHistory(equipmentId: string): Observable<LocationHistory[]> {
    return this.http.get<LocationHistory[]>(`${this.apiUrl}/equipment/${equipmentId}`);
  }

  transfer(equipmentId: string, request: TransferRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/equipment/${equipmentId}/transfer`, request);
  }
}
