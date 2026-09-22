import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportService } from '../../core/services/report.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatDividerModule, MatProgressSpinnerModule],
  template: `
    <div class="reports-page">
      <div class="header">
        <div>
          <h1><mat-icon>analytics</mat-icon> Reportes Gerenciales</h1>
          <p>THOTH C.O.R.E - Panel Ejecutivo</p>
        </div>
        <button mat-stroked-button (click)="printReport()" class="print-btn">
          <mat-icon>print</mat-icon> Imprimir
        </button>
      </div>

      @if (loading()) {
        <div class="loading"><mat-spinner diameter="40"></mat-spinner><p>Cargando datos...</p></div>
      } @else if (data()) {
        <!-- KPIs -->
        <div class="kpi-grid">
          <div class="kpi-card blue">
            <mat-icon>computer</mat-icon>
            <div class="kpi-value">{{ data().kpis.totalEquipos }}</div>
            <div class="kpi-label">Total Equipos</div>
          </div>
          <div class="kpi-card green">
            <mat-icon>attach_money</mat-icon>
            <div class="kpi-value">{{ formatMoney(data().kpis.valorTotalInventario) }}</div>
            <div class="kpi-label">Valor Inventario</div>
          </div>
          <div class="kpi-card purple">
            <mat-icon>schedule</mat-icon>
            <div class="kpi-value">{{ data().kpis.edadPromedioAnos }} a</div>
            <div class="kpi-label">Edad Promedio</div>
          </div>
          <div class="kpi-card orange">
            <mat-icon>build</mat-icon>
            <div class="kpi-value">{{ data().kpis.totalMantenimientos }}</div>
            <div class="kpi-label">Mantenimientos</div>
          </div>
        </div>

        <!-- Charts Row 1 -->
        <div class="charts-row">
          <!-- Por Estado -->
          <mat-card class="chart-card">
            <h3>Estado de Equipos</h3>
            <div class="donut-chart-container">
              @for (entry of objectEntries(data().porEstado); track entry[0]) {
                <div class="donut-item">
                  <div class="donut-bar" [style.width.%]="getPercent(entry[1], data().kpis.totalEquipos)"
                       [style.background]="getStatusColor(entry[0])"></div>
                  <span class="donut-label">{{ entry[0] }}</span>
                  <span class="donut-value">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </mat-card>

          <!-- Por Categoria -->
          <mat-card class="chart-card">
            <h3>Equipos por Categoria</h3>
            <div class="bar-chart">
              @for (entry of objectEntries(data().porCategoria); track entry[0]) {
                <div class="bar-row">
                  <span class="bar-label">{{ entry[0] }}</span>
                  <div class="bar-track">
                    <div class="bar-fill blue-fill" [style.width.%]="getPercent(entry[1], getMaxValue(data().porCategoria))"></div>
                  </div>
                  <span class="bar-value">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </mat-card>
        </div>

        <!-- Charts Row 2 -->
        <div class="charts-row">
          <!-- Propiedad -->
          <mat-card class="chart-card small">
            <h3>Propiedad</h3>
            <div class="ownership-chart">
              @for (entry of objectEntries(data().porPropiedad); track entry[0]) {
                <div class="ownership-item">
                  <div class="ownership-icon" [class.owned]="entry[0] === 'Propios'" [class.rented]="entry[0] === 'Alquilados'">
                    <mat-icon>{{ entry[0] === 'Propios' ? 'verified' : 'schedule' }}</mat-icon>
                  </div>
                  <div class="ownership-info">
                    <div class="ownership-value">{{ entry[1] }}</div>
                    <div class="ownership-label">{{ entry[0] }}</div>
                  </div>
                </div>
              }
            </div>
          </mat-card>

          <!-- Ubicacion -->
          <mat-card class="chart-card">
            <h3>Equipos por Ubicacion</h3>
            <div class="bar-chart">
              @for (entry of objectEntries(data().porUbicacion); track entry[0]) {
                <div class="bar-row">
                  <span class="bar-label">{{ entry[0] }}</span>
                  <div class="bar-track">
                    <div class="bar-fill green-fill" [style.width.%]="getPercent(entry[1], getMaxValue(data().porUbicacion))"></div>
                  </div>
                  <span class="bar-value">{{ entry[1] }}</span>
                </div>
              }
            </div>
          </mat-card>

          <!-- Hardware -->
          <mat-card class="chart-card small">
            <h3>Salud Hardware</h3>
            <div class="hw-metrics">
              <div class="metric-circle" [class.ok]="data().saludPromedioDisco >= 50" [class.warn]="data().saludPromedioDisco < 50 && data().saludPromedioDisco >= 30" [class.crit]="data().saludPromedioDisco < 30">
                <span class="metric-value">{{ data().saludPromedioDisco || '-' }}%</span>
                <span class="metric-label">Salud Disco</span>
              </div>
              <div class="metric-circle" [class.ok]="data().temperaturaPromedio < 60" [class.warn]="data().temperaturaPromedio >= 60 && data().temperaturaPromedio < 70" [class.crit]="data().temperaturaPromedio >= 70">
                <span class="metric-value">{{ data().temperaturaPromedio || '-' }} C</span>
                <span class="metric-label">Temp Prom</span>
              </div>
            </div>
            @if (data().porTipoDisco) {
              <div class="disk-types">
                @for (entry of objectEntries(data().porTipoDisco); track entry[0]) {
                  <span class="disk-badge">{{ entry[0] }}: {{ entry[1] }}</span>
                }
              </div>
            }
          </mat-card>
        </div>

        <!-- Charts Row 3 -->
        <div class="charts-row">
          <!-- Valor por categoria -->
          <mat-card class="chart-card">
            <h3>Valor de Inventario por Categoria</h3>
            <div class="bar-chart">
              @for (entry of objectEntries(data().valorPorCategoria); track entry[0]) {
                <div class="bar-row">
                  <span class="bar-label">{{ entry[0] }}</span>
                  <div class="bar-track">
                    <div class="bar-fill purple-fill" [style.width.%]="getPercent(entry[1], getMaxValue(data().valorPorCategoria))"></div>
                  </div>
                  <span class="bar-value">{{ formatMoney(entry[1]) }}</span>
                </div>
              }
            </div>
          </mat-card>

          <!-- Mantenimientos por mes -->
          <mat-card class="chart-card">
            <h3>Mantenimientos - Ultimos 6 Meses</h3>
            <div class="maint-chart">
              @for (m of data().mantenimientosPorMes; track m.month) {
                <div class="maint-column">
                  <div class="maint-bars">
                    <div class="maint-bar preventive" [style.height.px]="m.preventive * 30 || 2" title="Preventivos: {{m.preventive}}"></div>
                    <div class="maint-bar corrective" [style.height.px]="m.corrective * 30 || 2" title="Correctivos: {{m.corrective}}"></div>
                  </div>
                  <span class="maint-month">{{ m.month.substring(5) }}</span>
                  <span class="maint-total">{{ m.total }}</span>
                </div>
              }
            </div>
            <div class="legend">
              <span class="legend-item"><span class="legend-dot prev"></span> Preventivo</span>
              <span class="legend-item"><span class="legend-dot corr"></span> Correctivo</span>
            </div>
          </mat-card>
        </div>

        <!-- Equipos Criticos -->
        @if (data().equiposCriticos?.length > 0) {
          <mat-card class="chart-card critical-card">
            <h3><mat-icon>warning</mat-icon> Equipos Criticos ({{ data().equiposCriticos.length }})</h3>
            <div class="critical-list">
              @for (eq of data().equiposCriticos; track eq.serial) {
                <div class="critical-item">
                  <div class="critical-info">
                    <span class="critical-name">{{ eq.name }}</span>
                    <span class="critical-cat">{{ eq.category }} | {{ eq.serial }}</span>
                  </div>
                  <div class="critical-issues">
                    @for (issue of eq.issues; track issue) {
                      <span class="issue-badge">{{ issue }}</span>
                    }
                  </div>
                </div>
              }
            </div>
          </mat-card>
        }
      }
    </div>
  `,
  styles: [`
    .reports-page { padding: 32px; max-width: 1400px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 32px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .header p { color: #94A3B8; margin: 4px 0 0; }
    .print-btn { color: #94A3B8 !important; border-color: #475569 !important; }

    .loading { text-align: center; padding: 60px; }
    .loading p { color: #94A3B8; margin-top: 16px; }

    .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 24px; border-radius: 16px; text-align: center; position: relative; overflow: hidden; }
    .kpi-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.blue::before { background: linear-gradient(90deg, #3B82F6, #60A5FA); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.green::before { background: linear-gradient(90deg, #10B981, #6EE7B7); }
    .kpi-card.purple { background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.2); }
    .kpi-card.purple::before { background: linear-gradient(90deg, #8B5CF6, #A78BFA); }
    .kpi-card.orange { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); }
    .kpi-card.orange::before { background: linear-gradient(90deg, #F59E0B, #FBBF24); }
    .kpi-card mat-icon { font-size: 32px; width: 32px; height: 32px; color: #94A3B8; margin-bottom: 8px; }
    .kpi-value { font-size: 32px; font-weight: 700; color: #F1F5F9; }
    .kpi-label { color: #94A3B8; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }

    .charts-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 16px; margin-bottom: 16px; }
    .chart-card { padding: 24px; }
    .chart-card.small { max-width: 350px; }
    .chart-card h3 { display: flex; align-items: center; gap: 8px; color: #E2E8F0; font-size: 16px; margin: 0 0 20px; }
    .chart-card h3 mat-icon { color: #EF4444; }

    /* Bar Chart */
    .bar-chart { display: flex; flex-direction: column; gap: 12px; }
    .bar-row { display: flex; align-items: center; gap: 12px; }
    .bar-label { width: 120px; color: #CBD5E1; font-size: 13px; text-align: right; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .bar-track { flex: 1; height: 24px; background: rgba(15,23,42,0.5); border-radius: 6px; overflow: hidden; }
    .bar-fill { height: 100%; border-radius: 6px; min-width: 4px; transition: width 1s ease; }
    .blue-fill { background: linear-gradient(90deg, #3B82F6, #60A5FA); }
    .green-fill { background: linear-gradient(90deg, #10B981, #6EE7B7); }
    .purple-fill { background: linear-gradient(90deg, #8B5CF6, #A78BFA); }
    .bar-value { width: 60px; color: #F1F5F9; font-weight: 600; font-size: 14px; }

    /* Donut / Status */
    .donut-chart-container { display: flex; flex-direction: column; gap: 10px; }
    .donut-item { display: flex; align-items: center; gap: 12px; }
    .donut-bar { height: 28px; border-radius: 6px; min-width: 8px; transition: width 1s ease; }
    .donut-label { width: 110px; color: #CBD5E1; font-size: 13px; }
    .donut-value { color: #F1F5F9; font-weight: 700; font-size: 16px; }

    /* Ownership */
    .ownership-chart { display: flex; gap: 24px; justify-content: center; padding: 16px 0; }
    .ownership-item { display: flex; align-items: center; gap: 12px; }
    .ownership-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
    .ownership-icon.owned { background: rgba(59,130,246,0.2); }
    .ownership-icon.rented { background: rgba(139,92,246,0.2); }
    .ownership-icon mat-icon { color: #93C5FD; }
    .ownership-value { font-size: 28px; font-weight: 700; color: #F1F5F9; }
    .ownership-label { color: #94A3B8; font-size: 13px; }

    /* Hardware Metrics */
    .hw-metrics { display: flex; gap: 24px; justify-content: center; padding: 16px 0; }
    .metric-circle { width: 100px; height: 100px; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 3px solid; }
    .metric-circle.ok { border-color: #10B981; }
    .metric-circle.warn { border-color: #F59E0B; }
    .metric-circle.crit { border-color: #EF4444; }
    .metric-value { font-size: 20px; font-weight: 700; color: #F1F5F9; }
    .metric-label { font-size: 10px; color: #94A3B8; text-transform: uppercase; }
    .disk-types { display: flex; gap: 8px; justify-content: center; margin-top: 12px; flex-wrap: wrap; }
    .disk-badge { padding: 4px 12px; border-radius: 8px; background: rgba(59,130,246,0.15); color: #93C5FD; font-size: 12px; font-weight: 600; }

    /* Maintenance Chart */
    .maint-chart { display: flex; gap: 16px; justify-content: center; align-items: flex-end; padding: 20px 0; min-height: 120px; }
    .maint-column { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .maint-bars { display: flex; gap: 4px; align-items: flex-end; }
    .maint-bar { width: 20px; border-radius: 4px 4px 0 0; min-height: 2px; }
    .maint-bar.preventive { background: linear-gradient(180deg, #3B82F6, #1E40AF); }
    .maint-bar.corrective { background: linear-gradient(180deg, #F59E0B, #D97706); }
    .maint-month { color: #94A3B8; font-size: 11px; }
    .maint-total { color: #F1F5F9; font-weight: 600; font-size: 14px; }
    .legend { display: flex; gap: 20px; justify-content: center; margin-top: 12px; }
    .legend-item { display: flex; align-items: center; gap: 6px; color: #94A3B8; font-size: 12px; }
    .legend-dot { width: 12px; height: 12px; border-radius: 3px; }
    .legend-dot.prev { background: #3B82F6; }
    .legend-dot.corr { background: #F59E0B; }

    /* Critical */
    .critical-card { border-left: 4px solid #EF4444 !important; }
    .critical-list { display: flex; flex-direction: column; gap: 12px; }
    .critical-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: rgba(239,68,68,0.08); border-radius: 8px; flex-wrap: wrap; gap: 8px; }
    .critical-name { color: #F1F5F9; font-weight: 600; display: block; }
    .critical-cat { color: #94A3B8; font-size: 12px; }
    .critical-issues { display: flex; gap: 6px; flex-wrap: wrap; }
    .issue-badge { padding: 3px 10px; border-radius: 8px; background: rgba(239,68,68,0.2); color: #FCA5A5; font-size: 11px; font-weight: 600; }

    @media print {
      .header button, .print-btn { display: none !important; }
      .reports-page { padding: 0; }
      .mat-mdc-card { box-shadow: none !important; border: 1px solid #ddd !important; background: white !important; color: black !important; }
      * { color: black !important; }
    }

    @media (max-width: 768px) {
      .reports-page { padding: 16px; }
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .charts-row { grid-template-columns: 1fr; }
      .chart-card.small { max-width: 100%; }
      .bar-label { width: 80px; font-size: 11px; }
    }
  `]
})
export class ReportsComponent implements OnInit {
  data = signal<any>(null);
  loading = signal(true);

  constructor(private reportService: ReportService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.reportService.getDashboard().subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); this.cdr.detectChanges(); },
      error: () => { this.loading.set(false); this.cdr.detectChanges(); }
    });
  }

  objectEntries(obj: any): [string, any][] {
    return obj ? Object.entries(obj) : [];
  }

  getPercent(value: number, max: number): number {
    if (!max || max === 0) return 0;
    return Math.max(5, (value / max) * 100);
  }

  getMaxValue(obj: any): number {
    if (!obj) return 1;
    const values = Object.values(obj) as number[];
    return Math.max(...values, 1);
  }

  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'Activo': '#10B981', 'Mantenimiento': '#F59E0B',
      'Inactivo': '#EF4444', 'Retirado': '#64748B'
    };
    return colors[status] || '#3B82F6';
  }

  formatMoney(value: number): string {
    if (!value) return '$0';
    return '$' + new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(value);
  }

  printReport() { window.print(); }
}