import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Sede {
  id: string;
  name: string;
  address?: string;
  phone?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class SedeService {
  private apiUrl = `${environment.apiUrl}/sedes`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<Sede[]> { return this.http.get<Sede[]>(this.apiUrl); }
  listAll(): Observable<Sede[]> { return this.http.get<Sede[]>(`${this.apiUrl}/all`); }
  create(sede: Partial<Sede>): Observable<Sede> { return this.http.post<Sede>(this.apiUrl, sede); }
  update(id: string, sede: Partial<Sede>): Observable<Sede> { return this.http.put<Sede>(`${this.apiUrl}/${id}`, sede); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}