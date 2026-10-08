import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EquipmentService } from '../../core/services/equipment.service';
import { Equipment } from '../../core/models/equipment.model';
import { DeviceTypeService, DeviceType } from '../../core/services/device-type.service';
import { AuthService } from '../../core/services/auth.service';
import { EtiquetaPipe, etiqueta, codigoEstado } from '../../core/pipes/etiqueta.pipe';

@Component({
  selector: 'app-equipment-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatInputModule, MatFormFieldModule, MatSelectModule, MatSnackBarModule, EtiquetaPipe],
  template: `
    <div class="equipment-list">
      <div class="header">
        <h2>Equipos</h2>
        <div class="header-actions">
          <button mat-stroked-button (click)="exportToExcel()" class="export-btn">
            <mat-icon>download</mat-icon> Exportar Excel
          </button>
          @if (auth.hasPermission('IMPORT', 'CREATE')) {
          <button mat-stroked-button routerLink="/equipment/import" class="export-btn">
            <mat-icon>upload_file</mat-icon> Importar
          </button>
          }
          <button mat-raised-button color="primary" routerLink="/equipment/new">
            <mat-icon>add</mat-icon> Nuevo Equipo
          </button>
        </div>
      </div>

      <div class="search-bar">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Buscar equipo...</mat-label>
          <input matInput [ngModel]="searchTerm()" (ngModelChange)="searchTerm.set($event)" placeholder="Nombre, serial, MAC, marca, asignado...">
          <mat-icon matPrefix>search</mat-icon>
          @if (searchTerm()) {
            <button mat-icon-button matSuffix (click)="clearSearch()">
              <mat-icon>close</mat-icon>
            </button>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Estado</mat-label>
          <mat-select [ngModel]="statusFilter()" (ngModelChange)="statusFilter.set($event)">
            <mat-option value="">Todos</mat-option>
            <mat-option value="ACTIVE">Activos</mat-option>
            <mat-option value="MAINTENANCE">Mantenimiento</mat-option>
            <mat-option value="INACTIVE">Inactivos</mat-option>
            <mat-option value="RETIRED">Retirados</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Categoria</mat-label>
          <mat-select [ngModel]="categoryFilter()" (ngModelChange)="categoryFilter.set($event)">
            <mat-option value="">Todas</mat-option>
            @for (cat of availableCategories(); track cat) {
              <mat-option [value]="cat">{{ cat }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Sede</mat-label>
          <mat-select [ngModel]="sedeFilter()" (ngModelChange)="sedeFilter.set($event)">
            <mat-option value="">Todas</mat-option>
            @for (sede of availableSedes(); track sede) {
              <mat-option [value]="sede">{{ sede }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </div>

      <div class="results-info">
        <span>{{ filteredEquipments().length }} de {{ allEquipments().length }} equipos</span>
      </div>

      @if (loading()) {
        <p>Cargando equipos...</p>
      } @else if (filteredEquipments().length === 0) {
        <div class="empty-state">
          <mat-icon class="empty-icon">{{ searchTerm() || statusFilter() || categoryFilter() || sedeFilter() ? 'search_off' : 'inventory_2' }}</mat-icon>
          <h3>{{ searchTerm() || statusFilter() || categoryFilter() || sedeFilter() ? 'Sin resultados' : 'No hay equipos registrados' }}</h3>
          <p>{{ searchTerm() || statusFilter() || categoryFilter() || sedeFilter() ? 'Intenta con otros terminos de busqueda' : 'Registra tu primer equipo para comenzar' }}</p>
          @if (!searchTerm() && !statusFilter() && !categoryFilter() && !sedeFilter()) {
            <button mat-raised-button color="primary" routerLink="/equipment/new">Registrar Equipo</button>
          }
        </div>
      } @else {
        <table mat-table [dataSource]="filteredEquipments()" class="mat-elevation-z2 full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let e">
              <a [routerLink]="['/equipment', e.equipmentId]" class="link">{{ e.name }}</a>
            </td>
          </ng-container>
          <ng-container matColumnDef="inventoryNumber">
            <th mat-header-cell *matHeaderCellDef>N Inventario</th>
            <td mat-cell *matCellDef="let e">
              <span class="inv-number">{{ e.inventoryNumber || '-' }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Categoria</th>
            <td mat-cell *matCellDef="let e">{{ e.category }}</td>
          </ng-container>
          <ng-container matColumnDef="serialNumber">
            <th mat-header-cell *matHeaderCellDef>Serial</th>
            <td mat-cell *matCellDef="let e">{{ e.serialNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Estado</th>
            <td mat-cell *matCellDef="let e">
              <span class="status-badge" [class]="getStatusClass(e.status)">{{ e.status | etiqueta:'estado' }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="assignedTo">
            <th mat-header-cell *matHeaderCellDef>Asignado</th>
            <td mat-cell *matCellDef="let e">{{ e.assignedTo || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="location">
            <th mat-header-cell *matHeaderCellDef>Ubicacion</th>
            <td mat-cell *matCellDef="let e">{{ e.location?.building || '-' }} - {{ e.location?.office || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Acciones</th>
            <td mat-cell *matCellDef="let e">
              <button mat-icon-button [routerLink]="['/equipment', e.equipmentId]">
                <mat-icon color="primary">visibility</mat-icon>
              </button>
              <button mat-icon-button [routerLink]="['/ai', e.equipmentId]">
                <mat-icon color="accent">psychology</mat-icon>
              </button>
              <button mat-icon-button (click)="deleteEquipment(e.equipmentId)">
                <mat-icon color="warn">delete</mat-icon>
              </button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>
      }
    </div>
  `,
  styles: [`
    .equipment-list { padding: 24px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
    h2 { margin: 0; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-size: 24px; }
    .header-actions { display: flex; gap: 12px; }
    .export-btn { color: #10B981 !important; border-color: #10B981 !important; }
    .search-bar { display: flex; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
    .search-field { flex: 2; min-width: 250px; }
    .filter-field { flex: 1; min-width: 140px; }
    .results-info { color: #94A3B8; font-size: 13px; margin-bottom: 12px; }
    .full-width { width: 100%; }
    .inv-number { color: #A78BFA; font-family: 'Courier New', monospace; font-weight: 600; font-size: 12px; }
    .inv-number { color: #A78BFA; font-family: 'Courier New', monospace; font-weight: 600; font-size: 12px; }
    .link { color: #60A5FA !important; text-decoration: none; font-weight: 600; }
    .link:hover { text-decoration: underline; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; }
    .status-active { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .status-maintenance { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .status-inactive { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .status-retired { background: rgba(100,116,139,0.2); color: #CBD5E1; }
    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #64748B; }
    .empty-state h3 { color: #E2E8F0; }
    .empty-state p { color: #94A3B8; margin-bottom: 16px; }
    @media (max-width: 768px) {
      .search-bar { flex-direction: column; }
      .search-field, .filter-field { min-width: 100%; }
      .header { flex-direction: column; align-items: stretch; }
      .header-actions { justify-content: space-between; }
    }
  `]
})
export class EquipmentListComponent implements OnInit {
  allEquipments = signal<Equipment[]>([]);
  loading = signal(true);
  searchTerm = signal('');
  statusFilter = signal('');
  categoryFilter = signal('');
  sedeFilter = signal('');
  deviceTypes = signal<DeviceType[]>([]);

