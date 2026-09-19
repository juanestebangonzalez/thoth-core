import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Equipment, CreateEquipmentRequest, PageResponse } from '../models/equipment.model';

@Injectable({ providedIn: 'root' })
export class EquipmentService {
  private apiUrl = `${environment.apiUrl}/equipment`;

  constructor(private http: HttpClient) {}

  list(page = 0, size = 20): Observable<PageResponse<Equipment>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Equipment>>(this.apiUrl, { params });
  }

  getById(id: string): Observable<Equipment> {
    return this.http.get<Equipment>(`${this.apiUrl}/${id}`);
  }

  create(request: CreateEquipmentRequest): Observable<any> {
    return this.http.post(this.apiUrl, request);
  }

  update(id: string, request: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, request);
  }

  changeStatus(id: string, status: string, reason: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status`, { status, reason });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}