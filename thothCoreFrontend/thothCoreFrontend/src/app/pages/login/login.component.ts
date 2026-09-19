import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { IdleService } from '../../core/services/idle.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatInputModule, MatButtonModule, MatIconModule, MatTabsModule, MatSnackBarModule],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <div class="login-header">
          <h1>THOTH C.O.R.E</h1>
          <p>Computer Operations Resources Environment</p>
        </div>

        @if (mustChangePassword) {
          <div class="change-password-section">
            <div class="warning-box">
              <mat-icon>lock</mat-icon>
              <div>
                <strong>Debes cambiar tu contrasena</strong>
                <p>Tu contrasena fue reseteada por un administrador. Por favor, establece una nueva contrasena para continuar.</p>
              </div>
            </div>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nueva Contrasena</mat-label>
              <input matInput [(ngModel)]="newPassword" [type]="hideNew ? 'password' : 'text'">
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix (click)="hideNew = !hideNew">
                <mat-icon>{{ hideNew ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
            </mat-form-field>
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirmar Nueva Contrasena</mat-label>
              <input matInput [(ngModel)]="confirmPassword" (keyup.enter)="submitChangePassword()" [type]="hideNew ? 'password' : 'text'">
              <mat-icon matPrefix>lock</mat-icon>
            </mat-form-field>
            <button mat-raised-button color="primary" class="full-width" (click)="submitChangePassword()" [disabled]="loading">
              {{ loading ? 'Actualizando...' : 'Cambiar Contrasena' }}
            </button>
            <button mat-button class="full-width" (click)="cancelChange()">Cancelar</button>
          </div>
        } @else {
          <mat-tab-group>
            <mat-tab label="Iniciar Sesion">
              <div class="form-container">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Usuario</mat-label>
                  <input matInput [(ngModel)]="loginData.username" placeholder="admin">
                  <mat-icon matPrefix>person</mat-icon>
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Contrasena</mat-label>
                  <input matInput [(ngModel)]="loginData.password" (keyup.enter)="login()" [type]="hidePassword ? 'password' : 'text'">
                  <mat-icon matPrefix>lock</mat-icon>
                  <button mat-icon-button matSuffix (click)="hidePassword = !hidePassword">
                    <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
                  </button>
                </mat-form-field>
                <button mat-raised-button color="primary" class="full-width" (click)="login()" [disabled]="loading">
                  {{ loading ? 'Ingresando...' : 'Ingresar' }}
                </button>
              </div>
            </mat-tab>
            <mat-tab label="Registrarse">
              <div class="form-container">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Usuario</mat-label>
                  <input matInput [(ngModel)]="registerData.username">
                  <mat-icon matPrefix>person</mat-icon>
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Email</mat-label>
                  <input matInput [(ngModel)]="registerData.email" type="email">
                  <mat-icon matPrefix>email</mat-icon>
                </mat-form-field>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Contrasena</mat-label>
                  <input matInput [(ngModel)]="registerData.password" (keyup.enter)="register()" type="password" minlength="6">
            <mat-hint>Minimo 6 caracteres</mat-hint>
                  <mat-icon matPrefix>lock</mat-icon>
                </mat-form-field>
                <button mat-raised-button color="accent" class="full-width" (click)="register()" [disabled]="loading">
                  {{ loading ? 'Registrando...' : 'Registrarse' }}
                </button>
              </div>
            </mat-tab>
          </mat-tab-group>
        }
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 20px; }
    .login-card { width: 460px; padding: 32px; border-radius: 16px; }
    .login-header { text-align: center; margin-bottom: 24px; }
    .login-header h1 {
      margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 700;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .login-header p { color: #94A3B8; margin: 4px 0 0; font-size: 13px; }
    .form-container, .change-password-section { padding: 16px 0; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .warning-box {
      display: flex; gap: 12px; padding: 16px;
      background: rgba(245, 158, 11, 0.15);
      border-left: 4px solid #F59E0B; border-radius: 8px;
      margin-bottom: 20px;
    }
    .warning-box mat-icon { color: #F59E0B; flex-shrink: 0; }
    .warning-box strong { color: #FBBF24; display: block; margin-bottom: 4px; }
    .warning-box p { color: #FCD34D; font-size: 13px; margin: 0; line-height: 1.5; }
  `]
})
export class LoginComponent {
  loginData = { username: '', password: '' };
  registerData = { username: '', password: '', email: '', role: 'USER' };
  hidePassword = true;
  hideNew = true;
  loading = false;

  mustChangePassword = false;
  tempUsername = '';
  tempPassword = '';
  newPassword = '';
  confirmPassword = '';

  constructor(private auth: AuthService, private router: Router, private snackBar: MatSnackBar, private idleService: IdleService) {}

  login() {
    this.loading = true;
    this.auth.login(this.loginData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.passwordChangeRequired) {
          this.tempUsername = this.loginData.username;
          this.tempPassword = this.loginData.password;
          this.mustChangePassword = true;
          this.auth.logout();
          this.snackBar.open('Debes cambiar tu contrasena', 'OK', { duration: 3000 });
        } else {
          this.idleService.start();
          this.router.navigate(['/dashboard']);
        }
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Credenciales invalidas', 'OK', { duration: 3000 });
      }
    });
  }

  register() {
    if (!this.registerData.username || !this.registerData.email || !this.registerData.password) {
      this.snackBar.open('Todos los campos son obligatorios', 'OK', { duration: 3000 });
      return;
    }
    if (this.registerData.password.length < 6) {
      this.snackBar.open('La contrasena debe tener minimo 6 caracteres', 'OK', { duration: 3000 });
      return;
    }
    this.loading = true;
    this.auth.register(this.registerData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.message && !response.token) {
          this.snackBar.open(response.message, 'OK', { duration: 5000 });
        } else {
          this.snackBar.open('Usuario registrado exitosamente', 'OK', { duration: 3000 });
          this.idleService.start();
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err: any) => {
        this.loading = false;
        const msg = err.error?.message || 'Error al registrar usuario';
        this.snackBar.open(msg, 'OK', { duration: 5000 });
      }
    });
  }

  submitChangePassword() {
    if (!this.newPassword || this.newPassword.length < 6) {
      this.snackBar.open('La nueva contrasena debe tener al menos 6 caracteres', 'OK', { duration: 3000 });
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.snackBar.open('Las contrasenas no coinciden', 'OK', { duration: 3000 });
      return;
    }
    this.loading = true;
    this.auth.changePassword({
      username: this.tempUsername,
      currentPassword: this.tempPassword,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.snackBar.open('Contrasena actualizada exitosamente', 'OK', { duration: 3000 });
        this.idleService.start();
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo cambiar'), 'OK', { duration: 5000 });
      }
    });
  }

  cancelChange() {
    this.mustChangePassword = false;
    this.newPassword = '';
    this.confirmPassword = '';
    this.loginData.password = '';
  }
}