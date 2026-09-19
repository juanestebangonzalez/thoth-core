import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AuditEntry {
  id: string;
  action: string;
  module: string;
  entityId?: string;
  entityName?: string;
  details?: string;
  performedBy: string;
  performedAt: string;
  ipAddress?: string;
  archivedAt?: string;
}

export interface AuditPageResponse {
  content: AuditEntry[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

export interface AuditStats {
  activeRecords: number;
  archivedRecords: number;
  totalRecords: number;
}

@Injectable({ providedIn: 'root' })
export class AuditService {
  private apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  getLog(page = 0, size = 25, module?: string, user?: string, action?: string): Observable<AuditPageResponse> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (module) params = params.set('module', module);
    if (user) params = params.set('user', user);
    if (action) params = params.set('action', action);
    return this.http.get<AuditPageResponse>(this.apiUrl, { params });
  }

  getArchived(page = 0, size = 25, module?: string, user?: string, action?: string): Observable<AuditPageResponse> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (module) params = params.set('module', module);
    if (user) params = params.set('user', user);
    if (action) params = params.set('action', action);
    return this.http.get<AuditPageResponse>(`${this.apiUrl}/archive`, { params });
  }

  archiveNow(): Observable<any> {
    return this.http.post(`${this.apiUrl}/archive`, {});
  }

  getStats(): Observable<AuditStats> {
    return this.http.get<AuditStats>(`${this.apiUrl}/stats`);
  }
}
