import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

/**
 * Pagina publica de restablecimiento de contrasena. Se llega desde el enlace
 * del correo: /reset-password?token=...
 * No requiere sesion; el interceptor no envia token ni redirige en estas rutas.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <div class="login-header">
          <h1>THOTH C.O.R.E</h1>
          <p>Restablecer contrasena</p>
        </div>

        @if (estado === 'validando') {
          <div class="center-box">
            <mat-spinner diameter="40"></mat-spinner>
            <p class="muted">Validando enlace...</p>
          </div>
        } @else if (estado === 'invalido') {
          <div class="warning-box">
            <mat-icon>link_off</mat-icon>
            <div>
              <strong>Enlace no valido</strong>
              <p>El enlace no es valido o ya vencio. Solicita uno nuevo desde "Olvide mi contrasena".</p>
            </div>
          </div>
          <button mat-raised-button color="primary" class="full-width" (click)="irALogin()">
            Ir al Inicio de Sesion
          </button>
        } @else if (estado === 'exito') {
          <div class="success-box">
            <mat-icon>check_circle</mat-icon>
            <div>
              <strong>Contrasena actualizada</strong>
              <p>{{ mensajeExito }} Seras redirigido al inicio de sesion...</p>
            </div>
          </div>
          <button mat-raised-button color="primary" class="full-width" (click)="irALogin()">
            Ir al Inicio de Sesion
          </button>
        } @else {
          <div class="form-container">
            <div class="info-box">
              <mat-icon>lock_reset</mat-icon>
              <div>
                <strong>Nueva contrasena</strong>
                <p>Minimo 10 caracteres, con al menos una letra y un numero.</p>
              </div>
            </div>
            <mat-form-field appearance="outline" class="full-width password-field">
              <mat-label>Nueva Contrasena</mat-label>
              <input matInput [(ngModel)]="newPassword" [type]="hide ? 'password' : 'text'" autocomplete="new-password">
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix type="button" (click)="hide = !hide">
                <mat-icon>{{ hide ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width password-field">
              <mat-label>Confirmar Contrasena</mat-label>
              <input matInput [(ngModel)]="confirmPassword" (keyup.enter)="enviar()" [type]="hide ? 'password' : 'text'" autocomplete="new-password">
              <mat-icon matPrefix>lock</mat-icon>
            </mat-form-field>
            @if (confirmPassword && newPassword !== confirmPassword) {
              <p class="error-text">Las contrasenas no coinciden</p>
            }
            <button mat-raised-button color="primary" class="full-width" (click)="enviar()" [disabled]="loading">
              {{ loading ? 'Guardando...' : 'Guardar Contrasena' }}
            </button>
            <button mat-button class="full-width" (click)="irALogin()">Cancelar</button>
          </div>
        }
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 20px; }
    .login-card { width: 460px; max-width: 100%; padding: 32px; border-radius: 16px; }
    .login-header { text-align: center; margin-bottom: 24px; }
    .login-header h1 {
      margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 700;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .login-header p { color: #94A3B8; margin: 4px 0 0; font-size: 13px; }
    .form-container { padding: 16px 0; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .center-box { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 24px 0; }
    .muted { color: #94A3B8; margin: 0; }
    .error-text { color: #FCA5A5; font-size: 13px; margin: -4px 0 12px; }
    .warning-box, .info-box, .success-box {
      display: flex; gap: 12px; padding: 16px; border-radius: 8px; margin-bottom: 20px;
    }
    .warning-box { background: rgba(245, 158, 11, 0.15); border-left: 4px solid #F59E0B; }
    .warning-box mat-icon { color: #F59E0B; flex-shrink: 0; }
    .warning-box strong { color: #FBBF24; display: block; margin-bottom: 4px; }
    .warning-box p { color: #FCD34D; font-size: 13px; margin: 0; line-height: 1.5; }
    .info-box { background: rgba(59, 130, 246, 0.15); border-left: 4px solid #3B82F6; }
    .info-box mat-icon { color: #60A5FA; flex-shrink: 0; }
    .info-box strong { color: #93C5FD; display: block; margin-bottom: 4px; }
    .info-box p { color: #BFDBFE; font-size: 13px; margin: 0; line-height: 1.5; }
    .success-box { background: rgba(16, 185, 129, 0.15); border-left: 4px solid #10B981; }
    .success-box mat-icon { color: #6EE7B7; flex-shrink: 0; }
    .success-box strong { color: #6EE7B7; display: block; margin-bottom: 4px; }
    .success-box p { color: #A7F3D0; font-size: 13px; margin: 0; line-height: 1.5; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  estado: 'validando' | 'invalido' | 'formulario' | 'exito' = 'validando';
  token = '';
  newPassword = '';
  confirmPassword = '';
  hide = true;
  loading = false;
  mensajeExito = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.estado = 'invalido';
      return;
    }
    this.auth.validateResetToken(this.token).subscribe({
      next: (r) => { this.estado = r?.valid ? 'formulario' : 'invalido'; this.cdr.detectChanges(); },
      error: () => { this.estado = 'invalido'; this.cdr.detectChanges(); }
    });
  }

  enviar() {
    const error = this.validarPassword(this.newPassword);
    if (error) {
      this.snackBar.open(error, 'OK', { duration: 4000 });
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.snackBar.open('Las contrasenas no coinciden', 'OK', { duration: 3000 });
      return;
    }
    this.loading = true;
    this.auth.resetPassword(this.token, this.newPassword).subscribe({
      next: (r) => {
        this.loading = false;
        this.mensajeExito = r?.message || 'Tu contrasena fue actualizada.';
        this.estado = 'exito';
        this.cdr.detectChanges();
        setTimeout(() => this.irALogin(), 4000);
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.error?.message || 'No se pudo restablecer la contrasena';
        this.snackBar.open(msg, 'OK', { duration: 6000 });
        this.cdr.detectChanges();
      }
    });
  }

  irALogin() {
    if (this.router.url.startsWith('/reset-password')) this.router.navigate(['/login']);
  }

  /** Misma politica que el login y el backend (PasswordPolicy.java). */
  private validarPassword(password: string): string | null {
    if (!password || password.length < 10) return 'La contrasena debe tener al menos 10 caracteres';
    if (password.length > 100) return 'La contrasena no puede superar los 100 caracteres';
    if (!/[a-zA-Z]/.test(password) || !/[0-9]/.test(password)) {
      return 'La contrasena debe incluir al menos una letra y al menos un numero';
    }
    return null;
  }
}
