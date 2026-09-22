import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-maintenance-report',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatTabsModule],
  template: `
    <div class="report-page">
      <div class="header">
        <h1><mat-icon>assessment</mat-icon> Informe de Mantenimientos</h1>
        <button mat-stroked-button (click)="printReport()" class="print-btn">
          <mat-icon>print</mat-icon> Imprimir
        </button>
      </div>

      @if (data()) {
        <!-- KPIs -->
        <div class="kpi-grid">
          <div class="kpi-card blue">
            <mat-icon>build</mat-icon>
            <div class="kpi-value">{{ data().total }}</div>
            <div class="kpi-label">Total</div>
          </div>
          <div class="kpi-card green">
            <mat-icon>shield</mat-icon>
            <div class="kpi-value">{{ data().totalPreventive }}</div>
            <div class="kpi-label">Preventivos</div>
          </div>
          <div class="kpi-card orange">
            <mat-icon>handyman</mat-icon>
            <div class="kpi-value">{{ data().totalCorrective }}</div>
            <div class="kpi-label">Correctivos</div>
          </div>
          <div class="kpi-card purple">
            <mat-icon>percent</mat-icon>
            <div class="kpi-value">{{ data().total > 0 ? ((data().totalPreventive / data().total) * 100).toFixed(0) : 0 }}%</div>
            <div class="kpi-label">Preventivos</div>
          </div>
        </div>

        <mat-tab-group animationDuration="300ms">
          <!-- Tab Semanal -->
          <mat-tab>
            <ng-template mat-tab-label><mat-icon>view_week</mat-icon>&nbsp;Semanal</ng-template>
            <div class="tab-content">
              <mat-card class="chart-card">
                <h3>Mantenimientos por Semana (ultimas 8 semanas)</h3>
                <div class="chart-bars">
                  @for (w of data().byWeek; track w.weekStart) {
                    <div class="chart-col">
                      <div class="bar-stack">
                        <div class="bar prev" [style.height.px]="w.preventive * 30 || 2" title="Preventivos: {{w.preventive}}"></div>
                        <div class="bar corr" [style.height.px]="w.corrective * 30 || 2" title="Correctivos: {{w.corrective}}"></div>
                      </div>
                      <span class="bar-total">{{ w.total }}</span>
                      <span class="bar-label">{{ w.label }}</span>
                    </div>
                  }
                </div>
                <div class="legend">
                  <span class="legend-item"><span class="ldot prev"></span> Preventivo</span>
                  <span class="legend-item"><span class="ldot corr"></span> Correctivo</span>
                </div>
              </mat-card>

              <mat-card class="table-card">
                <h3>Detalle Semanal</h3>
                <table>
                  <thead>
                    <tr><th>Semana</th><th>Preventivos</th><th>Correctivos</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                    @for (w of data().byWeek; track w.weekStart) {
                      <tr [class.has-data]="w.total > 0">
                        <td>{{ w.label }}</td>
                        <td><span class="badge prev">{{ w.preventive }}</span></td>
                        <td><span class="badge corr">{{ w.corrective }}</span></td>
                        <td><strong>{{ w.total }}</strong></td>
                      </tr>
                    }
                  </tbody>
                </table>
              </mat-card>
            </div>
          </mat-tab>

          <!-- Tab Mensual -->
          <mat-tab>
            <ng-template mat-tab-label><mat-icon>calendar_month</mat-icon>&nbsp;Mensual</ng-template>
            <div class="tab-content">
              <mat-card class="chart-card">
                <h3>Mantenimientos por Mes (ultimos 12 meses)</h3>
                <div class="chart-bars">
                  @for (m of data().byMonth; track m.month) {
                    <div class="chart-col">
                      <div class="bar-stack">
                        <div class="bar prev" [style.height.px]="m.preventive * 30 || 2" title="Preventivos: {{m.preventive}}"></div>
                        <div class="bar corr" [style.height.px]="m.corrective * 30 || 2" title="Correctivos: {{m.corrective}}"></div>
                      </div>
                      <span class="bar-total">{{ m.total }}</span>
                      <span class="bar-label">{{ m.label }}</span>
                    </div>
                  }
                </div>
                <div class="legend">
                  <span class="legend-item"><span class="ldot prev"></span> Preventivo</span>
                  <span class="legend-item"><span class="ldot corr"></span> Correctivo</span>
                </div>
              </mat-card>

              <mat-card class="table-card">
                <h3>Detalle Mensual</h3>
                <table>
                  <thead>
                    <tr><th>Mes</th><th>Preventivos</th><th>Correctivos</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                    @for (m of data().byMonth; track m.month) {
                      <tr [class.has-data]="m.total > 0">
                        <td>{{ m.label }}</td>
                        <td><span class="badge prev">{{ m.preventive }}</span></td>
                        <td><span class="badge corr">{{ m.corrective }}</span></td>
                        <td><strong>{{ m.total }}</strong></td>
                      </tr>
                    }
                  </tbody>
                </table>
              </mat-card>
            </div>
          </mat-tab>

          <!-- Tab Tecnicos -->
          <mat-tab>
            <ng-template mat-tab-label><mat-icon>people</mat-icon>&nbsp;Por Tecnico</ng-template>
            <div class="tab-content">
              <mat-card class="chart-card">
                <h3>Mantenimientos por Tecnico</h3>
                <div class="tech-list">
                  @for (entry of objectEntries(data().byTechnician); track entry[0]) {
                    <div class="tech-row">
                      <div class="tech-avatar">{{ entry[0].charAt(0).toUpperCase() }}</div>
                      <span class="tech-name">{{ entry[0] }}</span>
                      <div class="tech-bar-track">
                        <div class="tech-bar-fill" [style.width.%]="getPercent(entry[1])"></div>
                      </div>
                      <span class="tech-count">{{ entry[1] }}</span>
                    </div>
                  }
                </div>
              </mat-card>
            </div>
          </mat-tab>

          <!-- Tab Recientes -->
          <mat-tab>
            <ng-template mat-tab-label><mat-icon>history</mat-icon>&nbsp;Recientes</ng-template>
            <div class="tab-content">
              <mat-card class="table-card">
                <h3>Ultimos 10 Mantenimientos</h3>
                <table>
                  <thead>
                    <tr><th>Fecha</th><th>Tipo</th><th>Tecnico</th><th>Motivo</th><th>Firma</th></tr>
                  </thead>
                  <tbody>
                    @for (m of data().recent; track m.id) {
                      <tr>
                        <td>{{ m.date | date:'dd/MM/yyyy HH:mm' }}</td>
                        <td><span class="badge" [class.prev]="m.type === 'PREVENTIVE'" [class.corr]="m.type === 'CORRECTIVE'">{{ m.type === 'PREVENTIVE' ? 'PREV' : 'CORR' }}</span></td>
                        <td>{{ m.technician }}</td>
                        <td class="reason-cell">{{ m.reason }}</td>
                        <td>
                          @if (m.hasSig) {
                            <mat-icon style="color:#10B981;">verified</mat-icon>
                          } @else {
                            <mat-icon style="color:#64748B;">remove</mat-icon>
                          }
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </mat-card>
            </div>
          </mat-tab>
        </mat-tab-group>
      } @else {
        <p class="loading">Cargando datos...</p>
      }
    </div>
  `,
  styles: [`
    .report-page { padding: 32px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .print-btn { color: #94A3B8 !important; border-color: #475569 !important; }
    .loading { text-align: center; color: #94A3B8; padding: 60px; }

    .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 20px; border-radius: 14px; text-align: center; }
    .kpi-card mat-icon { font-size: 28px; width: 28px; height: 28px; color: #94A3B8; display: block; margin: 0 auto 8px; }
    .kpi-value { font-size: 32px; font-weight: 700; color: #F1F5F9; }
    .kpi-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.orange { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); }
    .kpi-card.purple { background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.2); }

    .tab-content { padding: 16px 0; }
    .chart-card, .table-card { padding: 24px; margin-bottom: 16px; }
    .chart-card h3, .table-card h3 { color: #E2E8F0; font-size: 16px; margin: 0 0 20px; }

    .chart-bars { display: flex; gap: 12px; justify-content: center; align-items: flex-end; min-height: 150px; padding: 20px 0; }
    .chart-col { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .bar-stack { display: flex; gap: 3px; align-items: flex-end; }
    .bar { width: 18px; border-radius: 3px 3px 0 0; min-height: 2px; }
    .bar.prev { background: linear-gradient(180deg, #3B82F6, #1E40AF); }
    .bar.corr { background: linear-gradient(180deg, #F59E0B, #D97706); }
    .bar-total { color: #F1F5F9; font-weight: 700; font-size: 14px; }
    .bar-label { color: #94A3B8; font-size: 10px; text-align: center; max-width: 60px; }
    .legend { display: flex; gap: 20px; justify-content: center; margin-top: 12px; }
    .legend-item { display: flex; align-items: center; gap: 6px; color: #94A3B8; font-size: 12px; }
    .ldot { width: 12px; height: 12px; border-radius: 3px; }
    .ldot.prev { background: #3B82F6; }
    .ldot.corr { background: #F59E0B; }

    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(148,163,184,0.15); }
    td { padding: 10px 12px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
    tr.has-data { background: rgba(59,130,246,0.03); }
    .reason-cell { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .badge { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; }
    .badge.prev { background: rgba(59,130,246,0.15); color: #93C5FD; }
    .badge.corr { background: rgba(245,158,11,0.15); color: #FBBF24; }

    .tech-list { display: flex; flex-direction: column; gap: 12px; }
    .tech-row { display: flex; align-items: center; gap: 14px; }
    .tech-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #3B82F6, #8B5CF6); display: flex; align-items: center; justify-content: center; color: white; font-weight: 700; font-size: 14px; flex-shrink: 0; }
    .tech-name { width: 150px; color: #E2E8F0; font-weight: 500; font-size: 14px; flex-shrink: 0; }
    .tech-bar-track { flex: 1; height: 20px; background: rgba(15,23,42,0.5); border-radius: 4px; overflow: hidden; }
    .tech-bar-fill { height: 100%; background: linear-gradient(90deg, #3B82F6, #60A5FA); border-radius: 4px; min-width: 4px; }
    .tech-count { color: #F1F5F9; font-weight: 700; font-size: 16px; width: 40px; text-align: right; }

    @media print { .header button { display: none !important; } }
    @media (max-width: 768px) {
      .report-page { padding: 16px; }
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .chart-bars { overflow-x: auto; }
      .tech-name { width: 100px; }
    }
  `]
})
export class MaintenanceReportComponent implements OnInit {
  data = signal<any>(null);

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.http.get<any>(`${environment.apiUrl}/reports/maintenance-report`).subscribe({
      next: (d) => { this.data.set(d); this.cdr.detectChanges(); }
    });
  }

  objectEntries(obj: any): [string, number][] {
    return obj ? (Object.entries(obj) as [string, number][]).sort((a, b) => b[1] - a[1]) : [];
  }

  getPercent(value: number): number {
    const max = Math.max(...Object.values(this.data()?.byTechnician || { x: 1 }) as number[]);
    return Math.max(5, (value / max) * 100);
  }

  printReport() { window.print(); }
}