import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface DeviceType {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class DeviceTypeService {
  private apiUrl = `${environment.apiUrl}/device-types`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<DeviceType[]> { return this.http.get<DeviceType[]>(this.apiUrl); }
  listAll(): Observable<DeviceType[]> { return this.http.get<DeviceType[]>(`${this.apiUrl}/all`); }
  create(deviceType: Partial<DeviceType>): Observable<DeviceType> { return this.http.post<DeviceType>(this.apiUrl, deviceType); }
  update(id: string, deviceType: Partial<DeviceType>): Observable<DeviceType> { return this.http.put<DeviceType>(`${this.apiUrl}/${id}`, deviceType); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}
