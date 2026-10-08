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
import { EquipmentService } from '../../core/services/equipment.service';
import { PeripheralTypeService } from '../../core/services/peripheral-type.service';
import { Peripheral } from '../../core/models/equipment.model';

export interface PeripheralDialogData { equipmentId: string; equipmentName: string; peripheral?: Peripheral; }

/** Dialogo para agregar o editar un periferico de un equipo. */
@Component({
  selector: 'app-peripheral-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <div class="icon-wrapper"><mat-icon>keyboard</mat-icon></div>
        <div>
          <h2>{{ data.peripheral ? 'Editar Periferico' : 'Agregar Periferico' }}</h2>
          <p>Equipo: <strong>{{ data.equipmentName }}</strong></p>
        </div>
      </div>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Tipo de periferico *</mat-label>
        <mat-select [(ngModel)]="type">
          @for (t of types; track t) {
            <mat-option [value]="t">{{ t }}</mat-option>
          }
        </mat-select>
        @if (loaded && types.length === 0) {
          <mat-hint>No hay tipos activos. Creelos en Tipos de Periferico.</mat-hint>
        }
      </mat-form-field>
      <mat-form-field appearance="outline" class="full">
        <mat-label>Marca *</mat-label>
        <input matInput [(ngModel)]="brand" (keyup.enter)="save()" placeholder="Ej: LOGITECH">
      </mat-form-field>
      <div class="actions">
        <button mat-button (click)="dialogRef.close()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="save()" [disabled]="saving || !type || !brand.trim()">
          {{ saving ? 'Guardando...' : 'Guardar' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { padding: 24px; color: #E2E8F0; min-width: 320px; }
    .dialog-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
    .icon-wrapper { width: 48px; height: 48px; border-radius: 12px; background: linear-gradient(135deg, #3B82F6, #8B5CF6); display: flex; align-items: center; justify-content: center; }
    .icon-wrapper mat-icon { color: white; }
    h2 { margin: 0; color: #F1F5F9; font-size: 20px; }
    p { margin: 2px 0 0; color: #94A3B8; font-size: 14px; }
    .full { width: 100%; }
    input { text-transform: uppercase; }
    .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px; }
  `]
})
export class PeripheralDialogComponent {
  types: string[] = [];
  loaded = false;
  type = '';
  brand = '';
  saving = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: PeripheralDialogData,
    public dialogRef: MatDialogRef<PeripheralDialogComponent>,
    private equipmentService: EquipmentService,
    private peripheralTypeService: PeripheralTypeService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {
    this.type = data.peripheral?.type || '';
    this.brand = data.peripheral?.brand || '';
    this.peripheralTypeService.listActive().subscribe({
      next: (list) => {
        const names = (list || []).map(t => t.name);
        // Si el tipo actual ya no esta activo se conserva como opcion
        if (this.type && !names.some(n => n.toUpperCase() === this.type.toUpperCase())) names.unshift(this.type);
        this.types = names;
        this.loaded = true;
        this.cdr.markForCheck();
      },
      error: () => { this.loaded = true; this.types = this.type ? [this.type] : []; this.cdr.markForCheck(); }
    });
  }

  save() {
    if (!this.type || !this.brand.trim() || this.saving) return;
    this.saving = true;
    const body = { type: this.type, brand: this.brand.trim().toUpperCase() };
    const obs = this.data.peripheral
      ? this.equipmentService.updatePeripheral(this.data.equipmentId, this.data.peripheral.id, body)
      : this.equipmentService.addPeripheral(this.data.equipmentId, body);
    obs.subscribe({
      next: () => this.dialogRef.close(true),
      error: (e) => {
        this.saving = false;
        this.snackBar.open('Error: ' + (e.error?.message || 'No se pudo guardar el periferico'), 'OK', { duration: 5000 });
        this.cdr.markForCheck();
      }
    });
  }
}
