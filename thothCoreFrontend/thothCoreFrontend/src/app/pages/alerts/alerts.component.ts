import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatBadgeModule } from '@angular/material/badge';
import { AlertService, MaintenanceAlert, HardwareAlert, RentalAlert, AlertSummary } from '../../core/services/alert.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule, MatTabsModule, MatBadgeModule],
  template: `
    <div class="alerts-page">
      <div class="header">
        <div>
          <h1><mat-icon>notifications_active</mat-icon> Centro de Alertas</h1>
          <p>Monitoreo en tiempo real de todos los equipos</p>
        </div>
        <button mat-stroked-button (click)="refresh()">
          <mat-icon>refresh</mat-icon> Actualizar
        </button>
      </div>

      <!-- Summary Cards -->
      <div class="summary-grid">
        <div class="summary-card" [class.has-alerts]="summary()?.upcomingMaintenance">
          <mat-icon>build</mat-icon>
          <span class="summary-number">{{ summary()?.upcomingMaintenance || 0 }}</span>
          <span class="summary-label">Mantenimientos</span>
        </div>
        <div class="summary-card" [class.has-alerts]="summary()?.hardwareCritical">
          <mat-icon>memory</mat-icon>
          <span class="summary-number">{{ summary()?.hardwareCritical || 0 }}</span>
          <span class="summary-label">Hardware Critico</span>
        </div>
        <div class="summary-card" [class.has-alerts]="summary()?.rentalExpiring">
          <mat-icon>description</mat-icon>
          <span class="summary-number">{{ summary()?.rentalExpiring || 0 }}</span>
          <span class="summary-label">Contratos</span>
        </div>
        <div class="summary-card total">
          <mat-icon>warning</mat-icon>
          <span class="summary-number">{{ summary()?.total || 0 }}</span>
          <span class="summary-label">Total Alertas</span>
        </div>
      </div>

      <mat-tab-group animationDuration="300ms" (selectedTabChange)="onTabChange($event)">
        <!-- Tab Mantenimientos -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>build</mat-icon>&nbsp;Mantenimientos
            @if (maintenanceAlerts().length > 0) {
              <span class="tab-badge orange">{{ maintenanceAlerts().length }}</span>
            }
          </ng-template>
          <div class="tab-content">
            @if (maintenanceAlerts().length === 0) {
              <div class="empty"><mat-icon>check_circle</mat-icon><p>Sin mantenimientos pendientes</p></div>
            } @else {
              @for (alert of maintenanceAlerts(); track alert.equipmentId) {
                <a class="alert-row" [routerLink]="['/equipment', alert.equipmentId]">
                  <div class="alert-icon" [class.sev-critical]="alert.severity === 'CRITICAL'"
                       [class.sev-high]="alert.severity === 'HIGH'" [class.sev-medium]="alert.severity === 'MEDIUM'">
                    <mat-icon>{{ alert.severity === 'CRITICAL' ? 'error' : (alert.severity === 'HIGH' ? 'warning' : 'info') }}</mat-icon>
                  </div>
                  <div class="alert-body">
                    <span class="alert-title">{{ alert.name }}</span>
                    <span class="alert-sub">{{ alert.category }} | Serial: {{ alert.serialNumber }}</span>
                    <span class="alert-msg">{{ alert.message }}</span>
                  </div>
                  <div class="alert-right">
                    <span class="days-badge" [class.sev-critical]="alert.daysUntil < 0"
                          [class.sev-high]="alert.daysUntil >= 0 && alert.daysUntil <= 3"
                          [class.sev-medium]="alert.daysUntil > 3">
                      {{ alert.daysUntil < 0 ? 'VENCIDO' : (alert.daysUntil === 0 ? 'HOY' : alert.daysUntil + ' dias') }}
                    </span>
                    <span class="alert-date">{{ alert.nextMaintenanceDate | date:'dd/MM/yyyy' }}</span>
                  </div>
                </a>
              }
            }
          </div>
        </mat-tab>

        <!-- Tab Hardware -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>memory</mat-icon>&nbsp;Hardware
            @if (hardwareAlerts().length > 0) {
              <span class="tab-badge red">{{ hardwareAlerts().length }}</span>
            }
          </ng-template>
          <div class="tab-content">
            @if (hardwareAlerts().length === 0) {
              <div class="empty"><mat-icon>check_circle</mat-icon><p>Ningun equipo con hardware critico</p></div>
            } @else {
              @for (alert of hardwareAlerts(); track alert.equipmentId) {
                <a class="alert-row" [routerLink]="['/equipment', alert.equipmentId]">
                  <div class="alert-icon sev-critical"><mat-icon>error</mat-icon></div>
                  <div class="alert-body">
                    <span class="alert-title">{{ alert.name }}</span>
                    <span class="alert-sub">{{ alert.category }} | Serial: {{ alert.serialNumber }}</span>
                    <div class="issues-row">
                      @for (issue of alert.issues; track issue) {
                        <span class="issue-chip">{{ issue }}</span>
                      }
                    </div>
                  </div>
                  <div class="alert-right">
                    <span class="days-badge sev-critical">CRITICO</span>
                  </div>
                </a>
              }
            }
          </div>
        </mat-tab>

        <!-- Tab Contratos -->
        <mat-tab>
          <ng-template mat-tab-label>
            <mat-icon>description</mat-icon>&nbsp;Contratos
            @if (rentalAlerts().length > 0) {
              <span class="tab-badge purple">{{ rentalAlerts().length }}</span>
            }
          </ng-template>
          <div class="tab-content">
            @if (rentalAlerts().length === 0) {
              <div class="empty"><mat-icon>check_circle</mat-icon><p>Ningun contrato por vencer</p></div>
            } @else {
              @for (alert of rentalAlerts(); track alert.equipmentId) {
                <a class="alert-row" [routerLink]="['/equipment', alert.equipmentId]">
                  <div class="alert-icon" [class.sev-critical]="alert.severity === 'CRITICAL'"
                       [class.sev-high]="alert.severity === 'HIGH'" [class.sev-medium]="alert.severity === 'MEDIUM'">
                    <mat-icon>{{ alert.severity === 'CRITICAL' ? 'error' : 'schedule' }}</mat-icon>
                  </div>
                  <div class="alert-body">
                    <span class="alert-title">{{ alert.name }}</span>
                    <span class="alert-sub">Empresa: {{ alert.rentalCompany }}</span>
                    <span class="alert-msg">{{ alert.message }}</span>
                  </div>
                  <div class="alert-right">
                    <span class="days-badge" [class.sev-critical]="alert.daysUntil < 0"
                          [class.sev-high]="alert.daysUntil >= 0 && alert.daysUntil <= 7"
                          [class.sev-medium]="alert.daysUntil > 7">
                      {{ alert.daysUntil < 0 ? 'VENCIDO' : alert.daysUntil + ' dias' }}
                    </span>
                    <span class="alert-date">Vence: {{ alert.rentalEndDate | date:'dd/MM/yyyy' }}</span>
                  </div>
                </a>
              }
            }
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .alerts-page { padding: 32px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #F59E0B, #EF4444); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .header p { color: #94A3B8; margin: 4px 0 0; }

    .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .summary-card { padding: 20px; border-radius: 14px; text-align: center; background: rgba(30,41,59,0.7); border: 1px solid rgba(148,163,184,0.1); transition: all 0.3s; }
    .summary-card mat-icon { font-size: 28px; width: 28px; height: 28px; color: #64748B; display: block; margin: 0 auto 8px; }
    .summary-number { display: block; font-size: 36px; font-weight: 700; color: #64748B; }
    .summary-label { display: block; color: #64748B; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .summary-card.has-alerts { border-color: rgba(245,158,11,0.4); }
    .summary-card.has-alerts mat-icon { color: #FBBF24; }
    .summary-card.has-alerts .summary-number { color: #FBBF24; }
    .summary-card.has-alerts .summary-label { color: #FBBF24; }
    .summary-card.total { border-color: rgba(239,68,68,0.4); }
    .summary-card.total mat-icon { color: #EF4444; }
    .summary-card.total .summary-number { color: #EF4444; }
    .summary-card.total .summary-label { color: #FCA5A5; }

    .tab-badge { padding: 2px 8px; border-radius: 10px; font-size: 11px; font-weight: 700; margin-left: 8px; }
    .tab-badge.orange { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .tab-badge.red { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .tab-badge.purple { background: rgba(139,92,246,0.2); color: #C4B5FD; }

    .tab-content { padding: 16px 0; }
    .empty { text-align: center; padding: 60px 20px; }
    .empty mat-icon { font-size: 64px; width: 64px; height: 64px; color: #10B981; }
    .empty p { color: #94A3B8; margin-top: 12px; font-size: 16px; }

    .alert-row { display: flex; align-items: center; gap: 16px; padding: 16px 20px; border-radius: 12px; background: rgba(30,41,59,0.5); border: 1px solid rgba(148,163,184,0.08); margin-bottom: 8px; text-decoration: none; cursor: pointer; transition: all 0.2s; }
    .alert-row:hover { background: rgba(59,130,246,0.1); border-color: rgba(59,130,246,0.3); transform: translateX(4px); }
    .alert-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .alert-icon mat-icon { color: white; font-size: 22px; width: 22px; height: 22px; }
    .alert-icon.sev-critical { background: linear-gradient(135deg, #EF4444, #B91C1C); }
    .alert-icon.sev-high { background: linear-gradient(135deg, #F59E0B, #D97706); }
    .alert-icon.sev-medium { background: linear-gradient(135deg, #3B82F6, #1E40AF); }
    .alert-body { flex: 1; min-width: 0; }
    .alert-title { display: block; color: #F1F5F9; font-weight: 600; font-size: 15px; }
    .alert-sub { display: block; color: #64748B; font-size: 12px; margin-top: 2px; }
    .alert-msg { display: block; color: #94A3B8; font-size: 13px; margin-top: 4px; }
    .issues-row { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px; }
    .issue-chip { padding: 3px 10px; border-radius: 8px; background: rgba(239,68,68,0.15); color: #FCA5A5; font-size: 11px; font-weight: 600; }
    .alert-right { text-align: right; flex-shrink: 0; }
    .days-badge { display: block; padding: 4px 14px; border-radius: 10px; font-size: 12px; font-weight: 700; }
    .days-badge.sev-critical { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .days-badge.sev-high { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .days-badge.sev-medium { background: rgba(59,130,246,0.2); color: #93C5FD; }
    .alert-date { display: block; color: #64748B; font-size: 11px; margin-top: 4px; }

    @media (max-width: 768px) {
      .alerts-page { padding: 16px; }
      .summary-grid { grid-template-columns: repeat(2, 1fr); }
      .alert-row { flex-wrap: wrap; }
      .alert-right { width: 100%; text-align: left; margin-top: 8px; display: flex; gap: 12px; align-items: center; }
    }
  `]
})
export class AlertsComponent implements OnInit {
  summary = signal<AlertSummary | null>(null);
  maintenanceAlerts = signal<MaintenanceAlert[]>([]);
  hardwareAlerts = signal<HardwareAlert[]>([]);
  rentalAlerts = signal<RentalAlert[]>([]);

  constructor(private alertService: AlertService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.loadAll(); }

  loadAll() {
    this.alertService.getSummary().subscribe({ next: (s) => { this.summary.set(s); this.cdr.detectChanges(); } });
    this.alertService.getUpcomingMaintenance().subscribe({ next: (a) => { this.maintenanceAlerts.set(a); this.cdr.detectChanges(); } });
    this.alertService.getHardwareCritical().subscribe({ next: (a) => { this.hardwareAlerts.set(a); this.cdr.detectChanges(); } });
    this.alertService.getRentalExpiring().subscribe({ next: (a) => { this.rentalAlerts.set(a); this.cdr.detectChanges(); } });
  }

  refresh() { this.loadAll(); }
  onTabChange(e: any) {}
}