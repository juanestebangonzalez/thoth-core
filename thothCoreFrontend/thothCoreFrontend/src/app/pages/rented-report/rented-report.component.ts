import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { environment } from '../../../environments/environment';

interface GroupRow { cantidad: number; totalMensual: number; }
interface SedeRow extends GroupRow { sede: string; }
interface CentroRow extends GroupRow { centroCosto: string; }
interface MatrizRow extends GroupRow { sede: string; centroCosto: string; }
interface RentedEquipmentRow {
  equipmentId: string; name: string; inventoryNumber?: string; category?: string; sede?: string; area?: string;
  centroCosto?: string; rentalCompany?: string; contractNumber?: string; monthlyValue?: number | null;
  startDate?: string; endDate?: string;
}
interface RentedReport {
  totalEquipos: number; totalMensual: number; sinValor: number;
  porSede: SedeRow[]; porCentroCosto: CentroRow[]; matriz: MatrizRow[]; equipos: RentedEquipmentRow[];
}

@Component({
  selector: 'app-rented-report',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule, MatSnackBarModule],
  template: `
    <div class="report-page">
      <div class="header">
        <h1><mat-icon>request_quote</mat-icon> Informe de Equipos Alquilados</h1>
        <div class="header-actions">
          <button mat-stroked-button (click)="exportExcel()" class="action-btn" [disabled]="!data() || exporting()">
            <mat-icon>download</mat-icon> {{ exporting() ? 'Generando...' : 'Exportar Excel' }}
          </button>
        </div>
      </div>

      @if (loading()) {
        <p class="loading">Cargando informe...</p>
      } @else if (error()) {
        <p class="empty-msg">{{ error() }}</p>
      } @else if (data(); as d) {
        <div class="kpi-grid">
          <div class="kpi-card blue">
            <mat-icon>devices</mat-icon>
            <div class="kpi-value">{{ d.totalEquipos }}</div>
            <div class="kpi-label">Equipos Alquilados</div>
          </div>
          <div class="kpi-card green">
            <mat-icon>payments</mat-icon>
            <div class="kpi-value">{{ cop(d.totalMensual) }}</div>
            <div class="kpi-label">Total Mensual</div>
          </div>
          <div class="kpi-card purple">
            <mat-icon>calendar_month</mat-icon>
            <div class="kpi-value">{{ cop(d.totalMensual * 12) }}</div>
            <div class="kpi-label">Total Anual (x12)</div>
          </div>
          <div class="kpi-card orange">
            <mat-icon>help_outline</mat-icon>
            <div class="kpi-value">{{ d.sinValor }}</div>
            <div class="kpi-label">Sin Valor Registrado</div>
          </div>
        </div>

        <div class="two-col">
          <mat-card class="table-card">
            <h3>Por Sede</h3>
            <div class="table-scroll">
              <table>
                <thead><tr><th>Sede</th><th class="num">Cantidad</th><th class="num">Total Mensual</th></tr></thead>
                <tbody>
                  @for (r of d.porSede; track r.sede) {
                    <tr><td>{{ r.sede || 'Sin sede' }}</td><td class="num">{{ r.cantidad }}</td><td class="num">{{ cop(r.totalMensual) }}</td></tr>
                  } @empty {
                    <tr><td colspan="3" class="empty-msg">Sin datos</td></tr>
                  }
                </tbody>
              </table>
            </div>
          </mat-card>
          <mat-card class="table-card">
            <h3>Por Centro de Costo</h3>
            <div class="table-scroll">
              <table>
                <thead><tr><th>Centro de Costo</th><th class="num">Cantidad</th><th class="num">Total Mensual</th></tr></thead>
                <tbody>
                  @for (r of d.porCentroCosto; track r.centroCosto) {
                    <tr><td>{{ r.centroCosto || 'Sin centro de costo' }}</td><td class="num">{{ r.cantidad }}</td><td class="num">{{ cop(r.totalMensual) }}</td></tr>
                  } @empty {
                    <tr><td colspan="3" class="empty-msg">Sin datos</td></tr>
                  }
                </tbody>
              </table>
            </div>
          </mat-card>
        </div>

        <mat-card class="table-card">
          <h3>Matriz Sede x Centro de Costo</h3>
          @if (matrix().sedes.length === 0) {
            <p class="empty-msg">Sin datos</p>
          } @else {
            <div class="table-scroll">
              <table class="matrix">
                <thead>
                  <tr>
                    <th>Sede</th>
                    @for (c of matrix().centros; track c) { <th class="num">{{ c }}</th> }
                    <th class="num total-col">Total</th>
                  </tr>
                </thead>
                <tbody>
                  @for (s of matrix().sedes; track s) {
                    <tr>
                      <td class="row-head">{{ s }}</td>
                      @for (c of matrix().centros; track c) {
                        <td class="num">
                          @if (matrix().cells[s + '||' + c]; as cell) {
                            <div class="cell-money">{{ cop(cell.totalMensual) }}</div>
                            <div class="cell-qty">{{ cell.cantidad }} eq.</div>
                          } @else { <span class="cell-empty">-</span> }
                        </td>
                      }
                      <td class="num total-col">
                        <div class="cell-money">{{ cop(matrix().rowTotals[s].totalMensual) }}</div>
                        <div class="cell-qty">{{ matrix().rowTotals[s].cantidad }} eq.</div>
                      </td>
                    </tr>
                  }
                  <tr class="total-row">
                    <td class="row-head">Total</td>
                    @for (c of matrix().centros; track c) {
                      <td class="num">
                        <div class="cell-money">{{ cop(matrix().colTotals[c].totalMensual) }}</div>
                        <div class="cell-qty">{{ matrix().colTotals[c].cantidad }} eq.</div>
                      </td>
                    }
                    <td class="num total-col">
                      <div class="cell-money">{{ cop(matrix().grand.totalMensual) }}</div>
                      <div class="cell-qty">{{ matrix().grand.cantidad }} eq.</div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          }
        </mat-card>

        <mat-card class="table-card">
          <h3>Detalle de Equipos ({{ d.equipos.length }})</h3>
          <div class="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Equipo</th><th>N Inventario</th><th>Categoria</th><th>Sede</th><th>Area</th><th>Centro de Costo</th>
                  <th>Arrendadora</th><th>Contrato</th><th class="num">Valor Mensual</th><th>Inicio</th><th>Fin</th>
                </tr>
              </thead>
              <tbody>
                @for (e of d.equipos; track e.equipmentId) {
                  <tr>
                    <td><a [routerLink]="['/equipment', e.equipmentId]" class="link">{{ e.name }}</a></td>
                    <td>{{ e.inventoryNumber || '-' }}</td>
                    <td>{{ e.category || '-' }}</td>
                    <td>{{ e.sede || '-' }}</td>
                    <td>{{ e.area || '-' }}</td>
                    <td>{{ e.centroCosto || '-' }}</td>
                    <td>{{ e.rentalCompany || '-' }}</td>
                    <td>{{ e.contractNumber || '-' }}</td>
                    <td class="num">
                      @if (e.monthlyValue != null) { {{ cop(e.monthlyValue) }} } @else { <span class="badge warn">Sin valor</span> }
                    </td>
                    <td>{{ e.startDate || '-' }}</td>
                    <td>{{ e.endDate || '-' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="11" class="empty-msg">No hay equipos alquilados</td></tr>
                }
              </tbody>
            </table>
          </div>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .report-page { padding: 32px; max-width: 1300px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
    .header-actions { display: flex; gap: 8px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; overflow: hidden; }
    .action-btn { color: #94A3B8 !important; border-color: #475569 !important; }
    .action-btn mat-icon { font-size: 18px; width: 18px; height: 18px; overflow: hidden; }
    .loading { text-align: center; color: #94A3B8; padding: 60px; }
    .empty-msg { text-align: center; color: #64748B; padding: 24px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .kpi-card { padding: 20px; border-radius: 14px; text-align: center; }
    .kpi-card mat-icon { font-size: 28px; width: 28px; height: 28px; overflow: hidden; color: #94A3B8; display: block; margin: 0 auto 8px; }
    .kpi-value { font-size: 26px; font-weight: 700; color: #F1F5F9; word-break: break-word; }
    .kpi-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.purple { background: rgba(139,92,246,0.1); border: 1px solid rgba(139,92,246,0.2); }
    .kpi-card.orange { background: rgba(245,158,11,0.1); border: 1px solid rgba(245,158,11,0.2); }
    .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .table-card { padding: 24px; margin-bottom: 16px; }
    .table-card h3 { color: #E2E8F0; font-size: 16px; margin: 0 0 16px; }
    .table-scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(148,163,184,0.15); white-space: nowrap; }
    td { padding: 10px 12px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
    .num { text-align: right; white-space: nowrap; }
    .row-head { font-weight: 600; color: #E2E8F0; white-space: nowrap; }
    .total-col { background: rgba(59,130,246,0.06); }
    .total-row td { background: rgba(59,130,246,0.08); font-weight: 600; border-top: 1px solid rgba(148,163,184,0.25); }
    .cell-money { color: #F1F5F9; }
    .cell-qty { color: #94A3B8; font-size: 11px; }
    .cell-empty { color: #475569; }
    .link { color: #60A5FA; text-decoration: none; }
    .link:hover { text-decoration: underline; }
    .badge { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; }
    .badge.warn { background: rgba(245,158,11,0.15); color: #FBBF24; }
    @media (max-width: 900px) {
      .report-page { padding: 16px; }
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .two-col { grid-template-columns: 1fr; }
    }
  `]
})
export class RentedReportComponent implements OnInit {
  data = signal<RentedReport | null>(null);
  loading = signal(true);
  error = signal('');
  exporting = signal(false);
  private copFormatter = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

