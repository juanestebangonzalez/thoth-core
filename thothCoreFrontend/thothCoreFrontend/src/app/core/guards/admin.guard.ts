import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const role = authService.currentUser()?.role;
  if (role !== 'ADMIN') {
    snackBar.open('Acceso denegado: se requiere rol ADMIN', 'OK', { duration: 3000 });
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};