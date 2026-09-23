import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { PermissionsDialogComponent } from './permissions-dialog.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { ResetPasswordDialogComponent } from './reset-password-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatMenuModule, MatSnackBarModule, MatDialogModule],
  template: `
    <div class="user-page">
      <div class="header">
        <button mat-icon-button routerLink="/dashboard">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>manage_accounts</mat-icon> Administracion de Usuarios</h2>
      </div>

      @if (loading()) {
        <p>Cargando usuarios...</p>
      } @else if (users().length === 0) {
        <mat-card class="empty-state">
          <mat-icon class="empty-icon">people</mat-icon>
          <h3>No hay usuarios registrados</h3>
        </mat-card>
      } @else {
        <mat-card class="table-card">
          <table mat-table [dataSource]="users()" class="full-width">
            <ng-container matColumnDef="username">
              <th mat-header-cell *matHeaderCellDef>Usuario</th>
              <td mat-cell *matCellDef="let u">
                <div class="user-cell">
                  <div class="avatar">{{ u.username.charAt(0).toUpperCase() }}</div>
                  <div>
                    <div class="username">{{ u.username }}</div>
                    <div class="email">{{ u.email }}</div>
                  </div>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Rol</th>
              <td mat-cell *matCellDef="let u">
                <span class="role-badge" [class]="u.role.toLowerCase()">{{ u.role }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Estado</th>
              <td mat-cell *matCellDef="let u">
                <span class="status-badge" [class.enabled]="u.enabled" [class.disabled]="!u.enabled">
                  <mat-icon>{{ u.enabled ? 'check_circle' : 'block' }}</mat-icon>
                  {{ u.enabled ? 'Activo' : 'Desactivado' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Creado</th>
              <td mat-cell *matCellDef="let u">
                {{ u.createdAt ? (u.createdAt | date:'dd/MM/yyyy') : '-' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Acciones</th>
              <td mat-cell *matCellDef="let u">
                <button mat-icon-button [matMenuTriggerFor]="menu" [disabled]="u.username === auth.currentUser()?.username">
                  <mat-icon>more_vert</mat-icon>
                </button>
                <mat-menu #menu="matMenu">
                  <button mat-menu-item [matMenuTriggerFor]="roleMenu">
                    <mat-icon>admin_panel_settings</mat-icon>
                    <span>Cambiar Rol</span>
                  </button>
                  <button mat-menu-item (click)="resetPassword(u)">
                    <mat-icon style="color:#F59E0B;">lock_reset</mat-icon>
                    <span>Resetear Password</span>
                  </button>
                <button mat-menu-item (click)="openPermissions(u)">
                    <mat-icon>security</mat-icon>
                    <span>Permisos</span>
                  </button>
                  <button mat-menu-item (click)="deleteUser(u)" style="color:#EF4444;">
                    <mat-icon style="color:#EF4444;">delete_forever</mat-icon>
                    <span>Eliminar Usuario</span>
                  </button>
                  <button mat-menu-item (click)="toggleEnabled(u)">
                    <mat-icon [style.color]="u.enabled ? '#EF4444' : '#10B981'">
                      {{ u.enabled ? 'block' : 'check_circle' }}
                    </mat-icon>
                    <span>{{ u.enabled ? 'Desactivar' : 'Activar' }}</span>
                  </button>
                </mat-menu>
                <mat-menu #roleMenu="matMenu">
                  <button mat-menu-item (click)="changeRole(u, 'ADMIN')">
                    <mat-icon style="color:#EF4444;">shield</mat-icon>
                    <span>ADMIN</span>
                  </button>
                  <button mat-menu-item (click)="changeRole(u, 'TECHNICIAN')">
                    <mat-icon style="color:#F59E0B;">build</mat-icon>
                    <span>TECHNICIAN</span>
                  </button>
                  <button mat-menu-item (click)="changeRole(u, 'USER')">
                    <mat-icon style="color:#3B82F6;">person</mat-icon>
                    <span>USER</span>
                  </button>
                  <button mat-menu-item (click)="changeRole(u, 'VIEWER')">
                    <mat-icon style="color:#64748B;">visibility</mat-icon>
                    <span>VIEWER</span>
                  </button>
                </mat-menu>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"></tr>
          </table>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .user-page { padding: 24px; max-width: 1400px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; }
    h2 {
      display: flex; align-items: center; gap: 8px; margin: 0;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text; font-size: 24px;
    }
    .table-card { padding: 0; overflow: hidden; }
    .full-width { width: 100%; }
    .user-cell { display: flex; align-items: center; gap: 12px; }
    .avatar {
      width: 40px; height: 40px; border-radius: 10px;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      color: white; display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 16px;
    }
    .username { font-weight: 600; color: #F1F5F9; }
    .email { font-size: 12px; color: #94A3B8; }

    .role-badge {
      padding: 4px 12px; border-radius: 12px; font-size: 11px;
      font-weight: 700; text-transform: uppercase; letter-spacing: 1px;
    }
    .role-badge.admin { background: rgba(239, 68, 68, 0.2); color: #F87171; }
    .role-badge.technician { background: rgba(245, 158, 11, 0.2); color: #FBBF24; }
    .role-badge.user { background: rgba(59, 130, 246, 0.2); color: #93C5FD; }
    .role-badge.viewer { background: rgba(100, 116, 139, 0.2); color: #CBD5E1; }

    .status-badge {
      display: inline-flex; align-items: center; gap: 4px;
      padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;
    }
    .status-badge mat-icon { font-size: 16px; width: 16px; height: 16px; overflow: hidden; }
    .status-badge.enabled { background: rgba(16, 185, 129, 0.2); color: #6EE7B7; }
    .status-badge.disabled { background: rgba(239, 68, 68, 0.2); color: #FCA5A5; }

    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #64748B; }
    .empty-state h3 { color: #E2E8F0; }

    @media (max-width: 768px) {
      .user-page { padding: 12px; }
      h2 { font-size: 18px; }
      .table-card { overflow-x: auto; -webkit-overflow-scrolling: touch; }
      .user-cell { gap: 8px; }
      .avatar { width: 32px; height: 32px; font-size: 13px; }
      .username { font-size: 13px; }
      .email { font-size: 11px; }
    }
  `]
})
export class UserListComponent implements OnInit {
  users = signal<User[]>([]);
  loading = signal(true);
  columns = ['username', 'role', 'status', 'createdAt', 'actions'];

