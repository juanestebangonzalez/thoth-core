import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, ChangePasswordRequest } from '../models/auth.model';
import { PermissionService } from './permission.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  currentUser = signal<AuthResponse | null>(null);
  permissions = signal<Record<string, string[]>>({});

  constructor(private http: HttpClient, private permissionService: PermissionService) {
    try {
      const token = localStorage.getItem('token');
      const auth = localStorage.getItem('auth');
      if (token && auth) {
        const parsed = JSON.parse(auth);
        // Check JWT expiration
        if (this.isTokenExpired(token)) {
          this.logout();
        } else {
          this.currentUser.set(parsed);
          this.loadPermissions(parsed.userId);
        }
      }
    } catch {
      this.logout();
    }
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('auth', JSON.stringify(response));
          localStorage.setItem('token', response.token);
          this.currentUser.set(response);
          this.loadPermissions(response.userId);
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('auth', JSON.stringify(response));
          localStorage.setItem('token', response.token);
          this.currentUser.set(response);
          this.loadPermissions(response.userId);
        }
      })
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/change-password`, request).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('auth', JSON.stringify(response));
          localStorage.setItem('token', response.token);
          this.currentUser.set(response);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('auth');
    localStorage.removeItem('permissions');
    this.currentUser.set(null);
    this.permissions.set({});
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    if (!token) return false;
    return !this.isTokenExpired(token);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expMs = payload.exp * 1000;
      return Date.now() >= expMs;
    } catch {
      return true;
    }
  }

  loadPermissions(userId?: string) {
    this.permissionService.getMyPermissions().subscribe({
      next: (perms) => {
        this.permissions.set(perms);
        localStorage.setItem('permissions', JSON.stringify(perms));
      },
      error: () => {
        const cached = localStorage.getItem('permissions');
        if (cached) this.permissions.set(JSON.parse(cached));
      }
    });
  }

  hasPermission(module: string, action: string): boolean {
    const user = this.currentUser();
    if (user?.role === 'ADMIN') return true;
    const perms = this.permissions();
    return perms[module]?.includes(action) || false;
  }

  canView(module: string): boolean { return this.hasPermission(module, 'VIEW'); }
  canCreate(module: string): boolean { return this.hasPermission(module, 'CREATE'); }
  canEdit(module: string): boolean { return this.hasPermission(module, 'EDIT'); }
  canDelete(module: string): boolean { return this.hasPermission(module, 'DELETE'); }
}