import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SignaturePadComponent } from '../../shared/signature-pad/signature-pad.component';
import { MaintenanceService } from '../../core/services/maintenance.service';
import { TechnicianService, Technician } from '../../core/services/technician.service';
import { CreateMaintenanceRequest, PartReplaced } from '../../core/models/maintenance.model';

@Component({
  selector: 'app-add-maintenance-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatDividerModule, MatSnackBarModule, SignaturePadComponent],
  template: `
    <div class="dialog-container">
      <div class="dialog-header">
        <div class="icon-wrapper">
          <mat-icon>add_circle</mat-icon>
        </div>
        <div>
          <h2>Registrar Mantenimiento</h2>
          <p>Equipo: <strong>{{ data.equipmentName }}</strong></p>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-heading">Informacion del Trabajo</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Tipo de Mantenimiento</mat-label>
            <mat-select [(ngModel)]="maintenance.maintenanceType" required>
              <mat-option value="PREVENTIVE">
                <mat-icon style="color:#3B82F6;">shield</mat-icon>
                Preventivo
              </mat-option>
              <mat-option value="CORRECTIVE">
                <mat-icon style="color:#F59E0B;">build</mat-icon>
                Correctivo
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Tecnico Responsable</mat-label>
            <mat-select [(ngModel)]="selectedTechnicianId" (selectionChange)="onTechnicianChange()" required>
              @for (tech of technicians; track tech.id) {
                <mat-option [value]="tech.id">
                  <mat-icon style="color:#F59E0B;font-size:18px;vertical-align:middle;">engineering</mat-icon>
                  {{ tech.username }}
                </mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-col">
            <mat-label>Motivo del Mantenimiento</mat-label>
            <input matInput [(ngModel)]="maintenance.reason" required placeholder="Por que se realizo este mantenimiento?">
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-col">
            <mat-label>Descripcion del Trabajo</mat-label>
            <textarea matInput [(ngModel)]="maintenance.description" rows="3" placeholder="Detalle del trabajo realizado, acciones tomadas..."></textarea>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Proximo Mantenimiento (Opcional)</mat-label>
            <input matInput [(ngModel)]="maintenance.nextScheduledDate" type="date">
          </mat-form-field>
        </div>
      </div>

      <mat-divider></mat-divider>

      <div class="form-section">
        <div class="section-title-row">
          <h3 class="section-heading">
            <mat-icon>construction</mat-icon>
            Partes Reemplazadas ({{ parts.length }})
          </h3>
          <button mat-stroked-button color="primary" (click)="addPart()">
            <mat-icon>add</mat-icon> Agregar Parte
          </button>
        </div>

        @if (parts.length === 0) {
          <p class="no-parts">No se han agregado partes. Si no se reemplazo ninguna, deja vacio.</p>
        }

        @for (part of parts; track $index; let idx = $index) {
          <div class="part-row">
            <div class="part-header">
              <span class="part-number">Parte #{{ idx + 1 }}</span>
              <button mat-icon-button color="warn" (click)="removePart(idx)">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
            <div class="form-grid">
              <mat-form-field appearance="outline">
                <mat-label>Nombre de la Parte</mat-label>
                <input matInput [(ngModel)]="part.partName" required placeholder="Ej: Disco SSD 480GB">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Serial (Opcional)</mat-label>
                <input matInput [(ngModel)]="part.partSerialNumber" placeholder="Serial de la parte nueva">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-col">
                <mat-label>Motivo del Cambio</mat-label>
                <input matInput [(ngModel)]="part.reason" placeholder="Por que se cambio esta parte?">
              </mat-form-field>
            </div>
          </div>
        }
      </div>

      <mat-divider></mat-divider>

      <div class="form-section">
        <h3 class="section-heading">
          <mat-icon>draw</mat-icon>
          Firma del Responsable del Equipo
        </h3>
        <mat-form-field appearance="outline" style="width:100%;">
          <mat-label>Nombre de quien firma</mat-label>
          <input matInput [(ngModel)]="signedBy" placeholder="Nombre completo del responsable">
        </mat-form-field>
        <app-signature-pad (signatureChange)="onSignatureChange($event)" label="Firma (obligatoria para constancia)"></app-signature-pad>
      </div>

      <mat-divider></mat-divider>

      <div class="actions">
        <button mat-button (click)="cancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="save()" [disabled]="loading">
          {{ loading ? 'Guardando...' : 'Guardar Mantenimiento' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .dialog-container { padding: 24px; color: #E2E8F0; max-height: 85vh; overflow-y: auto; }
    .dialog-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .icon-wrapper {
      width: 56px; height: 56px; border-radius: 14px;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
    }
    .icon-wrapper mat-icon { color: white; font-size: 28px; width: 28px; height: 28px; }
    h2 { margin: 0; color: #F1F5F9; font-size: 20px; }
    p { margin: 4px 0 0; color: #94A3B8; font-size: 14px; }
    .form-section { padding: 16px 0; }
    .section-heading {
      display: flex; align-items: center; gap: 8px;
      color: #94A3B8; font-size: 12px; text-transform: uppercase;
      letter-spacing: 1px; font-weight: 600; margin: 0 0 16px;
    }
    .section-heading mat-icon { color: #60A5FA; font-size: 18px; width: 18px; height: 18px; }
    .section-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .full-col { grid-column: 1 / -1; }
    .no-parts { text-align: center; color: #64748B; font-style: italic; font-size: 13px; padding: 16px; }
    .part-row {
      background: rgba(15, 23, 42, 0.5); border: 1px solid rgba(148, 163, 184, 0.15);
      border-radius: 12px; padding: 16px; margin-bottom: 12px;
      border-left: 4px solid #60A5FA;
    }
    .part-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 12px;
    }
    .part-number { color: #60A5FA; font-weight: 600; font-size: 14px; }
    .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    mat-divider { margin: 16px 0 !important; border-top-color: rgba(148, 163, 184, 0.15) !important; }
  `]
})
export class AddMaintenanceDialogComponent implements OnInit {
  maintenance: CreateMaintenanceRequest = {
    equipmentId: '',
    maintenanceType: '',
    technicianName: '',
    reason: '',
    description: '',
    nextScheduledDate: '',
    partsReplaced: []
  };
  technicians: Technician[] = [];
  selectedTechnicianId = '';
  parts: PartReplaced[] = [];
  loading = false;
  signedBy = '';
  signatureBase64 = '';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { equipmentId: string, equipmentName: string },
    private dialogRef: MatDialogRef<AddMaintenanceDialogComponent>,
    private maintenanceService: MaintenanceService,
    private technicianService: TechnicianService,
    private snackBar: MatSnackBar
  ) {
    this.maintenance.equipmentId = data.equipmentId;
  }

  ngOnInit() {
    this.technicianService.list().subscribe({
      next: (techs) => this.technicians = techs,
      error: () => this.technicians = []
    });
  }

  onTechnicianChange() {
    const tech = this.technicians.find(t => t.id === this.selectedTechnicianId);
    if (tech) {
      this.maintenance.technicianName = tech.username;
      this.maintenance.technicianId = tech.id;
    }
  }

  onSignatureChange(signature: string | null) {
    this.signatureBase64 = signature || '';
  }

  addPart() {
    this.parts.push({ partName: '', partSerialNumber: '', reason: '' });
  }

  removePart(index: number) {
    this.parts.splice(index, 1);
  }

  save() {
  if (!this.maintenance.maintenanceType) {
    this.snackBar.open('Selecciona el tipo de mantenimiento', 'OK', { duration: 3000 });
    return;
  }
  if (!this.maintenance.technicianName || !this.maintenance.reason) {
    this.snackBar.open('Tecnico y motivo son obligatorios', 'OK', { duration: 3000 });
    return;
  }

  const invalidPart = this.parts.find(p => !p.partName);
  if (invalidPart) {
    this.snackBar.open('Todas las partes deben tener nombre', 'OK', { duration: 3000 });
    return;
  }

  this.loading = true;
  this.maintenance.partsReplaced = this.parts.length > 0 ? this.parts : undefined;
  (this.maintenance as any).signatureBase64 = this.signatureBase64 || null;
  (this.maintenance as any).signedBy = this.signedBy || this.maintenance.technicianName;

  this.maintenanceService.create(this.maintenance).subscribe({
    next: () => this.dialogRef.close('created'),
    error: (err) => {
      this.loading = false;
      this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo guardar'), 'OK', { duration: 5000 });
    }
  });
}

  cancel() { this.dialogRef.close(); }
}