  /** Categorias del catalogo de tipos de dispositivo, mas las que ya tengan equipos y no esten en el catalogo. */
  availableCategories = computed(() => {
    const vistas = new Set<string>();
    const result: string[] = [];
    const add = (c?: string) => {
      const v = (c || '').trim();
      if (v && !vistas.has(v.toUpperCase())) { vistas.add(v.toUpperCase()); result.push(v); }
    };
    this.deviceTypes().forEach(t => add(t.name));
    this.allEquipments().forEach(e => add(e.category));
    return result.sort((a, b) => a.localeCompare(b));
  });
  columns = ['name', 'inventoryNumber', 'category', 'serialNumber', 'status', 'assignedTo', 'location', 'actions'];

  availableSedes = computed(() => {
    const sedes = new Set<string>();
    this.allEquipments().forEach(e => {
      if (e.location?.building) sedes.add(e.location.building);
    });
    return Array.from(sedes).sort();
  });

  filteredEquipments = computed(() => {
    let result = this.allEquipments();
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      result = result.filter(e =>
        e.name?.toLowerCase().includes(term) ||
        e.serialNumber?.toLowerCase().includes(term) ||
        e.inventoryNumber?.toLowerCase().includes(term) ||
        e.macAddress?.toLowerCase().includes(term) ||
        e.brand?.toLowerCase().includes(term) ||
        e.model?.toLowerCase().includes(term) ||
        e.assignedTo?.toLowerCase().includes(term) ||
        e.location?.building?.toLowerCase().includes(term) ||
        e.location?.office?.toLowerCase().includes(term)
      );
    }
    if (this.statusFilter()) {
      const estado = this.statusFilter();
      result = result.filter(e => codigoEstado(e.status) === estado);
    }
    if (this.categoryFilter()) {
      const cat = this.categoryFilter().trim().toUpperCase();
      result = result.filter(e => (e.category || '').trim().toUpperCase() === cat);
    }
    if (this.sedeFilter()) {
      result = result.filter(e => e.location?.building === this.sedeFilter());
    }
    return result;
  });

  constructor(
    private equipmentService: EquipmentService,
    private deviceTypeService: DeviceTypeService,
    public auth: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadEquipments();
    this.deviceTypeService.listActive().subscribe({
      next: (types) => { this.deviceTypes.set(types || []); this.cdr.detectChanges(); },
      error: () => this.deviceTypes.set([])
    });
  }

  loadEquipments() {
    this.loading.set(true);
    this.equipmentService.list(0, 500).subscribe({
      next: (res) => { this.allEquipments.set(res.content || []); this.loading.set(false); this.cdr.detectChanges(); },
      error: () => { this.allEquipments.set([]); this.loading.set(false); this.cdr.detectChanges(); }
    });
  }

  

  clearSearch() {
    this.searchTerm = signal('');
    this.cdr.detectChanges();
  }

  getStatusClass(status: string): string {
    switch (codigoEstado(status)) {
      case 'ACTIVE': return 'status-badge status-active';
      case 'MAINTENANCE': return 'status-badge status-maintenance';
      case 'INACTIVE': return 'status-badge status-inactive';
      case 'RETIRED': return 'status-badge status-retired';
      default: return 'status-badge';
    }
  }

  deleteEquipment(id: string) {
    if (confirm('Seguro que deseas retirar este equipo?')) {
      this.equipmentService.delete(id).subscribe({
        next: () => { this.snackBar.open('Equipo retirado', 'OK', { duration: 3000 }); this.loadEquipments(); },
        error: () => this.snackBar.open('Error al retirar', 'OK', { duration: 3000 })
      });
    }
  }

  exportToExcel() {
    const data = this.filteredEquipments();
    if (data.length === 0) {
      this.snackBar.open('No hay datos para exportar', 'OK', { duration: 3000 });
      return;
    }

    const headers = ['Nombre', 'N Inventario', 'Categoria', 'Serial', 'MAC', 'MAC WiFi', 'Marca', 'Modelo', 'Estado', 'Fecha Compra', 'Valor', 'Asignado a', 'Sede', 'Area', 'Centro de Costo', 'Propiedad', 'Procesador', 'RAM (GB)', 'Tipo RAM', 'Tipo Disco', 'Disco (GB)', 'Sistema Operativo', 'Distribucion / Version', 'Salud Disco', 'Temp Disco'];
    const rows = data.map(e => [
      e.name, e.inventoryNumber || '', e.category, e.serialNumber || '', e.macAddress || '', e.macAddress2 || '', e.brand || '', e.model || '',
      etiqueta(e.status, 'estado'), e.purchaseDate, e.purchaseValue,
      e.assignedTo || '', e.location?.building || '', e.location?.office || '', e.costCenter || '',
      etiqueta(e.ownershipType || 'OWNED', 'propiedad'),
      e.hardware?.processor || '', e.hardware?.ramSizeGb || '', e.hardware?.ramType || '',
      e.hardware?.diskType || '', e.hardware?.diskSizeGb || '',
      e.operatingSystem ? etiqueta(e.operatingSystem, 'so') : '', e.osVersion || '',
      e.hardware?.diskHealthPercent || '',
      e.hardware?.diskTemperatureCelsius || ''
    ]);

    let csv = '\uFEFF';
    csv += headers.join(';') + '\n';
    rows.forEach(row => {
      csv += row.map(cell => '"' + (cell?.toString() || '') + '"').join(';') + '\n';
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    const fecha = new Date().toISOString().split('T')[0];
    link.download = 'Equipos_THOTH_' + fecha + '.csv';
    link.click();

    this.snackBar.open('Archivo exportado: ' + link.download, 'OK', { duration: 3000 });
  }
}