import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('auth');
        localStorage.removeItem('permissions');
        router.navigate(['/login']);
        snackBar.open('Sesión expirada. Inicia sesión nuevamente.', 'OK', {
          duration: 5000,
          panelClass: ['warning-snackbar']
        });
      } else if (error.status === 403) {
        snackBar.open('No tienes permisos para realizar esta acción.', 'OK', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      } else if (error.status === 0) {
        snackBar.open('Error de conexión. Verifica tu red.', 'Reintentar', {
          duration: 6000,
          panelClass: ['error-snackbar']
        });
      }
      return throwError(() => error);
    })
  );
};
