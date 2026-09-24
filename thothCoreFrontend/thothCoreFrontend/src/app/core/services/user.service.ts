import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, ResetPasswordResult, UpdateResult } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  list(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  changeRole(userId: string, role: string): Observable<UpdateResult> {
    return this.http.patch<UpdateResult>(`${this.apiUrl}/${userId}/role`, { role });
  }

  toggleEnabled(userId: string): Observable<UpdateResult> {
    return this.http.patch<UpdateResult>(`${this.apiUrl}/${userId}/toggle-enabled`, {});
  }

  resetPassword(userId: string): Observable<ResetPasswordResult> {
    return this.http.post<ResetPasswordResult>(`${this.apiUrl}/${userId}/reset-password`, {});
  }

  changeEmail(userId: string, email: string): Observable<UpdateResult> {
    return this.http.patch<UpdateResult>(`${this.apiUrl}/${userId}/email`, { email });
  }

  deleteUser(userId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${userId}`);
  }
}