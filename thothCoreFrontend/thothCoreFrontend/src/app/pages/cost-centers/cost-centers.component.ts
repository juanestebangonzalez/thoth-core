import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CostCenterService, CostCenter } from '../../core/services/cost-center.service';

@Component({
  selector: 'app-cost-centers',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatDividerModule, MatTooltipModule],
  template: `
    <div class="areas-page">
      <div class="header">
        <h1><mat-icon>account_balance</mat-icon> Centros de Costo</h1>
      </div>

      <!-- Formulario agregar/editar -->
      <mat-card class="form-card">
        <h3>{{ editingId ? 'Editar Centro de Costo' : 'Nuevo Centro de Costo' }}</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" floatLabel="always" class="field-name">
            <mat-label>Nombre del Centro de Costo *</mat-label>
            <input matInput [(ngModel)]="areaName" placeholder="Ej: ASISTENCIAL" (keyup.enter)="save()">
          </mat-form-field>
          <mat-form-field appearance="outline" floatLabel="always" class="field-desc">
            <mat-label>Descripcion (opcional)</mat-label>
            <input matInput [(ngModel)]="areaDescription" placeholder="Descripcion del centro de costo">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="save()" [disabled]="!areaName.trim()">
            <mat-icon>{{ editingId ? 'save' : 'add' }}</mat-icon>
            {{ editingId ? 'Actualizar' : 'Agregar' }}
          </button>
          @if (editingId) {
            <button mat-stroked-button (click)="cancelEdit()">Cancelar</button>
          }
        </div>
      </mat-card>

      <!-- Lista de areas -->
      <mat-card class="list-card">
        <h3>Centros de Costo Registrados ({{ areas().length }})</h3>
        @if (areas().length === 0) {
          <p class="empty">No hay centros de costo registrados</p>
        } @else {
          @for (area of areas(); track area.id) {
            <div class="area-row" [class.inactive]="!area.active">
              <div class="area-icon" [class.active]="area.active">
                <mat-icon>{{ area.active ? 'account_balance' : 'block' }}</mat-icon>
              </div>
              <div class="area-info">
                <span class="area-name">{{ area.name }}</span>
                @if (area.description) {
                  <span class="area-desc">{{ area.description }}</span>
                }
              </div>
              <span class="area-status" [class.active]="area.active">
                {{ area.active ? 'Activo' : 'Inactivo' }}
              </span>
              <div class="area-actions">
                <button mat-icon-button (click)="edit(area)" matTooltip="Editar">
                  <mat-icon style="color:#60A5FA;">edit</mat-icon>
                </button>
                @if (area.active) {
                  <button mat-icon-button (click)="setActive(area, false)" matTooltip="Desactivar">
                    <mat-icon style="color:#F59E0B;">block</mat-icon>
                  </button>
                } @else {
                  <button mat-icon-button (click)="setActive(area, true)" matTooltip="Activar">
                    <mat-icon style="color:#10B981;">check_circle</mat-icon>
                  </button>
                }
                <button mat-icon-button (click)="remove(area)" matTooltip="Eliminar">
                  <mat-icon style="color:#EF4444;">delete</mat-icon>
                </button>
              </div>
            </div>
          }
        }
      </mat-card>
    </div>
  `,
  styles: [`
    .areas-page { padding: 32px; max-width: 900px; margin: 0 auto; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0 0 24px; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .form-card { padding: 24px; margin-bottom: 16px; }
    .form-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .form-row { display: flex; gap: 12px; align-items: flex-start; flex-wrap: wrap; }
    .field-name { flex: 1; min-width: 200px; }
    .field-desc { flex: 1.5; min-width: 200px; }
    .list-card { padding: 24px; }
    .list-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .empty { text-align: center; color: #64748B; padding: 40px; }
    .area-row { display: flex; align-items: center; gap: 14px; padding: 14px 16px; border-radius: 10px; background: rgba(30,41,59,0.5); margin-bottom: 8px; transition: all 0.2s; }
    .area-row:hover { background: rgba(59,130,246,0.08); }
    .area-row.inactive { opacity: 0.5; }
    .area-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: rgba(100,116,139,0.2); }
    .area-icon.active { background: rgba(59,130,246,0.2); }
    .area-icon mat-icon { color: #60A5FA; }
    .area-info { flex: 1; min-width: 0; }
    .area-name { display: block; color: #F1F5F9; font-weight: 600; font-size: 15px; }
    .area-desc { display: block; color: #94A3B8; font-size: 12px; margin-top: 2px; }
    .area-status { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; background: rgba(100,116,139,0.2); color: #94A3B8; flex-shrink: 0; }
    .area-status.active { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .area-actions { display: flex; gap: 4px; flex-shrink: 0; }
    @media (max-width: 768px) {
      .areas-page { padding: 16px; }
      .form-row { flex-direction: column; }
      .field-name, .field-desc { min-width: 100%; }
    }
  `]
})
export class CostCentersComponent implements OnInit {
  areas = signal<CostCenter[]>([]);
  areaName = '';
  areaDescription = '';
  editingId = '';

