import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export function permissionGuard(module: string, action: string = 'VIEW'): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated()) { router.navigate(['/login']); return false; }
    if (auth.hasPermission(module, action)) return true;
    router.navigate(['/dashboard']);
    return false;
  };
}