import { environment } from '../../../environments/environment';
import { EquipmentDocumentsComponent } from '../equipment-documents/equipment-documents.component';
import { Component, OnInit, signal, ChangeDetectorRef, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EquipmentService } from '../../core/services/equipment.service';
import { LocationHistoryService, LocationHistory } from '../../core/services/location-history.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TransferDialogComponent } from '../../shared/transfer-dialog/transfer-dialog.component';
import { Equipment } from '../../core/models/equipment.model';

@Component({
  selector: 'app-equipment-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule, MatChipsModule, MatSnackBarModule, MatDialogModule, EquipmentDocumentsComponent],
  template: `
    <div class="detail-page">
      <div class="header">
        <button mat-icon-button routerLink="/equipment">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2>Detalle del Equipo</h2>
      </div>

      @if (equipment()) {
        <mat-card class="detail-card">
          <div class="card-header">
            <div>
              <h3>{{ equipment()?.name }}</h3>
              <p class="subtitle">
                {{ equipment()?.category }} | Serial: {{ equipment()?.serialNumber }}
                @if (equipment()?.ownershipType === 'RENTED') {
                  <span class="ownership-badge rented">ALQUILADO</span>
                } @else {
                  <span class="ownership-badge owned">PROPIO</span>
                }
              </p>
            </div>
            <span class="status-badge" [class]="equipment()?.status?.toLowerCase()">{{ equipment()?.status }}</span>
          </div>

          @if (maintenanceStatus() !== 'none') {
            <div class="maintenance-alert" [class]="maintenanceStatus()">
              <mat-icon>{{ maintenanceIcon() }}</mat-icon>
              <div class="alert-content">
                <strong>{{ maintenanceMessage() }}</strong>
                <span class="alert-sub">Proximo mantenimiento programado: {{ equipment()?.nextMaintenanceDate | date:'dd/MM/yyyy' }}</span>
              </div>
              <button mat-stroked-button class="alert-btn" [routerLink]="['/equipment', equipmentId, 'history']">
                <mat-icon>build</mat-icon> Registrar
              </button>
            </div>
          }

          <mat-divider></mat-divider>

          <div class="status-actions">
            <p class="section-heading">CAMBIAR ESTADO</p>
            <div class="status-buttons">
              <button mat-stroked-button class="btn-active" (click)="changeStatus('ACTIVE')">
                <mat-icon>check_circle</mat-icon> Activar
              </button>
              <button mat-stroked-button class="btn-maintenance" (click)="changeStatus('MAINTENANCE')">
                <mat-icon>build</mat-icon> Mantenimiento
              </button>
              <button mat-stroked-button class="btn-inactive" (click)="changeStatus('INACTIVE')">
                <mat-icon>block</mat-icon> Inactivo
              </button>
              <button mat-stroked-button class="btn-retired" (click)="changeStatus('RETIRED')">
                <mat-icon>archive</mat-icon> Retirar
              </button>
            </div>
          </div>

          <mat-divider></mat-divider>

          <p class="section-heading">INFORMACION GENERAL</p>
          <div class="info-grid">
            <div class="info-item">
              <mat-icon>inventory</mat-icon>
              <div><span class="label">N Inventario</span><span class="value">{{ equipment()?.inventoryNumber || '-' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>business</mat-icon>
              <div><span class="label">Marca</span><span class="value">{{ equipment()?.brand || '-' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>memory</mat-icon>
              <div><span class="label">Modelo</span><span class="value">{{ equipment()?.model || '-' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>router</mat-icon>
              <div><span class="label">MAC Address</span><span class="value">{{ equipment()?.macAddress || '-' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>calendar_today</mat-icon>
              <div><span class="label">Fecha de Compra</span><span class="value">{{ equipment()?.purchaseDate || '-' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>attach_money</mat-icon>
              <div><span class="label">Valor de Compra</span><span class="value">$ {{ equipment()?.purchaseValue || 0 }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>person</mat-icon>
              <div><span class="label">Asignado a</span><span class="value">{{ equipment()?.assignedTo || 'Sin asignar' }}</span></div>
            </div>
            <div class="info-item">
              <mat-icon>location_on</mat-icon>
              <div><span class="label">Ubicacion</span><span class="value">
                {{ equipment()?.location?.building || '-' }} -
                Piso {{ equipment()?.location?.floor || '-' }} -
                {{ equipment()?.location?.office || '-' }}
              </span></div>
            </div>
            <div class="info-item">
              <mat-icon>admin_panel_settings</mat-icon>
              <div><span class="label">Registrado por</span><span class="value">{{ equipment()?.createdBy || 'SYSTEM' }}</span></div>
            </div>
            @if (equipment()?.nextMaintenanceDate) {
              <div class="info-item">
                <mat-icon>event_upcoming</mat-icon>
                <div>
                  <span class="label">Proximo Mantenimiento</span>
                  <span class="value" [class.next-critical]="maintenanceStatus() === 'critical'"
                                       [class.next-warning]="maintenanceStatus() === 'warning'"
                                       [class.next-info]="maintenanceStatus() === 'info'">
                    {{ equipment()?.nextMaintenanceDate | date:'dd/MM/yyyy' }}
                  </span>
                </div>
              </div>
            }
          </div>

          @if (equipment()?.hardware) {
            <mat-divider></mat-divider>
            <p class="section-heading"><mat-icon>memory</mat-icon> HARDWARE</p>
            <div class="info-grid">
              <div class="info-item">
                <mat-icon>developer_board</mat-icon>
                <div><span class="label">Procesador</span><span class="value">{{ equipment()?.hardware?.processor || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>view_module</mat-icon>
                <div>
                  <span class="label">RAM</span>
                  <span class="value">{{ equipment()?.hardware?.ramSizeGb ? equipment()?.hardware?.ramSizeGb + ' GB' : '-' }} {{ equipment()?.hardware?.ramType || '' }}</span>
                </div>
              </div>
              <div class="info-item">
                <mat-icon>storage</mat-icon>
                <div>
                  <span class="label">Disco</span>
                  <span class="value">{{ equipment()?.hardware?.diskType || '-' }} {{ equipment()?.hardware?.diskSizeGb ? equipment()?.hardware?.diskSizeGb + ' GB' : '' }}</span>
                </div>
              </div>
              <div class="info-item">
                <mat-icon>health_and_safety</mat-icon>
                <div>
                  <span class="label">Salud del Disco</span>
                  <span class="value">
                    {{ equipment()?.hardware?.diskHealthPercent !== undefined ? equipment()?.hardware?.diskHealthPercent + '%' : '-' }}
                    @if (equipment()?.hardware?.diskHealthStatus === 'CRITICO') {
                      <span class="hw-badge critical">CRITICO</span>
                    } @else if (equipment()?.hardware?.diskHealthStatus === 'ADVERTENCIA') {
                      <span class="hw-badge warning">ADVERTENCIA</span>
                    } @else if (equipment()?.hardware?.diskHealthStatus === 'OK') {
                      <span class="hw-badge ok">OK</span>
                    }
                  </span>
                </div>
              </div>
              <div class="info-item">
                <mat-icon>thermostat</mat-icon>
                <div>
                  <span class="label">Temperatura</span>
                  <span class="value">
                    {{ equipment()?.hardware?.diskTemperatureCelsius !== undefined ? equipment()?.hardware?.diskTemperatureCelsius + ' C' : '-' }}
                    @if (equipment()?.hardware?.diskTemperatureStatus === 'CRITICO') {
                      <span class="hw-badge critical">CRITICO</span>
                    } @else if (equipment()?.hardware?.diskTemperatureStatus === 'ADVERTENCIA') {
                      <span class="hw-badge warning">ADVERTENCIA</span>
                    } @else if (equipment()?.hardware?.diskTemperatureStatus === 'OK') {
                      <span class="hw-badge ok">OK</span>
                    }
                  </span>
                </div>
              </div>
            </div>
          }

          @if (equipment()?.ownershipType === 'RENTED' && equipment()?.rentalInfo) {
            <mat-divider></mat-divider>
            <p class="section-heading">
              <mat-icon>description</mat-icon> ALQUILER
              @if (equipment()?.rentalInfo?.isExpired) {
                <span class="hw-badge critical">CONTRATO VENCIDO</span>
              } @else if (equipment()?.rentalInfo?.isExpiringSoon) {
                <span class="hw-badge warning">VENCE EN {{ equipment()?.rentalInfo?.daysUntilExpiry }} DIAS</span>
              }
            </p>
            <div class="info-grid">
              <div class="info-item">
                <mat-icon>business</mat-icon>
                <div><span class="label">Empresa</span><span class="value">{{ equipment()?.rentalInfo?.rentalCompany || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>description</mat-icon>
                <div><span class="label">Contrato N</span><span class="value">{{ equipment()?.rentalInfo?.contractNumber || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>person</mat-icon>
                <div><span class="label">Contacto</span><span class="value">{{ equipment()?.rentalInfo?.contactName || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>phone</mat-icon>
                <div><span class="label">Telefono</span><span class="value">{{ equipment()?.rentalInfo?.contactPhone || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>email</mat-icon>
                <div><span class="label">Email</span><span class="value">{{ equipment()?.rentalInfo?.contactEmail || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>event</mat-icon>
                <div><span class="label">Inicio Contrato</span><span class="value">{{ equipment()?.rentalInfo?.startDate || '-' }}</span></div>
              </div>
              <div class="info-item">
                <mat-icon>event_busy</mat-icon>
                <div><span class="label">Fin Contrato</span><span class="value">{{ equipment()?.rentalInfo?.endDate || '-' }}</span></div>
              </div>
              @if (equipment()?.rentalInfo?.contractFileUrl) {
                <div class="info-item">
                  <mat-icon>picture_as_pdf</mat-icon>
                  <div>
                    <span class="label">Contrato PDF</span>
                    <a [href]="equipment()?.rentalInfo?.contractFileUrl" target="_blank" class="link">Descargar</a>
                  </div>
                </div>
              }
            </div>
          }

          <mat-divider></mat-divider>

          @if (showQrCode()) {
            <mat-divider></mat-divider>
            <div class="qr-section">
              <p class="section-heading"><mat-icon>qr_code</mat-icon> CODIGO QR</p>
              <div class="qr-container">
                <img [src]="qrUrl" alt="QR Code" class="qr-image" />
                <div class="qr-info">
                  <p>Escanea este codigo con la camara del movil para acceder al detalle del equipo.</p>
                  <button mat-stroked-button color="primary" (click)="downloadQr()">
                    <mat-icon>download</mat-icon> Descargar QR
                  </button>
                </div>
              </div>
            </div>
          }

          <mat-divider></mat-divider>

          @if (equipment()) {
            <app-equipment-documents [equipmentId]="equipmentId"></app-equipment-documents>
          }

          <mat-divider></mat-divider>

          <!-- Location History -->
          <p class="section-heading"><mat-icon>swap_horiz</mat-icon> HISTORIAL DE UBICACIONES</p>
          <div class="transfer-header">
            <button mat-stroked-button style="color:#F59E0B;border-color:#F59E0B;" (click)="openTransferDialog()">
              <mat-icon>swap_horiz</mat-icon> Trasladar Equipo
            </button>
          </div>
          @if (locationHistory().length > 0) {
            <div class="timeline">
              @for (loc of locationHistory(); track loc.id) {
                <div class="timeline-item">
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="timeline-date">{{ loc.transferredAt | date:'dd/MM/yyyy HH:mm' }}</span>
                      <span class="timeline-user">por {{ loc.performedBy }}</span>
                    </div>
                    <div class="timeline-body">
                      <span class="loc-from">{{ loc.fromBuilding || 'Sin ubicacion' }}</span>
                      <mat-icon class="arrow-icon">arrow_forward</mat-icon>
                      <span class="loc-to">{{ loc.toBuilding }} - {{ loc.toOffice }}</span>
                    </div>
                    <p class="timeline-reason">{{ loc.reason }}</p>
                  </div>
                </div>
              }
            </div>
          } @else {
            <p class="no-transfers">No hay traslados registrados para este equipo.</p>
          }

          <mat-divider></mat-divider>

          <div class="actions">
            <button mat-raised-button style="background:#059669;color:white;" (click)="showQr()">
              <mat-icon>qr_code</mat-icon> Ver QR
            </button>
            <button mat-raised-button color="accent" [routerLink]="['/ai', equipmentId]">
              <mat-icon>psychology</mat-icon> Analisis IA
            </button>
            <button mat-raised-button style="background:#8B5CF6;color:white;" [routerLink]="['/equipment', equipmentId, 'history']">
              <mat-icon>history</mat-icon> Historial HV
            </button>
            <button mat-raised-button color="primary" [routerLink]="['/equipment', equipmentId, 'edit']">
              <mat-icon>edit</mat-icon> Editar
            </button>
          </div>
        </mat-card>
      } @else {
        <p>Cargando equipo...</p>
      }
    </div>
  `,
  styles: [`
    .detail-page { padding: 24px; max-width: 1000px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 { color: #F1F5F9; margin: 0; }
    .detail-card { padding: 24px; }
    .card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; gap: 12px; }
    .card-header h3 { color: #F1F5F9; margin: 0 0 4px; font-size: 24px; }
    .subtitle { color: #94A3B8; margin: 0; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .status-badge { padding: 6px 16px; border-radius: 16px; font-size: 12px; font-weight: 600; text-transform: uppercase; height: fit-content; }
    .activo, .active { background: rgba(16, 185, 129, 0.2); color: #6EE7B7; }
    .maintenance, .mantenimiento { background: rgba(245, 158, 11, 0.2); color: #FBBF24; }
    .inactive, .inactivo { background: rgba(239, 68, 68, 0.2); color: #FCA5A5; }
    .retired, .retirado { background: rgba(100, 116, 139, 0.2); color: #CBD5E1; }
    .ownership-badge { padding: 3px 10px; border-radius: 10px; font-size: 10px; font-weight: 700; }
    .ownership-badge.owned { background: rgba(59, 130, 246, 0.2); color: #93C5FD; }
    .ownership-badge.rented { background: rgba(139, 92, 246, 0.2); color: #C4B5FD; }
    .section-heading {
      display: flex; align-items: center; gap: 8px;
      color: #94A3B8; font-size: 12px; text-transform: uppercase;
      letter-spacing: 1px; font-weight: 600; margin: 20px 0 12px;
    }
    .section-heading mat-icon { color: #60A5FA; }
    .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; padding: 0 0 20px; }
    .info-item { display: flex; align-items: flex-start; gap: 12px; }
    .info-item mat-icon { color: #60A5FA; margin-top: 4px; }
    .label { display: block; font-size: 11px; color: #94A3B8; text-transform: uppercase; margin-bottom: 4px; letter-spacing: 1px; }
    .value { display: block; color: #E2E8F0; font-weight: 500; display: flex; align-items: center; gap: 8px; }
    .next-critical { color: #F87171 !important; font-weight: 700; }
    .next-warning { color: #FBBF24 !important; font-weight: 700; }
    .next-info { color: #60A5FA !important; }
    .hw-badge { padding: 2px 10px; border-radius: 10px; font-size: 10px; font-weight: 700; }
    .hw-badge.ok { background: rgba(16, 185, 129, 0.2); color: #6EE7B7; }
    .hw-badge.warning { background: rgba(245, 158, 11, 0.2); color: #FBBF24; }
    .hw-badge.critical { background: rgba(239, 68, 68, 0.2); color: #FCA5A5; }
    .link { color: #60A5FA !important; font-weight: 600; text-decoration: none; }
    .link:hover { text-decoration: underline; }
    .status-actions { padding: 20px 0; }
    .status-buttons { display: flex; gap: 12px; flex-wrap: wrap; }
    .status-buttons button { min-width: 140px; }
    .btn-active { color: #10B981 !important; border-color: #10B981 !important; }
    .btn-maintenance { color: #F59E0B !important; border-color: #F59E0B !important; }
    .btn-inactive { color: #EF4444 !important; border-color: #EF4444 !important; }
    .btn-retired { color: #64748B !important; border-color: #64748B !important; }
    .actions { display: flex; gap: 12px; justify-content: flex-end; padding-top: 20px; flex-wrap: wrap; }

    /* Alerta de mantenimiento proximo/vencido */
    .maintenance-alert {
      display: flex; align-items: center; gap: 16px;
      padding: 16px 20px; border-radius: 12px;
      margin: 20px 0;
    }
    .maintenance-alert.critical {
      background: rgba(239, 68, 68, 0.15);
      border-left: 4px solid #EF4444;
    }
    .maintenance-alert.warning {
      background: rgba(245, 158, 11, 0.15);
      border-left: 4px solid #F59E0B;
    }
    .maintenance-alert.info {
      background: rgba(59, 130, 246, 0.15);
      border-left: 4px solid #3B82F6;
    }
    .maintenance-alert mat-icon {
      font-size: 32px; width: 32px; height: 32px;
      flex-shrink: 0;
    }
    .maintenance-alert.critical mat-icon { color: #EF4444; }
    .maintenance-alert.warning mat-icon { color: #F59E0B; }
    .maintenance-alert.info mat-icon { color: #3B82F6; }
    .alert-content { flex: 1; display: flex; flex-direction: column; gap: 4px; }
    .alert-content strong { color: #F1F5F9; font-size: 15px; }
    .alert-sub { color: #CBD5E1; font-size: 13px; }
    .alert-btn { flex-shrink: 0; }

    /* Timeline location history */
    .transfer-header { margin-bottom: 16px; }
    .timeline { position: relative; padding-left: 24px; }
    .timeline::before {
      content: ''; position: absolute; left: 8px; top: 0; bottom: 0;
      width: 2px; background: rgba(148,163,184,0.2);
    }
    .timeline-item { position: relative; margin-bottom: 20px; }
    .timeline-dot {
      position: absolute; left: -20px; top: 4px;
      width: 12px; height: 12px; border-radius: 50%;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      border: 2px solid #0F172A;
    }
    .timeline-content { padding-left: 8px; }
    .timeline-header { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
    .timeline-date { color: #94A3B8; font-size: 12px; font-family: monospace; }
    .timeline-user { color: #64748B; font-size: 12px; }
    .timeline-body { display: flex; align-items: center; gap: 8px; }
    .loc-from { color: #FCA5A5; font-weight: 500; }
    .loc-to { color: #6EE7B7; font-weight: 600; }
    .arrow-icon { color: #60A5FA; font-size: 18px; width: 18px; height: 18px; }
    .timeline-reason { color: #94A3B8; font-size: 13px; margin: 4px 0 0; font-style: italic; }
    .no-transfers { color: #64748B; font-style: italic; text-align: center; padding: 20px; }
  `]
})
export class EquipmentDetailComponent implements OnInit {
  equipment = signal<Equipment | null>(null);
  equipmentId = '';
  showQrCode = signal(false);
  locationHistory = signal<LocationHistory[]>([]);
  qrUrl = '';

