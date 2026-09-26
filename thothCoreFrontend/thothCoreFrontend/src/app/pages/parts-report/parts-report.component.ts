import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-parts-report',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule],
  template: `
    <div class="report-page">
      <div class="header">
        <h1><mat-icon>construction</mat-icon> Reporte de Partes Reemplazadas</h1>
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
        <!-- KPI -->
        <div class="kpi-grid">
          <div class="kpi-card blue">
            <mat-icon>inventory_2</mat-icon>
            <div class="kpi-value">{{ data().totalPartes }}</div>
            <div class="kpi-label">Total Partes</div>
          </div>
          <div class="kpi-card green">
            <mat-icon>calendar_month</mat-icon>
            <div class="kpi-value">{{ getCurrentMonthParts() }}</div>
            <div class="kpi-label">Este Mes</div>
          </div>
          <div class="kpi-card orange">
            <mat-icon>trending_up</mat-icon>
            <div class="kpi-value">{{ getAvgPerMonth() }}</div>
            <div class="kpi-label">Promedio/Mes</div>
          </div>
        </div>

        <!-- Grafico mensual -->
        <mat-card class="chart-card">
          <h3>Partes Reemplazadas por Mes (ultimos 12 meses)</h3>
          <div class="chart-bars">
            @for (m of data().porMes; track m.month) {
              <div class="chart-col">
                <div class="bar-stack">
                  <div class="bar parts" [style.height.px]="m.cantidadPartes * 30 || 2" [title]="m.cantidadPartes + ' partes'"></div>
                </div>
                <span class="bar-total">{{ m.cantidadPartes }}</span>
                <span class="bar-label">{{ m.label }}</span>
              </div>
            }
          </div>
        </mat-card>

        <!-- Tabla ultimas partes -->
        <mat-card class="table-card">
          <h3>Ultimas 20 Partes Reemplazadas</h3>
          @if (data().ultimasPartes.length === 0) {
            <p class="empty-msg">No hay partes reemplazadas registradas</p>
          } @else {
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Parte</th>
                    <th>Serial</th>
                    <th>Motivo</th>
                    <th>Tecnico</th>
                    <th>Tipo Mant.</th>
                    <th>Fecha Compra</th>
                    <th>Ticket</th>
                  </tr>
                </thead>
                <tbody>
                  @for (p of data().ultimasPartes; track $index) {
                    <tr>
                      <td>{{ p.maintenanceDate | date:'dd/MM/yyyy' }}</td>
                      <td><strong>{{ p.partName }}</strong></td>
                      <td>{{ p.partSerialNumber || '-' }}</td>
                      <td class="reason-cell">{{ p.reason || '-' }}</td>
                      <td>{{ p.technicianName || '-' }}</td>
                      <td>
                        <span class="badge" [class.prev]="p.maintenanceType === 'PREVENTIVE'" [class.corr]="p.maintenanceType === 'CORRECTIVE'">
                          {{ p.maintenanceType === 'PREVENTIVE' ? 'PREV' : p.maintenanceType === 'CORRECTIVE' ? 'CORR' : (p.maintenanceType || '-') }}
                        </span>
                      </td>
                      <td>{{ p.purchaseDate ? (p.purchaseDate | date:'dd/MM/yyyy') : '-' }}</td>
                      <td>{{ p.ticketNumber || '-' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </mat-card>

        <!-- Detalle mensual expandible -->
        <mat-card class="table-card">
          <h3>Detalle Mensual</h3>
          <table>
            <thead>
              <tr><th>Mes</th><th>Cantidad de Partes</th><th>Detalles</th></tr>
            </thead>
            <tbody>
              @for (m of data().porMes; track m.month) {
                <tr [class.has-data]="m.cantidadPartes > 0">
                  <td>{{ m.label }}</td>
                  <td><span class="badge blue-badge">{{ m.cantidadPartes }}</span></td>
                  <td>
                    @if (m.partes && m.partes.length > 0) {
                      <span class="parts-list">
                        @for (p of m.partes.slice(0,3); track $index) {
                          <span class="part-chip">{{ p.partName }}</span>
                        }
                        @if (m.partes.length > 3) {
                          <span class="part-chip more">+{{ m.partes.length - 3 }} mas</span>
                        }
                      </span>
                    } @else {
                      <span class="no-data">Sin partes</span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </mat-card>
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

    .kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 20px; border-radius: 14px; text-align: center; }
    .kpi-card mat-icon { font-size: 28px; width: 28px; height: 28px; overflow: hidden; color: #94A3B8; display: block; margin: 0 auto 8px; }
    .kpi-value { font-size: 32px; font-weight: 700; color: #F1F5F9; }
    .kpi-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.orange { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); }

    .chart-card, .table-card { padding: 24px; margin-bottom: 16px; }
    .chart-card h3, .table-card h3 { color: #E2E8F0; font-size: 16px; margin: 0 0 20px; }

    .chart-bars { display: flex; gap: 12px; justify-content: center; align-items: flex-end; min-height: 150px; padding: 20px 0; }
    .chart-col { display: flex; flex-direction: column; align-items: center; gap: 4px; }
    .bar-stack { display: flex; gap: 3px; align-items: flex-end; }
    .bar { width: 24px; border-radius: 3px 3px 0 0; min-height: 2px; }
    .bar.parts { background: linear-gradient(180deg, #60A5FA, #3B82F6); }
    .bar-total { color: #F1F5F9; font-weight: 700; font-size: 14px; }
    .bar-label { color: #94A3B8; font-size: 10px; text-align: center; max-width: 60px; }

    .table-scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(148,163,184,0.15); white-space: nowrap; }
    td { padding: 10px 12px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
    tr.has-data { background: rgba(59,130,246,0.03); }
    .reason-cell { max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .badge { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; }
    .badge.prev { background: rgba(59,130,246,0.15); color: #93C5FD; }
    .badge.corr { background: rgba(245,158,11,0.15); color: #FBBF24; }
    .badge.blue-badge { background: rgba(59,130,246,0.15); color: #93C5FD; }

    .parts-list { display: flex; flex-wrap: wrap; gap: 4px; }
    .part-chip { padding: 2px 8px; border-radius: 6px; font-size: 11px; background: rgba(59,130,246,0.1); color: #93C5FD; }
    .part-chip.more { background: rgba(148,163,184,0.1); color: #94A3B8; font-style: italic; }
    .no-data { color: #475569; font-size: 12px; font-style: italic; }

    @media print { .header-actions { display: none !important; } }
    @media (max-width: 768px) {
      .report-page { padding: 16px; }
      .kpi-grid { grid-template-columns: 1fr; }
      .chart-bars { overflow-x: auto; }
      h1 { font-size: 20px; }
    }
  `]
})
export class PartsReportComponent implements OnInit {
  data = signal<any>(null);

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.http.get<any>(`${environment.apiUrl}/reports/parts-report`).subscribe({
      next: (d) => { this.data.set(d); this.cdr.detectChanges(); }
    });
  }

  getCurrentMonthParts(): number {
    const d = this.data();
    if (!d?.porMes?.length) return 0;
    return d.porMes[d.porMes.length - 1]?.cantidadPartes || 0;
  }

  getAvgPerMonth(): string {
    const d = this.data();
    if (!d?.porMes?.length || d.totalPartes === 0) return '0';
    const monthsWithData = d.porMes.filter((m: any) => m.cantidadPartes > 0).length || 1;
    return (d.totalPartes / monthsWithData).toFixed(1);
  }

  downloadCSV() {
    const d = this.data();
    if (!d) return;

    let csv = 'REPORTE DE PARTES REEMPLAZADAS - THOTH C.O.R.E.\n\n';
    csv += 'RESUMEN\n';
    csv += 'Total Partes Reemplazadas,' + d.totalPartes + '\n\n';

    csv += 'POR MES\n';
    csv += 'Mes,Cantidad\n';
    for (const m of d.porMes || []) {
      csv += m.label + ',' + m.cantidadPartes + '\n';
    }
    csv += '\n';

    csv += 'ULTIMAS PARTES REEMPLAZADAS\n';
    csv += 'Fecha,Parte,Serial,Motivo,Tecnico,Tipo Mant.,Fecha Compra,Ticket\n';
    for (const p of d.ultimasPartes || []) {
      const fecha = p.maintenanceDate ? new Date(p.maintenanceDate).toLocaleDateString('es-CO') : '-';
      const compra = p.purchaseDate ? new Date(p.purchaseDate).toLocaleDateString('es-CO') : '-';
      csv += [fecha, p.partName || '-', p.partSerialNumber || '-', p.reason || '-', p.technicianName || '-', p.maintenanceType || '-', compra, p.ticketNumber || '-'].join(',') + '\n';
    }

    const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const today = new Date().toISOString().slice(0, 10);
    a.download = 'Reporte_Partes_Reemplazadas_' + today + '.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  printReport() { window.print(); }
}
