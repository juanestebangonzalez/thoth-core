import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Area {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class AreaService {
  private apiUrl = `${environment.apiUrl}/areas`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<Area[]> { return this.http.get<Area[]>(this.apiUrl); }
  listAll(): Observable<Area[]> { return this.http.get<Area[]>(`${this.apiUrl}/all`); }
  create(area: Partial<Area>): Observable<Area> { return this.http.post<Area>(this.apiUrl, area); }
  update(id: string, area: Partial<Area>): Observable<Area> { return this.http.put<Area>(`${this.apiUrl}/${id}`, area); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}
