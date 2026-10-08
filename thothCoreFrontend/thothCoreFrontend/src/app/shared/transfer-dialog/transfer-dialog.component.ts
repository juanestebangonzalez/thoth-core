import { Component, Inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { EquipmentService } from '../../core/services/equipment.service';
import { LocationHistoryService } from '../../core/services/location-history.service';
import { SedeService } from '../../core/services/sede.service';
import { AreaService, Area } from '../../core/services/area.service';

@Component({
  selector: 'app-transfer-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatCheckboxModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <div class="icon-wrapper">
          <mat-icon>swap_horiz</mat-icon>
        </div>
        <div>
          <h2>Trasladar Equipo</h2>
          <p>{{ data.equipmentName }}</p>
          <p class="current-loc">Ubicacion actual: <strong>{{ data.currentBuilding || 'Sin asignar' }} - {{ data.currentOffice || '' }}</strong></p>
        </div>
      </div>

      <div class="form-grid">
        <mat-form-field appearance="outline">
          <mat-label>Sede Destino</mat-label>
          <mat-select [(ngModel)]="toBuilding" required>
            @for (sede of sedes; track sede.name) {
              <mat-option [value]="sede.name">{{ sede.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Area Destino</mat-label>
          <mat-select [(ngModel)]="toOffice">
            <mat-option value="">-- Sin asignar --</mat-option>
            @for (area of areas; track area.id) {
              <mat-option [value]="area.name">{{ area.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-col">
          <mat-label>Motivo del traslado</mat-label>
          <textarea matInput [(ngModel)]="reason" required rows="3" placeholder="Por que se traslada este equipo?"></textarea>
        </mat-form-field>
      </div>

      @if (monitorCount > 0) {
        <mat-checkbox [(ngModel)]="includeMonitors" class="monitors-check">
          Trasladar tambien los monitores asociados ({{ monitorCount }})
        </mat-checkbox>
      }

      <div class="actions">
        <button mat-button (click)="cancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="transfer()" [disabled]="loading">
          {{ loading ? 'Trasladando...' : 'Confirmar Traslado' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { padding: 24px; color: #E2E8F0; }
    .dialog-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .icon-wrapper {
      width: 56px; height: 56px; border-radius: 14px;
      background: linear-gradient(135deg, #F59E0B, #EF4444);
      display: flex; align-items: center; justify-content: center;
    }
    .icon-wrapper mat-icon { color: white; font-size: 28px; width: 28px; height: 28px; }
    h2 { margin: 0; color: #F1F5F9; font-size: 20px; }
    p { margin: 2px 0; color: #94A3B8; font-size: 14px; }
    .current-loc { font-size: 12px; }
    .current-loc strong { color: #60A5FA; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .full-col { grid-column: 1 / -1; }
    .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    .monitors-check { display: block; margin-bottom: 8px; }
  `]
})
export class TransferDialogComponent {
  toBuilding = '';
  toOffice = '';
  reason = '';
  loading = false;
  sedes: any[] = [];
  areas: Area[] = [];
  includeMonitors = true;
  monitorCount = 0;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { equipmentId: string, equipmentName: string, currentBuilding: string, currentOffice: string },
    private dialogRef: MatDialogRef<TransferDialogComponent>,
    private locationHistoryService: LocationHistoryService,
    private sedeService: SedeService,
    private areaService: AreaService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private equipmentService: EquipmentService
  ) {
    this.equipmentService.getMonitors(this.data.equipmentId).subscribe({
      next: (m) => { this.monitorCount = (m || []).length; this.cdr.markForCheck(); },
      error: () => { this.monitorCount = 0; }
    });
    this.sedeService.listActive().subscribe({
      next: (s) => { this.sedes = s; this.cdr.markForCheck(); },
      error: () => {}
    });
    this.areaService.listActive().subscribe({
      next: (a) => { this.areas = a; this.cdr.markForCheck(); },
      error: () => {}
    });
  }

  transfer() {
    if (!this.toBuilding || !this.reason) {
      this.snackBar.open('Sede destino y motivo son obligatorios', 'OK', { duration: 3000 });
      return;
    }
    this.loading = true;
    this.locationHistoryService.transfer(this.data.equipmentId, {
      toBuilding: this.toBuilding,
      toFloor: '',
      toOffice: this.toOffice,
      reason: this.reason,
      includeMonitors: this.monitorCount > 0 ? this.includeMonitors : true
    }).subscribe({
      next: () => this.dialogRef.close('transferred'),
      error: (err) => {
        this.loading = false;
        this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo trasladar'), 'OK', { duration: 5000 });
      }
    });
  }

  cancel() { this.dialogRef.close(); }
}
