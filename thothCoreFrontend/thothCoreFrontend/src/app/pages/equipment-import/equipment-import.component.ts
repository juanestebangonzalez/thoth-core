import { Component, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { environment } from '../../../environments/environment';
import { SedeService } from '../../core/services/sede.service';
import { AreaService } from '../../core/services/area.service';
import { CostCenterService } from '../../core/services/cost-center.service';
import { DeviceTypeService } from '../../core/services/device-type.service';
import { MaintenanceCategoryService } from '../../core/services/maintenance-category.service';

/** Tipo de dato de cada columna de la plantilla. */
type ColType = 'text' | 'number' | 'date';
type ListKey = 'categorias' | 'sedes' | 'areas' | 'centros' | 'propiedad' | 'ramTypes' | 'diskTypes' | 'sistemas' | 'mantenimientos' | 'ipAsignacion' | 'osEdiciones' | 'osVersiones' | 'osLicencias';

interface ColumnDef {
  key: string;
  label: string;
  type: ColType;
  width: number;
  list?: ListKey;
  /** false: la lista desplegable es solo sugerencia (admite texto libre). */
  strict?: boolean;
  example: string | number;
  help: string;
}

/** Columnas de la plantilla, en orden. El key coincide con el contrato del backend. */
const COLUMNS: ColumnDef[] = [
  { key: 'name', label: 'Nombre*', type: 'text', width: 28, example: 'PC ADMISIONES 01', help: 'Obligatorio. Nombre del equipo.' },
  { key: 'category', label: 'Categoria*', type: 'text', width: 20, list: 'categorias', example: '', help: 'Obligatorio. Debe existir en Tipos de Dispositivo.' },
  { key: 'serialNumber', label: 'Numero de Serie', type: 'text', width: 20, example: 'SN123456789', help: 'Opcional. Debe ser unico.' },
  { key: 'inventoryNumber', label: 'N Inventario Interno', type: 'text', width: 20, example: 'INV-2026-001', help: 'Opcional.' },
  { key: 'brand', label: 'Marca', type: 'text', width: 16, example: 'DELL', help: 'Opcional.' },
  { key: 'model', label: 'Modelo', type: 'text', width: 18, example: 'OPTIPLEX 7090', help: 'Opcional.' },
  { key: 'macAddress', label: 'MAC Ethernet', type: 'text', width: 20, example: 'AA:BB:CC:DD:EE:01', help: 'Opcional. Formato AA:BB:CC:DD:EE:FF o AABBCCDDEEFF.' },
  { key: 'macAddress2', label: 'MAC WiFi', type: 'text', width: 20, example: '', help: 'Opcional. Solo para portatiles.' },
  { key: 'ipAddress', label: 'Direccion IP', type: 'text', width: 18, example: '192.168.1.10', help: 'Opcional. Direccion IPv4, por ejemplo 192.168.1.10.' },
  { key: 'ipAssignment', label: 'Asignacion IP', type: 'text', width: 16, list: 'ipAsignacion', example: 'DHCP', help: 'Opcional. DHCP o FIJA.' },
  { key: 'sede', label: 'Sede', type: 'text', width: 20, list: 'sedes', example: '', help: 'Opcional. Debe existir en el catalogo de Sedes.' },
  { key: 'area', label: 'Area', type: 'text', width: 20, list: 'areas', example: '', help: 'Opcional. Debe existir en el catalogo de Areas.' },
  { key: 'costCenter', label: 'Centro de Costo', type: 'text', width: 20, list: 'centros', example: '', help: 'Opcional. Debe existir en el catalogo de Centros de Costo.' },
  { key: 'assignedTo', label: 'Asignado a', type: 'text', width: 22, example: 'JUAN PEREZ', help: 'Opcional. Persona o responsable.' },
  { key: 'responsiblePosition', label: 'Cargo Responsable', type: 'text', width: 22, example: 'AUXILIAR ADMINISTRATIVO', help: 'Opcional. Cargo de la persona responsable.' },
  { key: 'responsibleDocument', label: 'Documento Responsable', type: 'text', width: 20, example: '1012345678', help: 'Opcional. Documento de identidad del responsable.' },
  { key: 'responsiblePhone', label: 'Celular Responsable', type: 'text', width: 18, example: '3001234567', help: 'Opcional. Celular de 10 digitos, solo numeros.' },
  { key: 'responsibleEmail', label: 'Correo Responsable', type: 'text', width: 26, example: 'juan.perez@hospital.gov.co', help: 'Opcional. Correo electronico del responsable.' },
  { key: 'ownershipType', label: 'Propiedad (PROPIO/ALQUILADO)', type: 'text', width: 26, list: 'propiedad', example: 'PROPIO', help: 'Opcional. PROPIO o ALQUILADO (por defecto PROPIO).' },
  { key: 'purchaseDate', label: 'Fecha de Compra (AAAA-MM-DD)', type: 'date', width: 26, example: '2024-03-15', help: 'Opcional. Formato AAAA-MM-DD o fecha de Excel.' },
  { key: 'purchaseValue', label: 'Valor de Compra', type: 'number', width: 16, example: 3500000, help: 'Opcional. Solo numeros, sin puntos ni simbolos.' },
  { key: 'rentalCompany', label: 'Empresa Arrendadora', type: 'text', width: 22, example: '', help: 'Solo si es ALQUILADO.' },
  { key: 'rentalContractNumber', label: 'N Contrato Alquiler', type: 'text', width: 20, example: '', help: 'Solo si es ALQUILADO.' },
  { key: 'rentalStartDate', label: 'Inicio Alquiler', type: 'date', width: 16, example: '', help: 'Solo si es ALQUILADO. Formato AAAA-MM-DD.' },
  { key: 'rentalEndDate', label: 'Fin Alquiler', type: 'date', width: 16, example: '', help: 'Solo si es ALQUILADO. Formato AAAA-MM-DD.' },
  { key: 'rentalMonthlyValue', label: 'Valor Mensual Alquiler', type: 'number', width: 20, example: '', help: 'Solo si es ALQUILADO. Valor mensual en pesos.' },
  { key: 'processor', label: 'Procesador', type: 'text', width: 24, example: 'INTEL CORE I5-11500', help: 'Opcional.' },
  { key: 'ramSizeGb', label: 'RAM (GB)', type: 'number', width: 12, example: 16, help: 'Opcional. Numero entero.' },
  { key: 'ramType', label: 'Tipo RAM', type: 'text', width: 12, list: 'ramTypes', example: 'DDR4', help: 'Opcional. DDR3, DDR4, DDR5, LPDDR4 o LPDDR5.' },
  { key: 'diskType', label: 'Tipo Disco', type: 'text', width: 12, list: 'diskTypes', example: 'SSD', help: 'Opcional. HDD, SSD o NVME.' },
  { key: 'diskSizeGb', label: 'Disco (GB)', type: 'number', width: 12, example: 512, help: 'Opcional. Numero entero.' },
  { key: 'operatingSystem', label: 'Sistema Operativo', type: 'text', width: 18, list: 'sistemas', example: 'WINDOWS', help: 'Opcional. WINDOWS, LINUX, MACOS, CHROMEOS, ANDROID, IOS, OTRO o N/A.' },
  { key: 'osEdition', label: 'Software SO (Windows 10/11)', type: 'text', width: 26, list: 'osEdiciones', example: 'WINDOWS 11', help: 'Opcional. Solo si el Sistema Operativo es WINDOWS: WINDOWS 10 o WINDOWS 11.' },
  { key: 'osVersion', label: 'Distribucion / Version', type: 'text', width: 24, list: 'osVersiones', strict: false, example: '24H2', help: 'Opcional. Si es WINDOWS: 26H2, 26H1, 25H2, 24H2 o 23H2. Para otros sistemas, texto libre (ej: UBUNTU 24.04).' },
  { key: 'osLicenseType', label: 'Tipo de Licencia', type: 'text', width: 18, list: 'osLicencias', example: 'OEM', help: 'Opcional. Tipo de licencia del sistema operativo: OEM, RETAIL o VOLUMEN.' },
  { key: 'lastMaintenanceDate', label: 'Fecha Ultimo Mantenimiento', type: 'date', width: 26, example: '2026-06-10', help: 'Opcional. Con esta fecha se calcula el cronograma (proximo mantenimiento).' },
  { key: 'lastMaintenanceType', label: 'Tipo Ultimo Mantenimiento', type: 'text', width: 26, list: 'mantenimientos', example: '', help: 'Opcional. Debe existir en Tipos de Mantenimiento.' },
  { key: 'lastMaintenanceTechnician', label: 'Tecnico Ultimo Mantenimiento', type: 'text', width: 26, example: '', help: 'Opcional. Nombre del tecnico.' },
  { key: 'lastMaintenanceDescription', label: 'Descripcion Ultimo Mantenimiento', type: 'text', width: 34, example: '', help: 'Opcional. Descripcion del trabajo realizado.' },
  { key: 'associatedInventoryNumber', label: 'PC Asociado (N Inventario)', type: 'text', width: 26, example: '', help: 'Solo para monitores. N Inventario Interno del PC al que se conecta el monitor.' },
];

const MAX_ROWS = 2000;
const RAM_TYPES = ['DDR3', 'DDR4', 'DDR5', 'LPDDR4', 'LPDDR5'];
const DISK_TYPES = ['HDD', 'SSD', 'NVME'];
const OPERATING_SYSTEMS = ['WINDOWS', 'LINUX', 'MACOS', 'CHROMEOS', 'ANDROID', 'IOS', 'OTRO', 'N/A'];
const OWNERSHIP = ['PROPIO', 'ALQUILADO'];
const IP_ASSIGNMENT = ['DHCP', 'FIJA'];
const OS_EDITIONS = ['WINDOWS 10', 'WINDOWS 11'];
const OS_VERSIONS_WINDOWS = ['26H2', '26H1', '25H2', '24H2', '23H2'];
const OS_LICENSES = ['OEM', 'RETAIL', 'VOLUMEN'];
const XLSX_MIME = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

interface ImportRowResult {
  rowNumber: number;
  name?: string;
  status: string;
  errors?: string[];
  warnings?: string[];
  nextMaintenanceDate?: string;
}
interface ImportResponse {
  dryRun: boolean; total: number; validos: number; conErrores: number; importados: number; rows: ImportRowResult[];
}
interface Catalogs {
  categorias: string[]; sedes: string[]; areas: string[]; centros: string[]; mantenimientos: string[];
}

/** Normaliza un encabezado: sin tildes, sin asterisco, sin texto entre parentesis, minusculas. */
function normalizeHeader(v: string): string {
  return (v || '')
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/\(.*?\)/g, ' ')
    .replace(/[*º°.#:]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim().toLowerCase();
}

function pad(n: number): string { return n < 10 ? '0' + n : '' + n; }

function colLetter(n: number): string {
  let s = '';
  while (n > 0) { const m = (n - 1) % 26; s = String.fromCharCode(65 + m) + s; n = Math.floor((n - 1) / 26); }
  return s;
}

@Component({
  selector: 'app-equipment-import',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule, MatSnackBarModule, MatProgressBarModule, MatSlideToggleModule],
  template: `
    <div class="import-page">
      <div class="header">
        <h1><mat-icon>upload_file</mat-icon> Importar Equipos</h1>
        <button mat-stroked-button routerLink="/equipment" class="action-btn">
          <mat-icon>arrow_back</mat-icon> Volver a equipos
        </button>
      </div>

      <div class="steps">
        <mat-card class="step-card">
          <div class="step-num">1</div>
          <h3>Descargar plantilla</h3>
          <p>Plantilla Excel con listas desplegables de categorias, sedes, areas, centros de costo y tipos de mantenimiento actuales.</p>
          <button mat-raised-button color="primary" (click)="downloadTemplate()" [disabled]="busy()">
            <mat-icon>download</mat-icon> Descargar plantilla
          </button>
        </mat-card>
        <mat-card class="step-card">
          <div class="step-num">2</div>
          <h3>Subir archivo</h3>
          <p>Sube el archivo .xlsx diligenciado (maximo {{ maxRows }} filas). Se validara antes de importar.</p>
          <input #fileInput type="file" accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" hidden (change)="onFileSelected($event)">
          <button mat-raised-button color="accent" (click)="fileInput.value = ''; fileInput.click()" [disabled]="busy()">
            <mat-icon>upload</mat-icon> Subir archivo
          </button>
          @if (fileName()) { <span class="file-name"><mat-icon>description</mat-icon> {{ fileName() }}</span> }
        </mat-card>
        <mat-card class="step-card">
          <div class="step-num">3</div>
          <h3>Importar</h3>
          <p>Solo se importan las filas validas. Las filas con errores se pueden descargar, corregir y volver a subir.</p>
          <button mat-raised-button color="primary" (click)="confirmImport()" [disabled]="busy() || validCount() === 0 || !!importResult()">
            <mat-icon>playlist_add_check</mat-icon> Importar {{ validCount() }} equipos validos
          </button>
        </mat-card>
      </div>

      @if (busy()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
        <p class="busy-msg">{{ busyMsg() }}</p>
      }

      @if (importResult(); as r) {
        <mat-card class="result-card">
          <mat-icon class="result-icon">task_alt</mat-icon>
          <div>
            <h3>Importacion finalizada</h3>
            <p>Se importaron <strong>{{ r.importados }}</strong> de {{ r.total }} equipos enviados.
              @if (r.conErrores > 0) { <span class="err-text">{{ r.conErrores }} filas con errores no se importaron.</span> }
            </p>
            <a mat-stroked-button routerLink="/equipment" class="action-btn"><mat-icon>list</mat-icon> Ir a la lista de equipos</a>
          </div>
        </mat-card>
      }

      @if (preview(); as p) {
        <div class="kpi-grid">
          <div class="kpi-card blue"><div class="kpi-value">{{ p.total }}</div><div class="kpi-label">Filas leidas</div></div>
          <div class="kpi-card green"><div class="kpi-value">{{ p.validos }}</div><div class="kpi-label">Validas</div></div>
          <div class="kpi-card red"><div class="kpi-value">{{ p.conErrores }}</div><div class="kpi-label">Con errores</div></div>
        </div>

        <mat-card class="table-card">
          <div class="table-header">
            <h3>Vista previa ({{ visibleRows().length }})</h3>
            <div class="table-actions">
              <mat-slide-toggle [ngModel]="onlyErrors()" (ngModelChange)="onlyErrors.set($event)">Solo errores</mat-slide-toggle>
              <button mat-stroked-button class="action-btn" (click)="downloadErrors()" [disabled]="errorRows().length === 0">
                <mat-icon>error_outline</mat-icon> Descargar errores
              </button>
            </div>
          </div>
          <div class="table-scroll">
            <table>
              <thead>
                <tr><th>Fila</th><th>Nombre</th><th>Estado</th><th>Errores</th><th>Advertencias</th><th>Prox. Mantenimiento</th></tr>
              </thead>
              <tbody>
                @for (row of visibleRows(); track row.rowNumber) {
                  <tr [class.row-error]="row.status === 'ERROR'">
                    <td>{{ row.rowNumber }}</td>
                    <td>{{ row.name || '-' }}</td>
                    <td><span class="chip" [class.ok]="row.status !== 'ERROR'" [class.error]="row.status === 'ERROR'">{{ row.status }}</span></td>
                    <td>
                      @for (e of row.errors || []; track $index) { <div class="msg err-text">{{ e }}</div> }
                      @if (!row.errors?.length) { <span class="muted">-</span> }
                    </td>
                    <td>
                      @for (w of row.warnings || []; track $index) { <div class="msg warn-text">{{ w }}</div> }
                      @if (!row.warnings?.length) { <span class="muted">-</span> }
                    </td>
                    <td>{{ row.nextMaintenanceDate || '-' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" class="muted center">Sin filas para mostrar</td></tr>
                }
              </tbody>
            </table>
          </div>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .import-page { padding: 32px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; overflow: hidden; }
    .action-btn { color: #94A3B8 !important; border-color: #475569 !important; }
    .steps { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
    .step-card { padding: 24px; display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
    .step-card h3 { color: #E2E8F0; margin: 0; }
    .step-card p { color: #94A3B8; font-size: 13px; margin: 0 0 6px; flex: 1; }
    .step-num { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: rgba(59,130,246,0.2); color: #93C5FD; font-weight: 700; }
    .file-name { display: flex; align-items: center; gap: 6px; color: #CBD5E1; font-size: 12px; word-break: break-all; }
    .file-name mat-icon { font-size: 16px; width: 16px; height: 16px; color: #60A5FA; }
    .busy-msg { color: #94A3B8; text-align: center; margin: 8px 0 16px; }
    .result-card { padding: 24px; margin-bottom: 16px; display: flex; gap: 16px; align-items: flex-start; border: 1px solid rgba(16,185,129,0.3); }
    .result-card h3 { color: #6EE7B7; margin: 0 0 8px; }
    .result-card p { color: #CBD5E1; margin: 0 0 12px; }
    .result-icon { color: #10B981; font-size: 36px; width: 36px; height: 36px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
    .kpi-card { padding: 18px; border-radius: 14px; text-align: center; }
    .kpi-value { font-size: 30px; font-weight: 700; color: #F1F5F9; }
    .kpi-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; margin-top: 4px; }
    .kpi-card.blue { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.2); }
    .kpi-card.green { background: rgba(16,185,129,0.1); border: 1px solid rgba(16,185,129,0.2); }
    .kpi-card.red { background: rgba(239,68,68,0.1); border: 1px solid rgba(239,68,68,0.2); }
    .table-card { padding: 24px; margin-bottom: 16px; }
    .table-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 12px; }
    .table-header h3 { color: #E2E8F0; margin: 0; font-size: 16px; }
    .table-actions { display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }
    .table-scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(148,163,184,0.15); white-space: nowrap; }
    td { padding: 10px 12px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); vertical-align: top; }
    tr.row-error { background: rgba(239,68,68,0.04); }
    .chip { display: inline-block; padding: 3px 10px; border-radius: 10px; font-size: 11px; font-weight: 700; }
    .chip.ok { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .chip.error { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .msg { font-size: 12px; margin-bottom: 2px; }
    .err-text { color: #FCA5A5; }
    .warn-text { color: #FBBF24; }
    .muted { color: #64748B; }
    .center { text-align: center; padding: 24px; }
    @media (max-width: 900px) {
      .import-page { padding: 16px; }
      .steps, .kpi-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class EquipmentImportComponent {
  readonly maxRows = MAX_ROWS;
  busy = signal(false);
  busyMsg = signal('');
  fileName = signal('');
  preview = signal<ImportResponse | null>(null);
  importResult = signal<ImportResponse | null>(null);
  onlyErrors = signal(false);
  /** Filas leidas del archivo, tal como se envian al backend. */
  private parsedRows: Record<string, any>[] = [];

  validCount = computed(() => (this.preview()?.rows || []).filter(r => r.status !== 'ERROR').length);
  errorRows = computed(() => (this.preview()?.rows || []).filter(r => r.status === 'ERROR'));
  visibleRows = computed(() => this.onlyErrors() ? this.errorRows() : (this.preview()?.rows || []));

  private importUrl = `${environment.apiUrl}/equipment/import`;

  constructor(
    private http: HttpClient,
    private sedeService: SedeService,
    private areaService: AreaService,
    private costCenterService: CostCenterService,
    private deviceTypeService: DeviceTypeService,
    private maintenanceCategoryService: MaintenanceCategoryService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  private setBusy(on: boolean, msg = '') { this.busy.set(on); this.busyMsg.set(msg); this.cdr.detectChanges(); }

  /** Carga exceljs bajo demanda (chunk separado). */
  private async loadExcel(): Promise<any> {
    const mod: any = await import('exceljs');
    return mod.default ?? mod;
  }

  private loadCatalogs(): Promise<Catalogs> {
    const safe = (obs: Observable<any[]>) => obs.pipe(catchError(() => of([] as any[])));
    const pick = (arr: any[]) => (arr || []).map(x => x?.name).filter((n: any) => !!n).sort((a: string, b: string) => a.localeCompare(b));
    return new Promise((resolve) => {
      forkJoin({
        categorias: safe(this.deviceTypeService.listActive()),
        sedes: safe(this.sedeService.listActive()),
        areas: safe(this.areaService.listActive()),
        centros: safe(this.costCenterService.listActive()),
        mantenimientos: safe(this.maintenanceCategoryService.listActive()),
      }).subscribe((r) => resolve({
        categorias: pick(r.categorias), sedes: pick(r.sedes), areas: pick(r.areas),
        centros: pick(r.centros), mantenimientos: pick(r.mantenimientos)
      }));
    });
  }

  private saveWorkbook(buf: ArrayBuffer, fileName: string) {
    const blob = new Blob([buf], { type: XLSX_MIME });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = fileName;
    link.click();
    setTimeout(() => URL.revokeObjectURL(link.href), 2000);
  }

  private styleHeader(row: any) {
    row.font = { bold: true, color: { argb: 'FFFFFFFF' } };
    row.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true };
    row.height = 32;
    row.eachCell((c: any) => {
      c.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF1E40AF' } };
      c.border = { bottom: { style: 'thin', color: { argb: 'FF93C5FD' } } };
    });
  }

  async downloadTemplate() {
    this.setBusy(true, 'Generando plantilla...');
    try {
      const [ExcelJS, cat] = await Promise.all([this.loadExcel(), this.loadCatalogs()]);
      const wb = new ExcelJS.Workbook();
      wb.creator = 'THOTH C.O.R.E.';
      wb.created = new Date();

      const ws = wb.addWorksheet('Equipos', { views: [{ state: 'frozen', ySplit: 1 }] });
      const wi = wb.addWorksheet('Instrucciones');
      const wl = wb.addWorksheet('Listas');
      wl.state = 'hidden';

      // Hoja de listas (oculta): una columna por catalogo
      const lists: Record<ListKey, string[]> = {
        categorias: cat.categorias, sedes: cat.sedes, areas: cat.areas, centros: cat.centros,
        propiedad: OWNERSHIP, ramTypes: RAM_TYPES, diskTypes: DISK_TYPES, sistemas: OPERATING_SYSTEMS, mantenimientos: cat.mantenimientos, ipAsignacion: IP_ASSIGNMENT,
        osEdiciones: OS_EDITIONS, osVersiones: OS_VERSIONS_WINDOWS, osLicencias: OS_LICENSES
      };
      const listTitles: Record<ListKey, string> = {
        categorias: 'Categorias', sedes: 'Sedes', areas: 'Areas', centros: 'Centros de Costo',
        propiedad: 'Propiedad', ramTypes: 'Tipo RAM', diskTypes: 'Tipo Disco', sistemas: 'Sistema Operativo', mantenimientos: 'Tipos de Mantenimiento', ipAsignacion: 'Asignacion IP',
        osEdiciones: 'Software SO', osVersiones: 'Version Windows', osLicencias: 'Tipo de Licencia'
      };
      const listRef: Partial<Record<ListKey, string>> = {};
      (Object.keys(lists) as ListKey[]).forEach((k, i) => {
        const col = i + 1;
        const letter = colLetter(col);
        wl.getCell(1, col).value = listTitles[k];
        wl.getCell(1, col).font = { bold: true };
        lists[k].forEach((v, j) => { wl.getCell(j + 2, col).value = v; });
        wl.getColumn(col).width = 28;
        if (lists[k].length) listRef[k] = `Listas!$${letter}$2:$${letter}$${lists[k].length + 1}`;
      });

      // Hoja Equipos: encabezados + fila de ejemplo
      ws.columns = COLUMNS.map(c => ({ header: c.label, key: c.key, width: c.width }));
      this.styleHeader(ws.getRow(1));
      const example: Record<string, any> = {};
      COLUMNS.forEach(c => {
        let v: any = c.example;
        if (c.list && v === '' && lists[c.list]?.length) v = lists[c.list][0];
        example[c.key] = v === '' ? null : v;
      });
      const exRow = ws.addRow(example);
      exRow.font = { italic: true, color: { argb: 'FF64748B' } };

      // Columnas de texto con formato texto para que Excel no altere seriales/MAC
      COLUMNS.forEach((c, i) => { if (c.type === 'text') ws.getColumn(i + 1).numFmt = '@'; });

      // Listas desplegables (data validation) alimentadas desde la hoja oculta "Listas"
      COLUMNS.forEach((c, i) => {
        const ref = c.list ? listRef[c.list] : undefined;
        if (!c.list || !ref) return;
        const letter = colLetter(i + 1);
        const validation = {
          type: 'list', allowBlank: true, formulae: [ref],
          showErrorMessage: c.strict !== false, errorStyle: 'warning', errorTitle: 'Valor no valido',
          error: 'Seleccione un valor de la lista (' + listTitles[c.list] + ').'
        };
        const dv: any = (ws as any).dataValidations;
        if (dv && typeof dv.add === 'function') {
          dv.add(`${letter}2:${letter}${MAX_ROWS + 1}`, validation);
        } else {
          for (let r = 2; r <= MAX_ROWS + 1; r++) ws.getCell(`${letter}${r}`).dataValidation = validation;
        }
      });

      // Hoja de instrucciones
      wi.columns = [{ width: 34 }, { width: 14 }, { width: 80 }];
      wi.addRow(['Instrucciones de importacion de equipos']).font = { bold: true, size: 14 };
      wi.addRow([]);
      [
        '1. Diligencie la hoja "Equipos" a partir de la fila 2 (puede borrar o reemplazar la fila de ejemplo). No cambie los encabezados.',
        '2. Las columnas marcadas con * son obligatorias (Nombre y Categoria).',
        '3. Categoria, Sede, Area, Centro de Costo y Tipo Ultimo Mantenimiento deben existir en los catalogos del sistema; use las listas desplegables. La importacion no crea catalogos nuevos.',
        '4. Las fechas deben ir en formato AAAA-MM-DD (o como fecha de Excel).',
        '5. Los valores en pesos van solo con numeros, sin puntos, comas ni simbolos.',
        '6. Si el equipo es ALQUILADO, diligencie los datos del alquiler (empresa, contrato, fechas y valor mensual).',
        '7. El cronograma de mantenimiento se calcula desde la Fecha Ultimo Mantenimiento: si se informa, se registra ese mantenimiento y se programa el proximo automaticamente.',
        '8. Sistema Operativo es opcional y debe ser uno de: ' + OPERATING_SYSTEMS.join(', ') + ' (use la lista desplegable). Si es WINDOWS: en "Software SO" elija ' + OS_EDITIONS.join(' o ') + ' y en "Distribucion / Version" use una de las versiones validas: ' + OS_VERSIONS_WINDOWS.join(', ') + '. Para otros sistemas "Distribucion / Version" es texto libre (ej: UBUNTU 24.04). "Tipo de Licencia": ' + OS_LICENSES.join(', ') + '.',
        '9. Datos del responsable (opcionales): Cargo, Documento, Celular (10 digitos, solo numeros) y Correo. En la red puede indicar la Direccion IP (IPv4) y la Asignacion IP (DHCP o FIJA, use la lista desplegable).',
        '10. Los monitores se registran como cualquier equipo: pueden ser PROPIOS o ALQUILADOS (con empresa, contrato, fechas y valor mensual). Para asociarlos a un PC escriba en "PC Asociado (N Inventario)" el N Inventario Interno del PC (el PC debe estar registrado en THOTH).',
        '11. Maximo ' + MAX_ROWS + ' filas por archivo. Antes de importar se muestra una vista previa con los errores de cada fila.',
      ].forEach(t => { const r = wi.addRow([t]); wi.mergeCells(`A${r.number}:C${r.number}`); r.alignment = { wrapText: true }; r.height = 30; });
      wi.addRow([]);
      this.styleHeader(wi.addRow(['Columna', 'Obligatorio', 'Descripcion']));
      COLUMNS.forEach(c => {
        const r = wi.addRow([c.label.replace('*', ''), c.label.endsWith('*') ? 'SI' : 'NO', c.help]);
        r.alignment = { wrapText: true, vertical: 'top' };
      });

      const buf = await wb.xlsx.writeBuffer();
      this.saveWorkbook(buf, 'Plantilla_Importacion_Equipos_THOTH.xlsx');
      this.snackBar.open('Plantilla descargada', 'OK', { duration: 3000 });
    } catch (e) {
      console.error(e);
      this.snackBar.open('Error al generar la plantilla', 'OK', { duration: 4000 });
    } finally {
      this.setBusy(false);
    }
  }

  /** Extrae el valor "plano" de una celda de exceljs (texto enriquecido, formulas, hipervinculos). */
  private cellRaw(v: any): any {
    if (v === null || v === undefined) return null;
    if (v instanceof Date) return v;
    if (typeof v === 'object') {
      if (Array.isArray(v.richText)) return v.richText.map((t: any) => t.text).join('');
      if ('result' in v) return this.cellRaw(v.result);
      if ('text' in v) return this.cellRaw(v.text);
      if ('error' in v) return null;
      return String(v);
    }
    return v;
  }

  private toDateString(v: any): string | null {
    if (v === null || v === undefined || v === '') return null;
    if (v instanceof Date) {
      if (isNaN(v.getTime())) return null;
      return `${v.getUTCFullYear()}-${pad(v.getUTCMonth() + 1)}-${pad(v.getUTCDate())}`;
    }
    if (typeof v === 'number') {
      // Numero serial de Excel (dias desde 1899-12-30)
      const d = new Date(Math.round((v - 25569) * 86400000));
      return isNaN(d.getTime()) ? String(v) : `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`;
    }
    const s = String(v).trim();
    if (!s) return null;
    let m = s.match(/^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})/);
    if (m) return `${m[1]}-${pad(+m[2])}-${pad(+m[3])}`;
    m = s.match(/^(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})$/);
    if (m) return `${m[3]}-${pad(+m[2])}-${pad(+m[1])}`;
    if (/^\d+(\.\d+)?$/.test(s)) return this.toDateString(Number(s));
    return s; // el backend reporta el error de formato
  }

  private toNumber(v: any): number | string | null {
    if (v === null || v === undefined || v === '') return null;
    if (typeof v === 'number') return v;
    const s = String(v).trim().replace(/[$\s]/g, '');
    if (!s) return null;
    if (/^-?\d+([.,]\d{1,2})?$/.test(s)) return Number(s.replace(',', '.'));
    if (/^-?\d{1,3}([.,]\d{3})+$/.test(s)) return Number(s.replace(/[.,]/g, ''));
    return s; // el backend reporta el error de formato
  }

  private toText(v: any): string | null {
    if (v === null || v === undefined) return null;
    if (v instanceof Date) return this.toDateString(v);
    const s = String(v).trim();
    return s ? s : null;
  }

  async onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      this.snackBar.open('Solo se aceptan archivos .xlsx', 'OK', { duration: 4000 });
      return;
    }
    this.fileName.set(file.name);
    this.preview.set(null);
    this.importResult.set(null);
    this.onlyErrors.set(false);
    this.parsedRows = [];
    this.setBusy(true, 'Leyendo archivo...');
    let rows: Record<string, any>[] = [];
    try {
      const ExcelJS = await this.loadExcel();
      const wb = new ExcelJS.Workbook();
      await wb.xlsx.load(await file.arrayBuffer());
      const ws = wb.getWorksheet('Equipos') ?? wb.worksheets.find((w: any) => w.state !== 'hidden') ?? wb.worksheets[0];
      if (!ws) throw new Error('el archivo no contiene hojas');

      // Mapeo de encabezados por nombre normalizado
      const byHeader = new Map<string, ColumnDef>();
      COLUMNS.forEach(c => byHeader.set(normalizeHeader(c.label), c));
      const colMap = new Map<number, ColumnDef>();
      ws.getRow(1).eachCell({ includeEmpty: false }, (cell: any, colNumber: number) => {
        const def = byHeader.get(normalizeHeader(String(this.cellRaw(cell.value) ?? '')));
        if (def && !Array.from(colMap.values()).includes(def)) colMap.set(colNumber, def);
      });
      const mapped = new Set(Array.from(colMap.values()).map(c => c.key));
      if (!mapped.has('name') || !mapped.has('category')) {
        throw new Error('no se encontraron las columnas "Nombre" y "Categoria". Use la plantilla');
      }

      for (let r = 2; r <= ws.rowCount; r++) {
        const row = ws.getRow(r);
        const data: Record<string, any> = { rowNumber: r };
        let hasValue = false;
        colMap.forEach((def, col) => {
          const raw = this.cellRaw(row.getCell(col).value);
          let val: any;
          if (def.type === 'date') val = this.toDateString(raw);
          else if (def.type === 'number') val = this.toNumber(raw);
          else val = this.toText(raw);
          if ((def.key === 'ownershipType' || def.key === 'operatingSystem' || def.key === 'ipAssignment' || def.key === 'osEdition' || def.key === 'osLicenseType') && typeof val === 'string') val = val.trim().toUpperCase();
          data[def.key] = val ?? null;
          if (val !== null && val !== undefined && val !== '') hasValue = true;
        });
        if (!hasValue) continue;
        COLUMNS.forEach(c => { if (!(c.key in data)) data[c.key] = null; });
        // Version de Windows en mayusculas (ej: 24h2 -> 24H2); otros SO conservan el texto libre
        if (data['operatingSystem'] === 'WINDOWS' && typeof data['osVersion'] === 'string') data['osVersion'] = data['osVersion'].trim().toUpperCase();
        rows.push(data);
      }
    } catch (e: any) {
      console.error(e);
      this.setBusy(false);
      this.snackBar.open('No se pudo leer el archivo: ' + (e?.message || 'formato invalido'), 'OK', { duration: 6000 });
      return;
    }

    if (rows.length === 0) {
      this.setBusy(false);
      this.snackBar.open('El archivo no contiene filas con datos', 'OK', { duration: 4000 });
      return;
    }
    if (rows.length > MAX_ROWS) {
      this.setBusy(false);
      this.snackBar.open(`El archivo tiene ${rows.length} filas; el maximo es ${MAX_ROWS}`, 'OK', { duration: 6000 });
      return;
    }
    this.parsedRows = rows;
    this.setBusy(true, `Validando ${rows.length} filas...`);
    this.http.post<ImportResponse>(`${this.importUrl}?dryRun=true`, { rows }).subscribe({
      next: (res) => {
        this.preview.set(this.normalizeResponse(res));
        this.setBusy(false);
      },
      error: (e) => {
        this.setBusy(false);
        this.snackBar.open(e.error?.message || 'Error al validar el archivo', 'OK', { duration: 6000 });
      }
    });
  }

  private normalizeResponse(res: ImportResponse): ImportResponse {
    return {
      dryRun: !!res?.dryRun, total: res?.total || 0, validos: res?.validos || 0, conErrores: res?.conErrores || 0,
      importados: res?.importados || 0,
      rows: (res?.rows || []).slice().sort((a, b) => a.rowNumber - b.rowNumber)
    };
  }

  /** Envia al backend solo las filas que pasaron la validacion previa. */
  confirmImport() {
    const okRows = new Set((this.preview()?.rows || []).filter(r => r.status !== 'ERROR').map(r => r.rowNumber));
    const rows = this.parsedRows.filter(r => okRows.has(r['rowNumber']));
    if (rows.length === 0) return;
    if (!confirm(`Se importaran ${rows.length} equipos. Desea continuar?`)) return;
    this.setBusy(true, `Importando ${rows.length} equipos...`);
    this.http.post<ImportResponse>(`${this.importUrl}?dryRun=false`, { rows }).subscribe({
      next: (res) => {
        const r = this.normalizeResponse(res);
        this.importResult.set(r);
        // Actualiza la vista previa con el estado final de cada fila enviada
        const byRow = new Map(r.rows.map(x => [x.rowNumber, x] as [number, ImportRowResult]));
        const prev = this.preview();
        if (prev) this.preview.set({ ...prev, rows: prev.rows.map(x => byRow.get(x.rowNumber) ?? x) });
        this.setBusy(false);
        this.snackBar.open(`${r.importados} equipos importados`, 'OK', { duration: 4000 });
      },
      error: (e) => {
        this.setBusy(false);
        this.snackBar.open(e.error?.message || 'Error al importar los equipos', 'OK', { duration: 6000 });
      }
    });
  }

  async downloadErrors() {
    const rows = this.errorRows();
    if (!rows.length) return;
    try {
      const ExcelJS = await this.loadExcel();
      const wb = new ExcelJS.Workbook();
      const ws = wb.addWorksheet('Errores');
      ws.columns = [
        { header: 'Fila', key: 'rowNumber', width: 8 },
        { header: 'Nombre', key: 'name', width: 30 },
        { header: 'Errores', key: 'errors', width: 90 }
      ];
      this.styleHeader(ws.getRow(1));
      rows.forEach(r => {
        const row = ws.addRow({ rowNumber: r.rowNumber, name: r.name || '', errors: (r.errors || []).join('\n') });
        row.alignment = { wrapText: true, vertical: 'top' };
      });
      const buf = await wb.xlsx.writeBuffer();
      this.saveWorkbook(buf, 'Errores_Importacion_' + new Date().toISOString().split('T')[0] + '.xlsx');
    } catch (e) {
      console.error(e);
      this.snackBar.open('Error al generar el archivo de errores', 'OK', { duration: 4000 });
    }
  }
}
