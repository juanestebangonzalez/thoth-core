import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CostCenter {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class CostCenterService {
  private apiUrl = `${environment.apiUrl}/cost-centers`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<CostCenter[]> { return this.http.get<CostCenter[]>(this.apiUrl); }
  listAll(): Observable<CostCenter[]> { return this.http.get<CostCenter[]>(`${this.apiUrl}/all`); }
  create(cc: Partial<CostCenter>): Observable<CostCenter> { return this.http.post<CostCenter>(this.apiUrl, cc); }
  update(id: string, cc: Partial<CostCenter>): Observable<CostCenter> { return this.http.put<CostCenter>(`${this.apiUrl}/${id}`, cc); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}
