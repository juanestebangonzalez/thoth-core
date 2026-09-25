import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/**
 * Rutas de autenticacion donde un 401 significa "credenciales incorrectas",
 * no "sesion expirada". En esas no hay que cerrar sesion ni redirigir: el
 * componente muestra su propio mensaje.
 */
const RUTAS_AUTH = [
  '/auth/login',
  '/auth/register',
  '/auth/change-password',
  '/auth/request-password-reset'
];

/**
 * DT-09: al reducir la vigencia del token de 24h a 2h, un usuario que trabaje
 * mas de dos horas seguidas se encuentra con que su token caduca en mitad de
 * la jornada. Antes de este cambio el interceptor no hacia nada con el 401:
 * la peticion fallaba, el usuario veia un error incomprensible y el token
 * caducado seguia en localStorage.
 *
 * Ahora un 401 fuera de las rutas de autenticacion cierra la sesion y lleva
 * al login con un mensaje que explica lo que paso.
 *
 * No se inyecta AuthService para evitar un ciclo de dependencias
 * (AuthService -> PermissionService -> HttpClient -> este interceptor).
 * La limpieza se hace sobre localStorage directamente, que es lo mismo que
 * hace AuthService.logout().
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  const token = localStorage.getItem('token');
  const peticion = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      const esRutaAuth = RUTAS_AUTH.some(ruta => req.url.includes(ruta));

      if (error.status === 401 && !esRutaAuth) {
        localStorage.removeItem('token');
        localStorage.removeItem('auth');
        localStorage.removeItem('permissions');

        snackBar.open(
          'Tu sesion expiro. Inicia sesion de nuevo.',
          'OK',
          { duration: 6000 }
        );

        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};
