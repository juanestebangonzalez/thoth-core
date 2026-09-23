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
        <div class="header-actions">
          <button mat-stroked-button (click)="downloadCSV()" class="action-btn">
            <mat-icon>download</mat-icon> Descargar CSV
          </button>
          <button mat-stroked-button (click)="printReport()" class="action-btn">
            <mat-icon>print</mat-icon> Imprimir
          </button>
        </div>
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

        <!-- Planned vs Completed KPIs -->
        @if (data().plannedVsCompleted) {
          <div class="kpi-grid" style="margin-bottom: 24px;">
            <div class="kpi-card teal">
              <mat-icon>event</mat-icon>
              <div class="kpi-value">{{ data().plannedVsCompleted.totalPlanned }}</div>
              <div class="kpi-label">Planeados</div>
            </div>
            <div class="kpi-card green">
              <mat-icon>task_alt</mat-icon>
              <div class="kpi-value">{{ data().plannedVsCompleted.totalCompleted }}</div>
              <div class="kpi-label">Cumplidos</div>
            </div>
            <div class="kpi-card red">
              <mat-icon>warning</mat-icon>
              <div class="kpi-value">{{ data().plannedVsCompleted.totalOverdue }}</div>
              <div class="kpi-label">Vencidos</div>
            </div>
            <div class="kpi-card" [class.green]="data().plannedVsCompleted.complianceRate >= 80" [class.orange]="data().plannedVsCompleted.complianceRate >= 50 && data().plannedVsCompleted.complianceRate < 80" [class.red]="data().plannedVsCompleted.complianceRate < 50">
              <mat-icon>speed</mat-icon>
              <div class="kpi-value">{{ data().plannedVsCompleted.complianceRate }}%</div>
              <div class="kpi-label">Cumplimiento</div>
            </div>
          </div>
        }

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

          <!-- Tab Por Sede -->
          <mat-tab>
            <ng-template mat-tab-label><mat-icon>business</mat-icon>&nbsp;Por Sede</ng-template>
            <div class="tab-content">
              <mat-card class="chart-card">
                <h3>Mantenimientos por Sede</h3>
                @if (objectEntries(data().bySede).length > 0) {
                  <div class="sede-list">
                    @for (entry of objectEntries(data().bySede); track entry[0]) {
                      <div class="sede-row">
                        <div class="sede-header">
                          <mat-icon>location_on</mat-icon>
                          <span class="sede-name">{{ entry[0] }}</span>
                          <span class="sede-total">{{ entry[1].total }} total</span>
                        </div>
                        <div class="sede-bars">
                          <div class="sede-bar-track">
                            <div class="sede-bar-prev" [style.width.%]="getSedePercent(entry[1].preventive, entry[1].total)" title="Preventivos: {{ entry[1].preventive }}"></div>
                            <div class="sede-bar-corr" [style.width.%]="getSedePercent(entry[1].corrective, entry[1].total)" title="Correctivos: {{ entry[1].corrective }}"></div>
                          </div>
                          <div class="sede-counts">
                            <span class="badge prev">{{ entry[1].preventive }} prev</span>
                            <span class="badge corr">{{ entry[1].corrective }} corr</span>
                          </div>
                        </div>
                      </div>
                    }
                  </div>
                  <div class="legend" style="margin-top:16px;">
                    <span class="legend-item"><span class="ldot prev"></span> Preventivo</span>
                    <span class="legend-item"><span class="ldot corr"></span> Correctivo</span>
                  </div>
                } @else {
                  <p class="empty-msg">No hay datos de mantenimiento por sede</p>
                }
              </mat-card>

              <!-- Cumplimiento por Sede -->
              @if (data().plannedVsCompleted?.bySede) {
                <mat-card class="table-card">
                  <h3>Cumplimiento por Sede</h3>
                  <table>
                    <thead>
                      <tr><th>Sede</th><th>Planeados</th><th>Cumplidos</th><th>Vencidos</th><th>Cumplimiento</th></tr>
                    </thead>
                    <tbody>
                      @for (entry of objectEntries(data().plannedVsCompleted.bySede); track entry[0]) {
                        <tr>
                          <td><strong>{{ entry[0] }}</strong></td>
                          <td>{{ entry[1].planned }}</td>
                          <td><span class="badge green-badge">{{ entry[1].completed }}</span></td>
                          <td><span class="badge red-badge">{{ entry[1].overdue }}</span></td>
                          <td>
                            <span class="compliance-chip" [class.good]="getComplianceRate(entry[1]) >= 80" [class.warn]="getComplianceRate(entry[1]) >= 50 && getComplianceRate(entry[1]) < 80" [class.bad]="getComplianceRate(entry[1]) < 50">
                              {{ getComplianceRate(entry[1]) }}%
                            </span>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </mat-card>
              }
            </div>
          </mat-tab>

          <!-- Tab Cumplimiento -->
          @if (data().plannedVsCompleted?.byMonth) {
            <mat-tab>
              <ng-template mat-tab-label><mat-icon>task_alt</mat-icon>&nbsp;Cumplimiento</ng-template>
              <div class="tab-content">
                <mat-card class="chart-card">
                  <h3>Planeados vs Cumplidos por Mes</h3>
                  <div class="chart-bars">
                    @for (m of data().plannedVsCompleted.byMonth; track m.month) {
                      <div class="chart-col">
                        <div class="bar-stack">
                          <div class="bar planned" [style.height.px]="m.planned * 30 || 2" title="Planeados: {{m.planned}}"></div>
                          <div class="bar completed" [style.height.px]="m.completed * 30 || 2" title="Cumplidos: {{m.completed}}"></div>
                        </div>
                        <span class="bar-total">{{ m.planned }}/{{ m.completed }}</span>
                        <span class="bar-label">{{ m.label }}</span>
                      </div>
                    }
                  </div>
                  <div class="legend">
                    <span class="legend-item"><span class="ldot planned"></span> Planeados</span>
                    <span class="legend-item"><span class="ldot completed"></span> Cumplidos</span>
                  </div>
                </mat-card>

                <mat-card class="table-card">
                  <h3>Detalle Mensual de Cumplimiento</h3>
                  <table>
                    <thead>
                      <tr><th>Mes</th><th>Planeados</th><th>Cumplidos</th><th>Cumplimiento</th></tr>
                    </thead>
                    <tbody>
                      @for (m of data().plannedVsCompleted.byMonth; track m.month) {
                        <tr [class.has-data]="m.planned > 0">
                          <td>{{ m.label }}</td>
                          <td>{{ m.planned }}</td>
                          <td><span class="badge green-badge">{{ m.completed }}</span></td>
                          <td>
                            <span class="compliance-chip" [class.good]="m.planned > 0 && (m.completed/m.planned*100) >= 80" [class.warn]="m.planned > 0 && (m.completed/m.planned*100) >= 50 && (m.completed/m.planned*100) < 80" [class.bad]="m.planned > 0 && (m.completed/m.planned*100) < 50">
                              {{ m.planned > 0 ? (m.completed/m.planned*100).toFixed(0) : '-' }}%
                            </span>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </mat-card>
              </div>
            </mat-tab>
          }

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
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
    .header-actions { display: flex; gap: 8px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; overflow: hidden; }
    .action-btn { color: #94A3B8 !important; border-color: #475569 !important; }
    .action-btn mat-icon { font-size: 18px; width: 18px; height: 18px; overflow: hidden; }
    .loading { text-align: center; color: #94A3B8; padding: 60px; }
    .empty-msg { text-align: center; color: #64748B; padding: 40px; }

    .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 20px; border-radius: 14px; text-align: center; }
    .kpi-card mat-icon { font-size: 28px; width: 28px; height: 28px; overflow: hidden; color: #94A3B8; display: block; margin: 0 auto 8px; }
    .kpi-value { font-size: 32px; font-weight: 700; color: #F1F5F9; }
    .kpi-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.orange { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); }
    .kpi-card.purple { background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.2); }
    .kpi-card.teal { background: rgba(20,184,166,0.1); border: 1px solid rgba(20,184,166,0.2); }
    .kpi-card.red { background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.2); }

    .tab-content { padding: 16px 0; }
    .chart-card, .table-card { padding: 24px; margin-bottom: 16px; }
    .chart-card h3, .table-card h3 { color: #E2E8F0; font-size: 16px; margin: 0 0 20px; }

    .chart-bars { display: flex; gap: 12px; justify-content: center; align-items: flex-end; min-height: 150px; padding: 20px 0; }
    .chart-col { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .bar-stack { display: flex; gap: 3px; align-items: flex-end; }
    .bar { width: 18px; border-radius: 3px 3px 0 0; min-height: 2px; }
    .bar.prev { background: linear-gradient(180deg, #3B82F6, #1E40AF); }
    .bar.corr { background: linear-gradient(180deg, #F59E0B, #D97706); }
    .bar.planned { background: linear-gradient(180deg, #94A3B8, #64748B); }
    .bar.completed { background: linear-gradient(180deg, #10B981, #059669); }
    .bar-total { color: #F1F5F9; font-weight: 700; font-size: 14px; }
    .bar-label { color: #94A3B8; font-size: 10px; text-align: center; max-width: 60px; }
    .legend { display: flex; gap: 20px; justify-content: center; margin-top: 12px; }
    .legend-item { display: flex; align-items: center; gap: 6px; color: #94A3B8; font-size: 12px; }
    .ldot { width: 12px; height: 12px; border-radius: 3px; }
    .ldot.prev { background: #3B82F6; }
    .ldot.corr { background: #F59E0B; }
    .ldot.planned { background: #94A3B8; }
    .ldot.completed { background: #10B981; }

    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(148,163,184,0.15); }
    td { padding: 10px 12px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
    tr.has-data { background: rgba(59,130,246,0.03); }
    .reason-cell { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .badge { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; }
    .badge.prev { background: rgba(59,130,246,0.15); color: #93C5FD; }
    .badge.corr { background: rgba(245,158,11,0.15); color: #FBBF24; }
    .badge.green-badge { background: rgba(16,185,129,0.15); color: #6EE7B7; }
    .badge.red-badge { background: rgba(239,68,68,0.15); color: #FCA5A5; }

    .compliance-chip { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 700; }
    .compliance-chip.good { background: rgba(16,185,129,0.15); color: #6EE7B7; }
    .compliance-chip.warn { background: rgba(245,158,11,0.15); color: #FBBF24; }
    .compliance-chip.bad { background: rgba(239,68,68,0.15); color: #FCA5A5; }

    .sede-list { display: flex; flex-direction: column; gap: 16px; }
    .sede-row { display: flex; flex-direction: column; gap: 8px; }
    .sede-header { display: flex; align-items: center; gap: 8px; }
    .sede-header mat-icon { color: #60A5FA; font-size: 20px; width: 20px; height: 20px; overflow: hidden; }
    .sede-name { color: #E2E8F0; font-weight: 600; font-size: 15px; flex: 1; }
    .sede-total { color: #94A3B8; font-size: 13px; }
    .sede-bars { display: flex; align-items: center; gap: 12px; }
    .sede-bar-track { flex: 1; height: 24px; background: rgba(15,23,42,0.5); border-radius: 6px; overflow: hidden; display: flex; }
    .sede-bar-prev { height: 100%; background: linear-gradient(90deg, #3B82F6, #60A5FA); }
    .sede-bar-corr { height: 100%; background: linear-gradient(90deg, #F59E0B, #FBBF24); }
    .sede-counts { display: flex; gap: 8px; flex-shrink: 0; }

    .tech-list { display: flex; flex-direction: column; gap: 12px; }
    .tech-row { display: flex; align-items: center; gap: 14px; }
    .tech-avatar { width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, #3B82F6, #8B5CF6); display: flex; align-items: center; justify-content: center; color: white; font-weight: 700; font-size: 14px; flex-shrink: 0; }
    .tech-name { width: 150px; color: #E2E8F0; font-weight: 500; font-size: 14px; flex-shrink: 0; }
    .tech-bar-track { flex: 1; height: 20px; background: rgba(15,23,42,0.5); border-radius: 4px; overflow: hidden; }
    .tech-bar-fill { height: 100%; background: linear-gradient(90deg, #3B82F6, #60A5FA); border-radius: 4px; min-width: 4px; }
    .tech-count { color: #F1F5F9; font-weight: 700; font-size: 16px; width: 40px; text-align: right; }

    @media print {
      .header-actions { display: none !important; }
    }
    @media (max-width: 768px) {
      .report-page { padding: 16px; }
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .chart-bars { overflow-x: auto; }
      .tech-name { width: 100px; }
      .sede-counts { flex-direction: column; gap: 4px; }
      h1 { font-size: 20px; }
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

  objectEntries(obj: any): [string, any][] {
    if (!obj) return [];
    return (Object.entries(obj) as [string, any][]).sort((a, b) => {
      const aVal = typeof a[1] === 'number' ? a[1] : (a[1]?.total || 0);
      const bVal = typeof b[1] === 'number' ? b[1] : (b[1]?.total || 0);
      return bVal - aVal;
    });
  }

  getPercent(value: number): number {
    const max = Math.max(...Object.values(this.data()?.byTechnician || { x: 1 }) as number[]);
    return Math.max(5, (value / max) * 100);
  }

  getSedePercent(value: number, total: number): number {
    return total > 0 ? (value / total) * 100 : 0;
  }

  getComplianceRate(sedeData: any): number {
    if (!sedeData || !sedeData.planned || sedeData.planned === 0) return 0;
    return Math.round((sedeData.completed / sedeData.planned) * 100);
  }

  downloadCSV() {
    const d = this.data();
    if (!d) return;

    let csv = 'INFORME DE MANTENIMIENTOS - THOTH C.O.R.E.\n\n';

    // Resumen
    csv += 'RESUMEN GENERAL\n';
    csv += 'Total,Preventivos,Correctivos,% Preventivos\n';
    csv += `${d.total},${d.totalPreventive},${d.totalCorrective},${d.total > 0 ? ((d.totalPreventive / d.total) * 100).toFixed(1) : 0}%\n\n`;

    // Cumplimiento
    if (d.plannedVsCompleted) {
      csv += 'CUMPLIMIENTO\n';
      csv += 'Planeados,Cumplidos,Vencidos,Tasa Cumplimiento\n';
      csv += `${d.plannedVsCompleted.totalPlanned},${d.plannedVsCompleted.totalCompleted},${d.plannedVsCompleted.totalOverdue},${d.plannedVsCompleted.complianceRate}%\n\n`;
    }

    // Por mes
    csv += 'DETALLE MENSUAL\n';
    csv += 'Mes,Preventivos,Correctivos,Total\n';
    for (const m of d.byMonth || []) {
      csv += `${m.label},${m.preventive},${m.corrective},${m.total}\n`;
    }
    csv += '\n';

    // Por sede
    if (d.bySede) {
      csv += 'POR SEDE\n';
      csv += 'Sede,Preventivos,Correctivos,Total\n';
      for (const [sede, counts] of this.objectEntries(d.bySede)) {
        csv += `${sede},${(counts as any).preventive},${(counts as any).corrective},${(counts as any).total}\n`;
      }
      csv += '\n';
    }

    // Cumplimiento por sede
    if (d.plannedVsCompleted?.bySede) {
      csv += 'CUMPLIMIENTO POR SEDE\n';
      csv += 'Sede,Planeados,Cumplidos,Vencidos,Cumplimiento\n';
      for (const [sede, counts] of this.objectEntries(d.plannedVsCompleted.bySede)) {
        const c = counts as any;
        csv += `${sede},${c.planned},${c.completed},${c.overdue},${this.getComplianceRate(c)}%\n`;
      }
      csv += '\n';
    }

    // Por tecnico
    csv += 'POR TECNICO\n';
    csv += 'Tecnico,Cantidad\n';
    for (const [tech, count] of this.objectEntries(d.byTechnician)) {
      csv += `${tech},${count}\n`;
    }

    // Download
    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const today = new Date().toISOString().slice(0, 10);
    a.download = `Informe_Mantenimiento_${today}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  printReport() { window.print(); }
}
