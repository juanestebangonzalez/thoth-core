import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MaintenanceService } from '../../core/services/maintenance.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { MaintenanceHistory } from '../../core/models/maintenance.model';
import { Equipment } from '../../core/models/equipment.model';
import { AddMaintenanceDialogComponent } from './add-maintenance-dialog.component';

@Component({
  selector: 'app-equipment-history',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatDividerModule, MatDialogModule, MatSnackBarModule],
  template: `
    <div class="history-page">
      <div class="header">
        <button mat-icon-button [routerLink]="['/equipment', equipmentId]">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>history</mat-icon> Historial de Mantenimientos</h2>
      </div>

      @if (equipment()) {
        <mat-card class="equipment-info">
          <div class="eq-header">
            <div>
              <h3>{{ equipment()?.name }}</h3>
              <p class="subtitle">
                {{ equipment()?.category }} | Serial: {{ equipment()?.serialNumber }}
              </p>
            </div>
            <div class="counter">
              <span class="counter-number">{{ history().length }}</span>
              <span class="counter-label">Mantenimientos</span>
            </div>
          </div>
        </mat-card>
      }

      <div class="add-button-container">
        <button mat-raised-button color="primary" (click)="openAddDialog()">
          <mat-icon>add_circle</mat-icon>
          Registrar Mantenimiento
        </button>
      </div>

      @if (loading()) {
        <p class="loading">Cargando historial...</p>
      } @else if (history().length === 0) {
        <mat-card class="empty-state">
          <mat-icon class="empty-icon">history_toggle_off</mat-icon>
          <h3>Sin mantenimientos registrados</h3>
          <p>Este equipo aun no tiene historial de mantenimientos.</p>
          <button mat-raised-button color="primary" (click)="openAddDialog()">
            <mat-icon>add</mat-icon> Registrar el Primero
          </button>
        </mat-card>
      } @else {
        <div class="timeline">
          @for (item of history(); track item.maintenanceId; let idx = $index) {
            <div class="timeline-item" [class.preventive]="item.maintenanceType === 'PREVENTIVE'" [class.corrective]="item.maintenanceType === 'CORRECTIVE'">
              <div class="timeline-marker">
                <mat-icon>{{ item.maintenanceType === 'PREVENTIVE' ? 'shield' : 'build' }}</mat-icon>
              </div>
              <div class="timeline-content">
                <mat-card>
                  <div class="timeline-header">
                    <div>
                      <span class="type-badge" [class.preventive]="item.maintenanceType === 'PREVENTIVE'" [class.corrective]="item.maintenanceType === 'CORRECTIVE'">
                        {{ item.maintenanceType === 'PREVENTIVE' ? 'PREVENTIVO' : 'CORRECTIVO' }}
                      </span>
                      <span class="date">{{ item.performedDate | date:'dd/MM/yyyy HH:mm' }}</span>
                    </div>
                    <div class="tech-info">
                      <mat-icon>person</mat-icon>
                      <span>{{ item.technicianName }}</span>
                    </div>
                  </div>

                  <mat-divider></mat-divider>

                  <div class="info-block">
                    <div class="label">MOTIVO</div>
                    <p class="value">{{ item.reason }}</p>
                  </div>

                  @if (item.description) {
                    <div class="info-block">
                      <div class="label">DESCRIPCION DEL TRABAJO</div>
                      <p class="value">{{ item.description }}</p>
                    </div>
                  }

                  @if (item.partsReplaced && item.partsReplaced.length > 0) {
                    <mat-divider></mat-divider>
                    <div class="info-block">
                      <div class="label">
                        <mat-icon>construction</mat-icon>
                        PARTES REEMPLAZADAS ({{ item.partsReplaced.length }})
                      </div>
                      <div class="parts-grid">
                        @for (part of item.partsReplaced; track part.partName) {
                          <div class="part-card">
                            <div class="part-name">{{ part.partName }}</div>
                            @if (part.partSerialNumber) {
                              <div class="part-serial">Serial: {{ part.partSerialNumber }}</div>
                            }
                            @if (part.reason) {
                              <div class="part-reason">{{ part.reason }}</div>
                            }
                          </div>
                        }
                      </div>
                    </div>
                  }

                  @if (item.nextScheduledDate) {
                    <mat-divider></mat-divider>
                    <div class="next-schedule">
                      <mat-icon>event_upcoming</mat-icon>
                      <span>Proximo mantenimiento: <strong>{{ item.nextScheduledDate | date:'dd/MM/yyyy' }}</strong></span>
                    </div>
                  }

                  @if (item.signatureBase64) {
                    <mat-divider></mat-divider>
                    <div class="signature-section">
                      <span class="sig-label">Firma de: {{ item.signedBy || "Responsable" }}</span>
                      <img [src]="item.signatureBase64" alt="Firma" class="sig-image" />
                    </div>
                  }

                  <div class="maint-actions">
                    <button mat-stroked-button (click)="generatePdf(item)" class="pdf-btn">
                      <mat-icon>picture_as_pdf</mat-icon> Generar PDF
                    </button>
                    <span class="registered-by-inline">Registrado por {{ item.createdBy }}</span>
                  </div>
                </mat-card>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .history-page { padding: 24px; max-width: 1100px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 {
      display: flex; align-items: center; gap: 8px; margin: 0;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text; font-size: 24px;
    }

    .equipment-info { padding: 20px; margin-bottom: 20px; }
    .eq-header { display: flex; justify-content: space-between; align-items: center; }
    .eq-header h3 { margin: 0 0 4px; color: #F1F5F9; font-size: 20px; }
    .subtitle { margin: 0; color: #94A3B8; font-size: 13px; }
    .counter {
      text-align: center; padding: 12px 24px;
      background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(139, 92, 246, 0.2));
      border: 1px solid rgba(96, 165, 250, 0.3); border-radius: 12px;
    }
    .counter-number {
      display: block; font-size: 32px; font-weight: 700; color: #60A5FA; line-height: 1;
    }
    .counter-label { display: block; color: #94A3B8; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }

    .add-button-container { margin-bottom: 24px; }

    .loading { text-align: center; color: #94A3B8; padding: 40px; }
    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #64748B; }
    .empty-state h3 { color: #E2E8F0; margin: 16px 0 8px; }
    .empty-state p { color: #94A3B8; margin-bottom: 20px; }

    /* Timeline */
    .timeline { position: relative; padding-left: 40px; }
    .timeline::before {
      content: ''; position: absolute; left: 20px; top: 0; bottom: 0;
      width: 2px; background: linear-gradient(180deg, #3B82F6, #8B5CF6);
      opacity: 0.3;
    }

    .timeline-item { position: relative; margin-bottom: 24px; }
    .timeline-marker {
      position: absolute; left: -40px; top: 12px;
      width: 40px; height: 40px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      z-index: 1; border: 3px solid #0F172A;
    }
    .timeline-item.preventive .timeline-marker {
      background: linear-gradient(135deg, #3B82F6, #1E40AF);
      box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
    }
    .timeline-item.corrective .timeline-marker {
      background: linear-gradient(135deg, #F59E0B, #D97706);
      box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4);
    }
    .timeline-marker mat-icon { color: white; }

    .timeline-content mat-card { padding: 20px; }

    .timeline-header {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: 16px; flex-wrap: wrap; gap: 12px;
    }
    .type-badge {
      padding: 4px 12px; border-radius: 12px; font-size: 11px;
      font-weight: 700; text-transform: uppercase; letter-spacing: 1px;
      margin-right: 12px;
    }
    .type-badge.preventive { background: rgba(59, 130, 246, 0.2); color: #93C5FD; }
    .type-badge.corrective { background: rgba(245, 158, 11, 0.2); color: #FBBF24; }
    .date { color: #94A3B8; font-size: 13px; }
    .tech-info {
      display: flex; align-items: center; gap: 6px;
      color: #CBD5E1; font-size: 13px;
    }
    .tech-info mat-icon { font-size: 18px; width: 18px; height: 18px; color: #60A5FA; }

    .info-block { padding: 16px 0; }
    .label {
      display: flex; align-items: center; gap: 6px;
      font-size: 11px; text-transform: uppercase; letter-spacing: 1px;
      color: #94A3B8; margin-bottom: 8px; font-weight: 600;
    }
    .label mat-icon { font-size: 16px; width: 16px; height: 16px; color: #60A5FA; }
    .value { margin: 0; color: #E2E8F0; line-height: 1.5; }

    .parts-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 12px; margin-top: 8px;
    }
    .part-card {
      padding: 12px 14px; border-radius: 8px;
      background: rgba(15, 23, 42, 0.5);
      border-left: 3px solid #60A5FA;
    }
    .part-name { font-weight: 600; color: #E2E8F0; font-size: 14px; }
    .part-serial { color: #94A3B8; font-size: 11px; font-family: 'Courier New', monospace; margin-top: 4px; }
    .part-reason { color: #CBD5E1; font-size: 12px; margin-top: 6px; font-style: italic; }

    .next-schedule {
      display: flex; align-items: center; gap: 8px;
      padding: 12px 16px; border-radius: 8px;
      background: rgba(139, 92, 246, 0.1); border-left: 3px solid #8B5CF6;
      margin-top: 16px; color: #C4B5FD; font-size: 14px;
    }
    .next-schedule mat-icon { color: #8B5CF6; }
    .next-schedule strong { color: #E2E8F0; }

    .signature-section { padding: 12px 0; text-align: center; }
    .sig-label { display: block; color: #94A3B8; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
    .sig-image { max-width: 300px; height: 80px; border-radius: 8px; border: 1px solid rgba(148,163,184,0.15); background: rgba(15,23,42,0.5); }
    .maint-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid rgba(148,163,184,0.1); }
    .pdf-btn { color: #EF4444 !important; border-color: rgba(239,68,68,0.3) !important; font-size: 12px; }
    .registered-by-inline { color: #64748B; font-size: 11px; }
    .registered-by {
      text-align: right; color: #64748B; font-size: 11px;
      margin-top: 12px; padding-top: 12px;
      border-top: 1px solid rgba(148, 163, 184, 0.1);
    }
  `]
})
export class EquipmentHistoryComponent implements OnInit {
  equipment = signal<Equipment | null>(null);
  history = signal<MaintenanceHistory[]>([]);
  loading = signal(true);
  equipmentId = '';