  constructor(
    private userService: UserService,
    public auth: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadUsers(); }

  loadUsers() {
    this.loading.set(true);
    this.userService.list().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
        this.cdr.detectChanges();
      },
      error: () => {
        this.users.set([]);
        this.loading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  changeRole(user: User, newRole: string) {
    if (user.role === newRole) return;
    this.userService.changeRole(user.id, newRole).subscribe({
      next: (r) => {
        this.snackBar.open(r.message, 'OK', { duration: 3000 });
        this.loadUsers();
      },
      error: (err) => this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo cambiar el rol'), 'OK', { duration: 5000 })
    });
  }

  toggleEnabled(user: User) {
    this.userService.toggleEnabled(user.id).subscribe({
      next: (r) => {
        this.snackBar.open(r.message, 'OK', { duration: 3000 });
        this.loadUsers();
      },
      error: (err) => this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo cambiar'), 'OK', { duration: 5000 })
    });
  }

  resetPassword(user: User) {
    if (!confirm('Seguro que deseas resetear la contrasena de ' + user.username + '?')) return;
    this.userService.resetPassword(user.id).subscribe({
      next: (r) => {
        this.dialog.open(ResetPasswordDialogComponent, {
          width: '480px',
          data: { username: user.username, newPassword: r.newPassword }
        });
      },
      error: (err) => this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo resetear'), 'OK', { duration: 5000 })
    });
  }

  deleteUser(user: any) {
    if (user.username === 'admin') {
      this.snackBar.open('No se puede eliminar el usuario admin', 'OK', { duration: 3000 });
      return;
    }
    if (!confirm('ELIMINAR PERMANENTEMENTE al usuario ' + user.username + '? Esta accion no se puede deshacer.')) return;
    this.userService.deleteUser(user.id).subscribe({
      next: () => { this.snackBar.open('Usuario eliminado', 'OK', { duration: 3000 }); this.loadUsers(); },
      error: (err: any) => this.snackBar.open(err.error?.message || 'Error al eliminar', 'OK', { duration: 5000 })
    });
  }

  openPermissions(user: any) {
    this.dialog.open(PermissionsDialogComponent, {
      width: '650px',
      maxHeight: '90vh',
      data: { userId: user.id, username: user.username, role: user.role }
    });
  }
}