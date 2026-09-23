import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MaintenanceCategory {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class MaintenanceCategoryService {
  private apiUrl = `${environment.apiUrl}/maintenance-categories`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<MaintenanceCategory[]> { return this.http.get<MaintenanceCategory[]>(this.apiUrl); }
  listAll(): Observable<MaintenanceCategory[]> { return this.http.get<MaintenanceCategory[]>(`${this.apiUrl}/all`); }
  create(category: Partial<MaintenanceCategory>): Observable<MaintenanceCategory> { return this.http.post<MaintenanceCategory>(this.apiUrl, category); }
  update(id: string, category: Partial<MaintenanceCategory>): Observable<MaintenanceCategory> { return this.http.put<MaintenanceCategory>(`${this.apiUrl}/${id}`, category); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}
