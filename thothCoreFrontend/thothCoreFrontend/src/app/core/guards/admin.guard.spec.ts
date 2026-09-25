import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { signal } from '@angular/core';
import { vi } from 'vitest';

describe('adminGuard', () => {
  let authService: { isAuthenticated: ReturnType<typeof vi.fn>; currentUser: ReturnType<typeof signal> };
  let router: { navigate: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = {
      isAuthenticated: vi.fn(),
      currentUser: signal(null as any),
    };
    router = { navigate: vi.fn() };
    snackBar = { open: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    });
  });

  it('should redirect to login when not authenticated', () => {
    authService.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should deny access for non-admin user', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.currentUser.set({ role: 'VIEWER', username: 'viewer' } as any);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(snackBar.open).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should allow access for admin user', () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.currentUser.set({ role: 'ADMIN', username: 'admin' } as any);

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
