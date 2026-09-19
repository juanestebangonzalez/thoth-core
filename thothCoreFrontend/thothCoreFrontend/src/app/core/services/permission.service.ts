import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PermissionService {
  private apiUrl = `${environment.apiUrl}/permissions`;
  constructor(private http: HttpClient) {}

  getModules(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/modules`);
  }

  getUserPermissions(userId: string): Observable<Record<string, string[]>> {
    return this.http.get<Record<string, string[]>>(`${this.apiUrl}/${userId}`);
  }

  getMyPermissions(): Observable<Record<string, string[]>> {
    return this.http.get<Record<string, string[]>>(`${this.apiUrl}/me`);
  }

  setUserPermissions(userId: string, permissions: Record<string, string[]>): Observable<Record<string, string[]>> {
    return this.http.put<Record<string, string[]>>(`${this.apiUrl}/${userId}`, permissions);
  }

  assignDefaults(userId: string, role: string): Observable<Record<string, string[]>> {
    return this.http.post<Record<string, string[]>>(`${this.apiUrl}/${userId}/defaults?role=${role}`, {});
  }
}