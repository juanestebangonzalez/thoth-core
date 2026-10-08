import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PeripheralType {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class PeripheralTypeService {
  private apiUrl = `${environment.apiUrl}/peripheral-types`;
  constructor(private http: HttpClient) {}

  listActive(): Observable<PeripheralType[]> { return this.http.get<PeripheralType[]>(this.apiUrl); }
  listAll(): Observable<PeripheralType[]> { return this.http.get<PeripheralType[]>(`${this.apiUrl}/all`); }
  create(peripheralType: Partial<PeripheralType>): Observable<PeripheralType> { return this.http.post<PeripheralType>(this.apiUrl, peripheralType); }
  update(id: string, peripheralType: Partial<PeripheralType>): Observable<PeripheralType> { return this.http.put<PeripheralType>(`${this.apiUrl}/${id}`, peripheralType); }
  delete(id: string): Observable<any> { return this.http.delete(`${this.apiUrl}/${id}`); }
}
