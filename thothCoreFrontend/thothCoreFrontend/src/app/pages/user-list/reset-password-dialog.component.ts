import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-reset-password-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  template: `
    <div class="dialog-container">
      <div class="header">
        <div class="icon-wrapper">
          <mat-icon>lock_reset</mat-icon>
        </div>
        <div>
          <h2>Contrasena Reseteada</h2>
          <p>Usuario: <strong>{{ data.username }}</strong></p>
        </div>
      </div>

      <div class="password-box">
        <div class="password-label">Nueva contrasena temporal:</div>
        <div class="password-value">{{ data.newPassword }}</div>
        <button mat-icon-button (click)="copyPassword()" class="copy-btn">
          <mat-icon>content_copy</mat-icon>
        </button>
      </div>

      <div class="warning">
        <mat-icon>warning</mat-icon>
        <div>
          <strong>IMPORTANTE:</strong>
          Entrega esta contrasena al usuario de forma segura.
          Debe cambiarla en su proximo inicio de sesion.
          No se mostrara de nuevo.
        </div>
      </div>

      <div class="actions">
        <button mat-raised-button color="primary" (click)="close()">Entendido</button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { padding: 24px; color: #E2E8F0; }
    .header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .icon-wrapper {
      width: 56px; height: 56px; border-radius: 14px;
      background: linear-gradient(135deg, #F59E0B, #D97706);
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 8px 20px rgba(245, 158, 11, 0.3);
    }
    .icon-wrapper mat-icon { color: white; font-size: 28px; width: 28px; height: 28px; }
    h2 { margin: 0; color: #F1F5F9; font-size: 20px; }
    p { margin: 4px 0 0; color: #94A3B8; font-size: 14px; }

    .password-box {
      position: relative; padding: 24px; margin-bottom: 20px;
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(139, 92, 246, 0.15));
      border: 1px solid rgba(96, 165, 250, 0.3); border-radius: 12px;
    }
    .password-label { font-size: 12px; color: #94A3B8; text-transform: uppercase; margin-bottom: 8px; letter-spacing: 1px; }
    .password-value {
      font-family: 'Courier New', monospace; font-size: 22px;
      font-weight: 700; color: #60A5FA; letter-spacing: 3px;
      user-select: all; word-break: break-all;
    }
    .copy-btn { position: absolute; top: 16px; right: 12px; }

    .warning {
      display: flex; gap: 12px; padding: 16px;
      background: rgba(239, 68, 68, 0.1);
      border-left: 4px solid #EF4444; border-radius: 8px;
      margin-bottom: 20px;
    }
    .warning mat-icon { color: #EF4444; flex-shrink: 0; }
    .warning div { font-size: 13px; line-height: 1.5; color: #FCA5A5; }
    .warning strong { color: #F87171; }

    .actions { display: flex; justify-content: flex-end; }
  `]
})
export class ResetPasswordDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { username: string, newPassword: string },
    private dialogRef: MatDialogRef<ResetPasswordDialogComponent>,
    private snackBar: MatSnackBar
  ) {}

  copyPassword() {
    navigator.clipboard.writeText(this.data.newPassword).then(() => {
      this.snackBar.open('Contrasena copiada al portapapeles', 'OK', { duration: 2000 });
    });
  }

  close() { this.dialogRef.close(); }
}