  /** Matriz sede x centro de costo con totales por fila, columna y general. */
  matrix = computed(() => {
    const d = this.data();
    const cells: Record<string, GroupRow> = {};
    const rowTotals: Record<string, GroupRow> = {};
    const colTotals: Record<string, GroupRow> = {};
    const grand: GroupRow = { cantidad: 0, totalMensual: 0 };
    const sedes: string[] = [];
    const centros: string[] = [];
    for (const m of d?.matriz || []) {
      const s = m.sede || 'Sin sede';
      const c = m.centroCosto || 'Sin centro de costo';
      if (!sedes.includes(s)) sedes.push(s);
      if (!centros.includes(c)) centros.push(c);
      const key = s + '||' + c;
      const cell = cells[key] || (cells[key] = { cantidad: 0, totalMensual: 0 });
      const rt = rowTotals[s] || (rowTotals[s] = { cantidad: 0, totalMensual: 0 });
      const ct = colTotals[c] || (colTotals[c] = { cantidad: 0, totalMensual: 0 });
      for (const t of [cell, rt, ct, grand]) { t.cantidad += m.cantidad || 0; t.totalMensual += m.totalMensual || 0; }
    }
    sedes.sort((a, b) => a.localeCompare(b));
    centros.sort((a, b) => a.localeCompare(b));
    return { sedes, centros, cells, rowTotals, colTotals, grand };
  });