  constructor(
    private route: ActivatedRoute,
    private maintenanceService: MaintenanceService,
    private equipmentService: EquipmentService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.equipmentId = this.route.snapshot.paramMap.get('id') || '';
    this.loadEquipment();
    this.loadHistory();
  }

  loadEquipment() {
    if (this.equipmentId) {
      this.equipmentService.getById(this.equipmentId).subscribe({
        next: (e) => { this.equipment.set(e); this.cdr.detectChanges(); }
      });
    }
  }

  loadHistory() {
    this.loading.set(true);
    this.maintenanceService.getByEquipment(this.equipmentId).subscribe({
      next: (h) => {
        this.history.set(h);
        this.loading.set(false);
        this.cdr.detectChanges();
      },
      error: () => {
        this.history.set([]);
        this.loading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  openAddDialog() {
    const dialogRef = this.dialog.open(AddMaintenanceDialogComponent, {
      width: '700px',
      maxHeight: '90vh',
      data: { equipmentId: this.equipmentId, equipmentName: this.equipment()?.name }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'created') {
        this.snackBar.open('Mantenimiento registrado exitosamente', 'OK', { duration: 3000 });
        this.loadHistory();
      }
    });
  }

  generatePdf(item: MaintenanceHistory) {
    const eq = this.equipment();
    const html = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Acta de Mantenimiento - ${eq?.name || ''}</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: Arial, sans-serif; padding: 40px; color: #333; font-size: 13px; }
    .header { text-align: center; border-bottom: 3px solid #1B4F72; padding-bottom: 16px; margin-bottom: 24px; }
    .header h1 { color: #1B4F72; font-size: 22px; margin-bottom: 4px; }
    .header h2 { color: #2C3E50; font-size: 16px; font-weight: normal; }
    .header p { color: #7F8C8D; font-size: 12px; margin-top: 8px; }
    .section { margin-bottom: 20px; }
    .section-title { background: #1B4F72; color: white; padding: 8px 14px; border-radius: 4px; font-size: 13px; font-weight: bold; margin-bottom: 12px; }
    table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
    th, td { border: 1px solid #ddd; padding: 8px 10px; text-align: left; font-size: 12px; }
    th { background: #f2f3f4; font-weight: bold; color: #2C3E50; width: 35%; }
    .type-badge { display: inline-block; padding: 3px 12px; border-radius: 12px; font-size: 11px; font-weight: bold; }
    .preventive { background: #D4EFDF; color: #1E8449; }
    .corrective { background: #FADBD8; color: #C0392B; }
    .parts-table th { background: #EBF5FB; }
    .signature-section { margin-top: 30px; page-break-inside: avoid; }
    .signature-box { display: flex; justify-content: space-between; margin-top: 20px; }
    .sig-block { text-align: center; width: 45%; }
    .sig-line { border-top: 1px solid #333; margin-top: 60px; padding-top: 8px; }
    .sig-image { height: 80px; margin-bottom: 0; }
    .sig-line-signed { border-top: 1px solid #333; padding-top: 8px; }
    .footer { margin-top: 40px; text-align: center; color: #999; font-size: 10px; border-top: 1px solid #ddd; padding-top: 12px; }
    .description-box { padding: 10px; background: #f9f9f9; border: 1px solid #ddd; border-radius: 4px; min-height: 40px; white-space: pre-wrap; }
    @media print { body { padding: 20px; } }
  </style>
</head>
<body>
  <div class="header">
    <h1>THOTH C.O.R.E</h1>
    <h2>Acta de Mantenimiento Tecnico</h2>
    <p>Instituto del Corazon - Area de Tecnologia</p>
  </div>

  <div class="section">
    <div class="section-title">INFORMACION DEL EQUIPO</div>
    <table>
      <tr><th>Nombre</th><td>${eq?.name || '-'}</td></tr>
      <tr><th>Categoria</th><td>${eq?.category || '-'}</td></tr>
      <tr><th>Serial</th><td>${eq?.serialNumber || '-'}</td></tr>
      <tr><th>N Inventario</th><td>${eq?.inventoryNumber || '-'}</td></tr>
      <tr><th>Marca / Modelo</th><td>${eq?.brand || '-'} ${eq?.model || ''}</td></tr>
      <tr><th>Sede</th><td>${eq?.location?.building || '-'}</td></tr>
      <tr><th>Piso / Oficina</th><td>${eq?.location?.floor || '-'} / ${eq?.location?.office || '-'}</td></tr>
      <tr><th>Asignado a</th><td>${eq?.assignedTo || '-'}</td></tr>
    </table>
  </div>

  <div class="section">
    <div class="section-title">DETALLE DEL MANTENIMIENTO</div>
    <table>
      <tr><th>Tipo</th><td><span class="type-badge ${item.maintenanceType === 'PREVENTIVE' ? 'preventive' : 'corrective'}">${item.maintenanceType === 'PREVENTIVE' ? 'PREVENTIVO' : 'CORRECTIVO'}</span></td></tr>
      <tr><th>Fecha</th><td>${new Date(item.performedDate).toLocaleString('es-CO')}</td></tr>
      <tr><th>Tecnico</th><td>${item.technicianName || '-'}</td></tr>
      <tr><th>Motivo</th><td>${item.reason || '-'}</td></tr>
    </table>
    ${item.description ? '<p style="font-weight:bold;margin-bottom:6px;">Descripcion del trabajo:</p><div class="description-box">' + item.description + '</div>' : ''}
  </div>

  ${item.partsReplaced && item.partsReplaced.length > 0 ? '<div class="section"><div class="section-title">PARTES REEMPLAZADAS (' + item.partsReplaced.length + ')</div><table class="parts-table"><tr><th>Parte</th><th>Serial</th><th>Motivo</th></tr>' + item.partsReplaced.map(p => '<tr><td>' + p.partName + '</td><td>' + (p.partSerialNumber || '-') + '</td><td>' + (p.reason || '-') + '</td></tr>').join('') + '</table></div>' : ''}

  ${item.nextScheduledDate ? '<div class="section"><div class="section-title">PROXIMO MANTENIMIENTO</div><table><tr><th>Fecha programada</th><td>' + new Date(item.nextScheduledDate).toLocaleDateString('es-CO') + '</td></tr></table></div>' : ''}

  <div class="signature-section">
    <div class="section-title">FIRMAS</div>
    <div class="signature-box">
      <div class="sig-block">
        <p style="font-weight:bold;margin-bottom:4px;">${item.technicianName}</p>
        <div class="sig-line">Tecnico Responsable</div>
      </div>
      <div class="sig-block">
        ${item.signatureBase64 ? '<img src="' + item.signatureBase64 + '" class="sig-image" /><p style="font-weight:bold;">' + (item.signedBy || 'Responsable') + '</p><div class="sig-line-signed">Responsable del Equipo</div>' : '<div class="sig-line">Responsable del Equipo</div>'}
      </div>
    </div>
  </div>

  <div class="footer">
    THOTH C.O.R.E - Sistema de Gestion de Activos Tecnologicos | Instituto del Corazon<br>
    Documento generado el ${new Date().toLocaleString('es-CO')}
  </div>
</body>
</html>`;

    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(html);
      printWindow.document.close();
      setTimeout(() => printWindow.print(), 500);
    }
  }
}