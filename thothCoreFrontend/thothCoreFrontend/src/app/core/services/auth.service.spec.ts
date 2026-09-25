import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { PermissionService } from './permission.service';
import { of } from 'rxjs';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockPermissionService = {
    getMyPermissions: () => of({})
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PermissionService, useValue: mockPermissionService },
        AuthService,
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should not be authenticated initially', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('login should store token and user', () => {
    const response = {
      userId: '1',
      token: createFakeJwt(Date.now() / 1000 + 3600),
      username: 'admin',
      role: 'ADMIN',
      message: 'OK',
      passwordChangeRequired: false,
    };

    service.login({ username: 'admin', password: 'pass' }).subscribe(res => {
      expect(res.username).toBe('admin');
    });

    const req = httpMock.expectOne(r => r.url.includes('/auth/login'));
    req.flush(response);

    expect(service.currentUser()?.username).toBe('admin');
    expect(localStorage.getItem('token')).toBe(response.token);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('logout should clear state and storage', () => {
    localStorage.setItem('token', 'test');
    localStorage.setItem('auth', '{}');
    localStorage.setItem('permissions', '{}');

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('auth')).toBeNull();
    expect(localStorage.getItem('permissions')).toBeNull();
  });

  it('isAuthenticated returns false for expired token', () => {
    const expiredToken = createFakeJwt(Date.now() / 1000 - 100);
    localStorage.setItem('token', expiredToken);
    expect(service.isAuthenticated()).toBe(false);
  });

  it('isAuthenticated returns true for valid token', () => {
    const validToken = createFakeJwt(Date.now() / 1000 + 3600);
    localStorage.setItem('token', validToken);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('hasPermission returns true for ADMIN regardless of module', () => {
    service.currentUser.set({
      userId: '1', token: '', username: 'admin',
      role: 'ADMIN', message: '', passwordChangeRequired: false,
    });
    expect(service.hasPermission('EQUIPMENT', 'DELETE')).toBe(true);
  });

  it('hasPermission checks permissions for non-admin', () => {
    service.currentUser.set({
      userId: '2', token: '', username: 'viewer',
      role: 'VIEWER', message: '', passwordChangeRequired: false,
    });
    service.permissions.set({ EQUIPMENT: ['VIEW'], MAINTENANCE: ['VIEW', 'CREATE'] });

    expect(service.hasPermission('EQUIPMENT', 'VIEW')).toBe(true);
    expect(service.hasPermission('EQUIPMENT', 'DELETE')).toBe(false);
    expect(service.hasPermission('MAINTENANCE', 'CREATE')).toBe(true);
    expect(service.hasPermission('USERS', 'VIEW')).toBe(false);
  });

  it('canView/canCreate/canEdit/canDelete delegate to hasPermission', () => {
    service.currentUser.set({
      userId: '3', token: '', username: 'tech',
      role: 'TECHNICIAN', message: '', passwordChangeRequired: false,
    });
    service.permissions.set({ EQUIPMENT: ['VIEW', 'CREATE', 'EDIT'] });

    expect(service.canView('EQUIPMENT')).toBe(true);
    expect(service.canCreate('EQUIPMENT')).toBe(true);
    expect(service.canEdit('EQUIPMENT')).toBe(true);
    expect(service.canDelete('EQUIPMENT')).toBe(false);
  });
});

function createFakeJwt(expSeconds: number): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'user', exp: expSeconds }));
  return `${header}.${payload}.fake-signature`;
}
