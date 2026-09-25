import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { permissionGuard } from './permission.guard';
import { AuthService } from '../services/auth.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { vi } from 'vitest';

describe('permissionGuard', () => {
  let authService: { isAuthenticated: ReturnType<typeof vi.fn>; hasPermission: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = {
      isAuthenticated: vi.fn(),
      hasPermission: vi.fn(),
    };
    router = { navigate: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('should redirect to login when not authenticated', () => {
    authService.isAuthenticated.mockReturnValue(false);
    const guard = permissionGuard('EQUIPMENT', 'VIEW');

    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should allow access when user has permission', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.hasPermission.mockReturnValue(true);
    const guard = permissionGuard('EQUIPMENT', 'VIEW');

    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
    expect(authService.hasPermission).toHaveBeenCalledWith('EQUIPMENT', 'VIEW');
  });

  it('should redirect to dashboard when permission denied', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.hasPermission.mockReturnValue(false);
    const guard = permissionGuard('USERS', 'DELETE');

    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should default to VIEW action when not specified', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.hasPermission.mockReturnValue(true);
    const guard = permissionGuard('MAINTENANCE');

    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(authService.hasPermission).toHaveBeenCalledWith('MAINTENANCE', 'VIEW');
  });
});