  constructor(private areaService: CostCenterService, private snackBar: MatSnackBar, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.loadAreas(); }

  loadAreas() {
    this.areaService.listAll().subscribe({
      next: (a) => { this.areas.set(a); this.cdr.detectChanges(); },
      error: () => this.snackBar.open('Error al cargar los centros de costo', 'OK', { duration: 3000 })
    });
  }

  save() {
    if (!this.areaName.trim()) return;
    const data: Partial<CostCenter> = { name: this.areaName.trim(), description: this.areaDescription.trim() || undefined };

    if (this.editingId) {
      this.areaService.update(this.editingId, data).subscribe({
        next: () => { this.snackBar.open('Centro de costo actualizado', 'OK', { duration: 3000 }); this.cancelEdit(); this.loadAreas(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error al actualizar el centro de costo', 'OK', { duration: 3000 })
      });
    } else {
      this.areaService.create(data).subscribe({
        next: () => { this.snackBar.open('Centro de costo creado', 'OK', { duration: 3000 }); this.cancelEdit(); this.loadAreas(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error al crear el centro de costo', 'OK', { duration: 3000 })
      });
    }
  }

  edit(area: CostCenter) {
    this.editingId = '';
    this.areaName = '';
    this.areaDescription = '';
    this.cdr.detectChanges();
    setTimeout(() => {
      this.editingId = area.id;
      this.areaName = area.name;
      this.areaDescription = area.description || '';
      this.cdr.detectChanges();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  cancelEdit() { this.editingId = ''; this.areaName = ''; this.areaDescription = ''; this.cdr.detectChanges(); }

  setActive(area: CostCenter, active: boolean) {
    if (!active && !confirm('Desactivar centro de costo ' + area.name + '?')) return;
    this.areaService.update(area.id, { active }).subscribe({
      next: () => { this.snackBar.open(active ? 'Centro de costo activado' : 'Centro de costo desactivado', 'OK', { duration: 3000 }); this.loadAreas(); },
      error: (e) => this.snackBar.open(e.error?.message || 'Error al cambiar el estado', 'OK', { duration: 3000 })
    });
  }

  remove(area: CostCenter) {
    if (!confirm('Eliminar definitivamente el centro de costo ' + area.name + '? Esta accion no se puede deshacer.')) return;
    this.areaService.delete(area.id).subscribe({
      next: () => { this.snackBar.open('Centro de costo eliminado', 'OK', { duration: 3000 }); if (this.editingId === area.id) this.cancelEdit(); this.loadAreas(); },
      error: (e) => this.snackBar.open(e.error?.message || 'Error al eliminar el centro de costo', 'OK', { duration: 3000 })
    });
  }
}
