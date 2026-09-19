import { Component, Inject, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { PermissionService } from '../../core/services/permission.service';

interface ModulePermission {
  module: string;
  label: string;
  icon: string;
  actions: { action: string; label: string; checked: boolean }[];
}

@Component({
  selector: 'app-permissions-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatCheckboxModule, MatSnackBarModule, MatDividerModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <div class="header-icon"><mat-icon>security</mat-icon></div>
        <div>
          <h2>Permisos de {{ data.username }}</h2>
          <p>Rol: <span class="role-badge">{{ data.role }}</span></p>
        </div>
      </div>

      @if (loading()) {
        <p class="loading-text">Cargando permisos...</p>
      } @else {
        <div class="permissions-grid">
          <!-- Header -->
          <div class="grid-header">
            <div class="module-col">Modulo</div>
            @for (act of actionLabels; track act.key) {
              <div class="action-col">{{ act.label }}</div>
            }
          </div>

          <!-- Rows -->
          @for (mod of modules(); track mod.module) {
            <div class="grid-row" [class.all-off]="mod.actions.every(a => !a.checked)">
              <div class="module-col">
                <mat-icon>{{ mod.icon }}</mat-icon>
                <span>{{ mod.label }}</span>
              </div>
              @for (act of mod.actions; track act.action) {
                <div class="action-col">
                  <mat-checkbox [(ngModel)]="act.checked" color="primary"></mat-checkbox>
                </div>
              }
            </div>
          }
        </div>

        <!-- Quick actions -->
        <div class="quick-actions">
          <button mat-stroked-button (click)="selectAll()">
            <mat-icon>check_box</mat-icon> Marcar Todos
          </button>
          <button mat-stroked-button (click)="deselectAll()">
            <mat-icon>check_box_outline_blank</mat-icon> Desmarcar Todos
          </button>
          <button mat-stroked-button color="accent" (click)="restoreDefaults()">
            <mat-icon>restore</mat-icon> Restaurar por Defecto ({{ data.role }})
          </button>
        </div>
      }

      <mat-divider></mat-divider>

      <div class="dialog-actions">
        <button mat-button (click)="cancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="save()" [disabled]="saving()">
          {{ saving() ? 'Guardando...' : 'Guardar Permisos' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { padding: 24px; color: #E2E8F0; min-width: 500px; }
    .dialog-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .header-icon { width: 52px; height: 52px; border-radius: 14px; background: linear-gradient(135deg, #3B82F6, #8B5CF6); display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 20px rgba(59,130,246,0.3); }
    .header-icon mat-icon { color: white; font-size: 26px; width: 26px; height: 26px; }
    h2 { margin: 0; color: #F1F5F9; font-size: 20px; }
    p { margin: 4px 0 0; color: #94A3B8; font-size: 14px; }
    .role-badge { padding: 2px 10px; border-radius: 8px; background: rgba(59,130,246,0.2); color: #93C5FD; font-size: 12px; font-weight: 700; }
    .loading-text { text-align: center; color: #94A3B8; padding: 40px; }

    .permissions-grid { margin-bottom: 20px; }
    .grid-header { display: grid; grid-template-columns: 200px repeat(4, 1fr); gap: 4px; padding: 10px 12px; background: rgba(59,130,246,0.1); border-radius: 10px 10px 0 0; }
    .grid-header .module-col { color: #93C5FD; font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; }
    .grid-header .action-col { text-align: center; color: #93C5FD; font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; }

    .grid-row { display: grid; grid-template-columns: 200px repeat(4, 1fr); gap: 4px; padding: 8px 12px; border-bottom: 1px solid rgba(148,163,184,0.08); transition: all 0.2s; align-items: center; }
    .grid-row:hover { background: rgba(59,130,246,0.05); }
    .grid-row.all-off { opacity: 0.5; }
    .grid-row .module-col { display: flex; align-items: center; gap: 10px; color: #E2E8F0; font-size: 14px; font-weight: 500; }
    .grid-row .module-col mat-icon { font-size: 20px; width: 20px; height: 20px; color: #60A5FA; }
    .grid-row .action-col { text-align: center; }

    .quick-actions { display: flex; gap: 10px; margin: 16px 0; flex-wrap: wrap; }
    .quick-actions button { font-size: 12px; }

    mat-divider { margin: 16px 0 !important; border-top-color: rgba(148,163,184,0.15) !important; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 12px; }

    @media (max-width: 600px) {
      .dialog-container { min-width: auto; padding: 16px; }
      .grid-header, .grid-row { grid-template-columns: 140px repeat(4, 1fr); }
      .grid-row .module-col span { font-size: 12px; }
      .grid-header .action-col { font-size: 10px; }
    }
  `]
})
export class PermissionsDialogComponent implements OnInit {
  modules = signal<ModulePermission[]>([]);
  loading = signal(true);
  saving = signal(false);

  moduleIcons: Record<string, string> = {
    EQUIPMENT: 'computer', MAINTENANCE: 'build', AI: 'psychology',
    REPORTS: 'analytics', CALENDAR: 'calendar_month', ALERTS: 'notifications',
    QR: 'qr_code', DOCUMENTS: 'folder', USERS: 'people'
  };

  actionLabels = [
    { key: 'VIEW', label: 'Ver' },
    { key: 'CREATE', label: 'Crear' },
    { key: 'EDIT', label: 'Editar' },
    { key: 'DELETE', label: 'Eliminar' }
  ];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { userId: string; username: string; role: string },
    private dialogRef: MatDialogRef<PermissionsDialogComponent>,
    private permissionService: PermissionService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadPermissions();
  }

  loadPermissions() {
    this.loading.set(true);
    this.permissionService.getModules().subscribe({
      next: (meta) => {
        this.permissionService.getUserPermissions(this.data.userId).subscribe({
          next: (perms) => {
            const mods: ModulePermission[] = (meta.modules as string[]).map(m => ({
              module: m,
              label: meta.moduleLabels[m] || m,
              icon: this.moduleIcons[m] || 'settings',
              actions: this.actionLabels.map(a => ({
                action: a.key,
                label: a.label,
                checked: perms[m]?.includes(a.key) || false
              }))
            }));
            this.modules.set(mods);
            this.loading.set(false);
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  selectAll() {
    this.modules().forEach(m => m.actions.forEach(a => a.checked = true));
    this.cdr.detectChanges();
  }

  deselectAll() {
    this.modules().forEach(m => m.actions.forEach(a => a.checked = false));
    this.cdr.detectChanges();
  }

  restoreDefaults() {
    this.saving.set(true);
    this.permissionService.assignDefaults(this.data.userId, this.data.role).subscribe({
      next: (perms) => {
        this.modules().forEach(m => {
          m.actions.forEach(a => {
            a.checked = perms[m.module]?.includes(a.action) || false;
          });
        });
        this.saving.set(false);
        this.snackBar.open('Permisos restaurados por defecto (' + this.data.role + ')', 'OK', { duration: 3000 });
        this.cdr.detectChanges();
      }
    });
  }

  save() {
    this.saving.set(true);
    const permissions: Record<string, string[]> = {};
    this.modules().forEach(m => {
      permissions[m.module] = m.actions.filter(a => a.checked).map(a => a.action);
    });

    this.permissionService.setUserPermissions(this.data.userId, permissions).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open('Permisos guardados exitosamente', 'OK', { duration: 3000 });
        this.dialogRef.close('saved');
      },
      error: () => {
        this.saving.set(false);
        this.snackBar.open('Error al guardar permisos', 'OK', { duration: 3000 });
      }
    });
  }

  cancel() { this.dialogRef.close(); }
}