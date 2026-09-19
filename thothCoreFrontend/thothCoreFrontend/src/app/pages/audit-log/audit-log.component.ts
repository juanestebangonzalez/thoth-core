import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuditService, AuditEntry, AuditStats } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, MatChipsModule, MatSnackBarModule, MatButtonToggleModule, MatTooltipModule],
  template: `
    <div class="audit-page">
      <div class="header">
        <button mat-icon-button routerLink="/dashboard">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>history</mat-icon> Log de Auditoria</h2>
        <div class="header-spacer"></div>
        @if (stats()) {
          <div class="stats-chips">
            <span class="stat-chip active">
              <mat-icon>article</mat-icon> {{ stats()!.activeRecords }} activos
            </span>
            <span class="stat-chip archived">
              <mat-icon>archive</mat-icon> {{ stats()!.archivedRecords }} archivados
            </span>
          </div>
        }
        @if (auth.currentUser()?.role === 'ADMIN') {
          <button mat-stroked-button (click)="archiveNow()" class="archive-btn" matTooltip="Archivar registros con mas de 6 meses">
            <mat-icon>archive</mat-icon> Archivar
          </button>
        }
      </div>

      <div class="view-toggle">
        <mat-button-toggle-group [(ngModel)]="viewMode" (change)="onViewChange()">
          <mat-button-toggle value="active">
            <mat-icon>article</mat-icon> Registros Activos
          </mat-button-toggle>
          <mat-button-toggle value="archived">
            <mat-icon>archive</mat-icon> Registros Archivados
          </mat-button-toggle>
        </mat-button-toggle-group>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Modulo</mat-label>
          <mat-select [(ngModel)]="moduleFilter" (selectionChange)="loadLog()">
            <mat-option value="">Todos</mat-option>
            <mat-option value="EQUIPMENT">Equipos</mat-option>
            <mat-option value="MAINTENANCE">Mantenimiento</mat-option>
            <mat-option value="DOCUMENT">Documentos</mat-option>
            <mat-option value="USERS">Usuarios</mat-option>
            <mat-option value="PERMISSIONS">Permisos</mat-option>
            <mat-option value="SEDE">Sedes</mat-option>
            <mat-option value="LOCATION">Ubicaciones</mat-option>
            <mat-option value="AUTH">Autenticacion</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Accion</mat-label>
          <mat-select [(ngModel)]="actionFilter" (selectionChange)="loadLog()">
            <mat-option value="">Todas</mat-option>
            <mat-option value="CREATE">Crear</mat-option>
            <mat-option value="UPDATE">Actualizar</mat-option>
            <mat-option value="DELETE">Eliminar</mat-option>
            <mat-option value="UPLOAD">Subir Documento</mat-option>
            <mat-option value="CHANGE_STATUS">Cambiar Estado</mat-option>
            <mat-option value="CHANGE_ROLE">Cambiar Rol</mat-option>
            <mat-option value="TOGGLE_ENABLED">Activar/Desactivar</mat-option>
            <mat-option value="UPDATE_PERMISSIONS">Cambiar Permisos</mat-option>
            <mat-option value="DELETE_USER">Eliminar Usuario</mat-option>
            <mat-option value="TRANSFER">Traslado</mat-option>
            <mat-option value="LOGIN">Login</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Usuario</mat-label>
          <input matInput [(ngModel)]="userFilter" (keyup.enter)="loadLog()" placeholder="Nombre de usuario">
        </mat-form-field>
        <button mat-stroked-button (click)="clearFilters()" class="clear-btn">
          <mat-icon>clear</mat-icon> Limpiar
        </button>
      </div>

      @if (loading()) {
        <p>Cargando registros...</p>
      } @else if (entries().length === 0) {
        <mat-card class="empty-state">
          <mat-icon class="empty-icon">history</mat-icon>
          <h3>No hay registros de auditoria {{ viewMode === 'archived' ? 'archivados' : '' }}</h3>
        </mat-card>
      } @else {
        <mat-card class="table-card">
          <table mat-table [dataSource]="entries()" class="full-width">
            <ng-container matColumnDef="performedAt">
              <th mat-header-cell *matHeaderCellDef>Fecha</th>
              <td mat-cell *matCellDef="let e">
                <span class="date-cell">{{ e.performedAt | date:'dd/MM/yyyy HH:mm' }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="performedBy">
              <th mat-header-cell *matHeaderCellDef>Usuario</th>
              <td mat-cell *matCellDef="let e">
                <div class="user-cell">
                  <div class="mini-avatar">{{ e.performedBy?.charAt(0)?.toUpperCase() }}</div>
                  {{ e.performedBy }}
                </div>
              </td>
            </ng-container>
            <ng-container matColumnDef="action">
              <th mat-header-cell *matHeaderCellDef>Accion</th>
              <td mat-cell *matCellDef="let e">
                <span class="action-badge" [class]="getActionClass(e.action)">
                  <mat-icon class="action-icon">{{ getActionIcon(e.action) }}</mat-icon>
                  {{ e.action }}
                </span>
              </td>
            </ng-container>
            <ng-container matColumnDef="module">
              <th mat-header-cell *matHeaderCellDef>Modulo</th>
              <td mat-cell *matCellDef="let e">
                <span class="module-badge">{{ e.module }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="details">
              <th mat-header-cell *matHeaderCellDef>Detalles</th>
              <td mat-cell *matCellDef="let e">
                <span class="details-text">{{ e.entityName ? e.entityName + ' - ' : '' }}{{ e.details || '-' }}</span>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;"></tr>
          </table>
          <mat-paginator
            [length]="totalElements()"
            [pageSize]="25"
            [pageSizeOptions]="[10, 25, 50]"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .audit-page { padding: 24px; max-width: 1400px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
    .header-spacer { flex: 1; }
    h2 {
      display: flex; align-items: center; gap: 8px; margin: 0;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text; font-size: 24px;
    }
    .stats-chips { display: flex; gap: 8px; }
    .stat-chip {
      display: inline-flex; align-items: center; gap: 4px;
      padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600;
    }
    .stat-chip mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .stat-chip.active { background: rgba(59,130,246,0.15); color: #93C5FD; }
    .stat-chip.archived { background: rgba(245,158,11,0.15); color: #FBBF24; }
    .archive-btn { color: #FBBF24 !important; border-color: rgba(245,158,11,0.3) !important; }
    .view-toggle { margin-bottom: 16px; }
    .filters { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
    .filter-field { flex: 1; min-width: 150px; }
    .clear-btn { color: #94A3B8 !important; }
    .table-card { padding: 0; overflow: hidden; }
    .full-width { width: 100%; }
    .date-cell { color: #94A3B8; font-size: 13px; font-family: monospace; }
    .user-cell { display: flex; align-items: center; gap: 8px; }
    .mini-avatar {
      width: 28px; height: 28px; border-radius: 8px;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      color: white; display: flex; align-items: center; justify-content: center;
      font-weight: 700; font-size: 12px;
    }
    .action-badge {
      display: inline-flex; align-items: center; gap: 4px;
      padding: 3px 10px; border-radius: 10px; font-size: 11px;
      font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
    }
    .action-icon { font-size: 14px; width: 14px; height: 14px; }
    .action-create { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .action-update { background: rgba(59,130,246,0.2); color: #93C5FD; }
    .action-delete { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .action-other { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .module-badge {
      padding: 2px 8px; border-radius: 8px; font-size: 11px; font-weight: 600;
      background: rgba(148,163,184,0.15); color: #CBD5E1;
    }
    .details-text { color: #94A3B8; font-size: 13px; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: block; }
    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #64748B; }
    .empty-state h3 { color: #E2E8F0; }
    @media (max-width: 768px) {
      .stats-chips { display: none; }
    }
  `]
})
export class AuditLogComponent implements OnInit {
  entries = signal<AuditEntry[]>([]);
  totalElements = signal(0);
  stats = signal<AuditStats | null>(null);
  loading = signal(true);
  columns = ['performedAt', 'performedBy', 'action', 'module', 'details'];
  moduleFilter = '';
  actionFilter = '';
  userFilter = '';
  currentPage = 0;
  viewMode: 'active' | 'archived' = 'active';