  constructor(private http: HttpClient, private snackBar: MatSnackBar, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.http.get<RentedReport>(`${environment.apiUrl}/reports/rented-equipment`).subscribe({
      next: (r) => {
        this.data.set({
          totalEquipos: r?.totalEquipos || 0, totalMensual: r?.totalMensual || 0, sinValor: r?.sinValor || 0,
          porSede: r?.porSede || [], porCentroCosto: r?.porCentroCosto || [], matriz: r?.matriz || [], equipos: r?.equipos || []
        });
        this.loading.set(false);
        this.cdr.detectChanges();
      },
      error: () => {
        this.error.set('Error al cargar el informe de equipos alquilados');
        this.loading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  cop(v: number | null | undefined): string {
    return this.copFormatter.format(Number(v) || 0);
  }

  async exportExcel() {
    const d = this.data();
    if (!d) return;
    this.exporting.set(true);
    try {
      const mod: any = await import('exceljs');
      const ExcelJS = mod.default ?? mod;
      const wb = new ExcelJS.Workbook();
      wb.creator = 'THOTH C.O.R.E.';
      wb.created = new Date();
      const money = '"$" #,##0';
      const headStyle = (row: any) => {
        row.font = { bold: true, color: { argb: 'FFFFFFFF' } };
        row.eachCell((c: any) => { c.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF1E40AF' } }; });
      };

      const ws = wb.addWorksheet('Resumen');
      ws.columns = [{ width: 34 }, { width: 16 }, { width: 20 }];
      ws.addRow(['Informe de Equipos Alquilados']).font = { bold: true, size: 14 };
      ws.addRow(['Generado', new Date().toLocaleString('es-CO')]);
      ws.addRow([]);
      ws.addRow(['Equipos alquilados', d.totalEquipos]);
      ws.addRow(['Total mensual', d.totalMensual]).getCell(2).numFmt = money;
      ws.addRow(['Total anual (x12)', d.totalMensual * 12]).getCell(2).numFmt = money;
      ws.addRow(['Sin valor registrado', d.sinValor]);
      ws.addRow([]);
      headStyle(ws.addRow(['Sede', 'Cantidad', 'Total Mensual']));
      for (const r of d.porSede) ws.addRow([r.sede || 'Sin sede', r.cantidad, r.totalMensual]).getCell(3).numFmt = money;
      ws.addRow([]);
      headStyle(ws.addRow(['Centro de Costo', 'Cantidad', 'Total Mensual']));
      for (const r of d.porCentroCosto) ws.addRow([r.centroCosto || 'Sin centro de costo', r.cantidad, r.totalMensual]).getCell(3).numFmt = money;
      ws.addRow([]);

      // Matriz: total mensual por sede x centro de costo
      const m = this.matrix();
      if (m.sedes.length) {
        ws.addRow(['Matriz Sede x Centro de Costo (total mensual)']).font = { bold: true };
        headStyle(ws.addRow(['Sede', ...m.centros, 'Total']));
        for (const s of m.sedes) {
          const row = ws.addRow([s, ...m.centros.map(c => m.cells[s + '||' + c]?.totalMensual ?? 0), m.rowTotals[s].totalMensual]);
          for (let i = 2; i <= m.centros.length + 2; i++) row.getCell(i).numFmt = money;
        }
        const tr = ws.addRow(['Total', ...m.centros.map(c => m.colTotals[c].totalMensual), m.grand.totalMensual]);
        tr.font = { bold: true };
        for (let i = 2; i <= m.centros.length + 2; i++) { tr.getCell(i).numFmt = money; if (!ws.getColumn(i).width || ws.getColumn(i).width! < 18) ws.getColumn(i).width = 18; }
      }

      const wd = wb.addWorksheet('Detalle');
      wd.columns = [
        { header: 'Equipo', key: 'name', width: 30 },
        { header: 'N Inventario', key: 'inventoryNumber', width: 16 },
        { header: 'Categoria', key: 'category', width: 16 },
        { header: 'Sede', key: 'sede', width: 20 },
        { header: 'Area', key: 'area', width: 20 },
        { header: 'Centro de Costo', key: 'centroCosto', width: 20 },
        { header: 'Empresa Arrendadora', key: 'rentalCompany', width: 24 },
        { header: 'N Contrato', key: 'contractNumber', width: 16 },
        { header: 'Valor Mensual', key: 'monthlyValue', width: 16 },
        { header: 'Inicio Alquiler', key: 'startDate', width: 14 },
        { header: 'Fin Alquiler', key: 'endDate', width: 14 }
      ];
      headStyle(wd.getRow(1));
      for (const e of d.equipos) {
        wd.addRow({ ...e, monthlyValue: e.monthlyValue ?? null });
      }
      wd.getColumn('monthlyValue').numFmt = money;
      wd.views = [{ state: 'frozen', ySplit: 1 }];

      const buf = await wb.xlsx.writeBuffer();
      const blob = new Blob([buf], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = 'Alquilados_THOTH_' + new Date().toISOString().split('T')[0] + '.xlsx';
      link.click();
      setTimeout(() => URL.revokeObjectURL(link.href), 2000);
      this.snackBar.open('Archivo exportado: ' + link.download, 'OK', { duration: 3000 });
    } catch (e) {
      console.error(e);
      this.snackBar.open('Error al generar el Excel', 'OK', { duration: 4000 });
    } finally {
      this.exporting.set(false);
      this.cdr.detectChanges();
    }
  }
}