  maintenanceStatus = computed<'none' | 'info' | 'warning' | 'critical'>(() => {
    const nextDate = this.equipment()?.nextMaintenanceDate;
    if (!nextDate) return 'none';
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const next = new Date(nextDate);
    next.setHours(0, 0, 0, 0);
    const diffMs = next.getTime() - today.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays < 0) return 'critical';
    if (diffDays === 0) return 'critical';
    if (diffDays <= 7) return 'warning';
    if (diffDays <= 30) return 'info';
    return 'none';
  });

  maintenanceIcon = computed(() => {
    const s = this.maintenanceStatus();
    if (s === 'critical') return 'error';
    if (s === 'warning') return 'warning';
    return 'info';
  });

  maintenanceMessage = computed(() => {
    const nextDate = this.equipment()?.nextMaintenanceDate;
    if (!nextDate) return '';
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const next = new Date(nextDate);
    next.setHours(0, 0, 0, 0);
    const diffMs = next.getTime() - today.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays < 0) return 'Mantenimiento vencido hace ' + Math.abs(diffDays) + ' dias';
    if (diffDays === 0) return 'Mantenimiento programado para HOY';
    if (diffDays === 1) return 'Mantenimiento programado para MAÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¹ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œANA';
    if (diffDays <= 7) return 'Mantenimiento en ' + diffDays + ' dias';
    return 'Mantenimiento en ' + diffDays + ' dias';
  });

  constructor(
    private route: ActivatedRoute,
    private equipmentService: EquipmentService,
    private cdr: ChangeDetectorRef,
    private locationHistoryService: LocationHistoryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.equipmentId = this.route.snapshot.paramMap.get('id') || '';
    this.loadEquipment();
    this.loadLocationHistory();
  }

  loadEquipment() {
    if (this.equipmentId) {
      this.equipmentService.getById(this.equipmentId).subscribe({
        next: (e) => { this.equipment.set(e); this.cdr.detectChanges(); }
      });
    }
  }

  changeStatus(newStatus: string) {
    const reason = prompt('Motivo del cambio de estado:') || 'Cambio manual';
    this.equipmentService.changeStatus(this.equipmentId, newStatus, reason).subscribe({
      next: () => {
        this.snackBar.open('Estado actualizado a: ' + newStatus, 'OK', { duration: 3000 });
        this.loadEquipment();
    this.loadLocationHistory();
      },
      error: (err) => {
        this.snackBar.open('Error: ' + (err.error?.message || 'No se pudo cambiar'), 'OK', { duration: 5000 });
      }
    });
  }

  loadLocationHistory() {
    if (this.equipmentId) {
      this.locationHistoryService.getHistory(this.equipmentId).subscribe({
        next: (h) => { this.locationHistory.set(h); this.cdr.detectChanges(); },
        error: () => this.locationHistory.set([])
      });
    }
  }

  openTransferDialog() {
    const eq = this.equipment();
    const ref = this.dialog.open(TransferDialogComponent, {
      width: '600px',
      data: {
        equipmentId: this.equipmentId,
        equipmentName: eq?.name || '',
        currentBuilding: eq?.location?.building || '',
        currentOffice: eq?.location?.office || ''
      }
    });
    ref.afterClosed().subscribe(result => {
      if (result === 'transferred') {
        this.snackBar.open('Equipo trasladado exitosamente', 'OK', { duration: 3000 });
        this.loadEquipment();
        this.loadLocationHistory();
      }
    });
  }

  showQr() {
    this.qrUrl = environment.apiUrl + '/qr/' + this.equipmentId;
    this.showQrCode.set(!this.showQrCode());
    this.cdr.detectChanges();
  }

  downloadQr() {
    window.open(environment.apiUrl + '/qr/' + this.equipmentId + '/download', '_blank');
  }
}