  constructor(
    private auditService: AuditService,
    public auth: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadLog();
    this.loadStats();
  }

  loadLog() {
    this.loading.set(true);
    const obs = this.viewMode === 'archived'
      ? this.auditService.getArchived(this.currentPage, 25, this.moduleFilter, this.userFilter, this.actionFilter)
      : this.auditService.getLog(this.currentPage, 25, this.moduleFilter, this.userFilter, this.actionFilter);

    obs.subscribe({
      next: (res) => {
        this.entries.set(res.content || []);
        this.totalElements.set(res.totalElements || 0);
        this.loading.set(false);
        this.cdr.detectChanges();
      },
      error: () => { this.entries.set([]); this.loading.set(false); this.cdr.detectChanges(); }
    });
  }

  loadStats() {
    this.auditService.getStats().subscribe({
      next: (s) => { this.stats.set(s); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  onViewChange() {
    this.currentPage = 0;
    this.loadLog();
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.loadLog();
  }

  clearFilters() {
    this.moduleFilter = '';
    this.actionFilter = '';
    this.userFilter = '';
    this.currentPage = 0;
    this.loadLog();
  }

  archiveNow() {
    if (!confirm('Archivar todos los registros con mas de 6 meses de antiguedad?')) return;
    this.auditService.archiveNow().subscribe({
      next: (res) => {
        this.snackBar.open(res.message + ' (' + res.archived + ' registros)', 'OK', { duration: 5000 });
        this.loadLog();
        this.loadStats();
      },
      error: () => this.snackBar.open('Error al archivar', 'OK', { duration: 3000 })
    });
  }

  getActionClass(action: string): string {
    if (action?.includes('CREATE') || action === 'LOGIN' || action === 'UPLOAD') return 'action-create';
    if (action?.includes('UPDATE') || action?.includes('CHANGE') || action === 'TRANSFER') return 'action-update';
    if (action?.includes('DELETE')) return 'action-delete';
    return 'action-other';
  }

  getActionIcon(action: string): string {
    if (action === 'UPLOAD') return 'upload_file';
    if (action === 'CHANGE_STATUS') return 'swap_vert';
    if (action?.includes('CREATE')) return 'add_circle';
    if (action?.includes('UPDATE') || action?.includes('CHANGE')) return 'edit';
    if (action?.includes('DELETE')) return 'delete';
    if (action === 'TRANSFER') return 'swap_horiz';
    if (action === 'LOGIN') return 'login';
    if (action?.includes('TOGGLE')) return 'toggle_on';
    return 'info';
  }
}
