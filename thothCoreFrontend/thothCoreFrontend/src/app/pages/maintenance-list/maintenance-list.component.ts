import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { EquipmentService } from '../../core/services/equipment.service';
import { Equipment } from '../../core/models/equipment.model';

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSortModule],
  template: `
    <div class="maintenance-page">
      <div class="header">
        <button mat-icon-button routerLink="/dashboard">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>build</mat-icon> Equipos en Mantenimiento</h2>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Buscar...</mat-label>
          <input matInput [ngModel]="searchTerm()" (ngModelChange)="searchTerm.set($event)" placeholder="Nombre, serial, asignado...">
          <mat-icon matPrefix>search</mat-icon>
          @if (searchTerm()) {
            <button mat-icon-button matSuffix (click)="searchTerm.set('')">
              <mat-icon>close</mat-icon>
            </button>
          }
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
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Categoria</mat-label>
          <mat-select [ngModel]="categoryFilter()" (ngModelChange)="categoryFilter.set($event)">
            <mat-option value="">Todas</mat-option>
            <mat-option value="LAPTOP">Laptop</mat-option>
            <mat-option value="DESKTOP">Desktop</mat-option>
            <mat-option value="SERVER">Servidor</mat-option>
            <mat-option value="MONITOR">Monitor</mat-option>
            <mat-option value="PRINTER">Impresora</mat-option>
            <mat-option value="NETWORK_DEVICE">Red</mat-option>
            <mat-option value="PERIPHERAL">Periferico</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="results-info">
        <span>{{ filtered().length }} equipos en mantenimiento</span>
      </div>

      @if (loading()) {
        <p>Cargando equipos...</p>
      } @else if (filtered().length === 0) {
        <mat-card class="empty-state">
          <mat-icon class="empty-icon">{{ searchTerm() || sedeFilter() || categoryFilter() ? 'search_off' : 'check_circle' }}</mat-icon>
          <h3>{{ searchTerm() || sedeFilter() || categoryFilter() ? 'Sin resultados' : 'No hay equipos en mantenimiento' }}</h3>
          <p>{{ searchTerm() || sedeFilter() || categoryFilter() ? 'Intenta con otros filtros' : 'Todos los equipos estan operativos' }}</p>
          @if (!searchTerm() && !sedeFilter() && !categoryFilter()) {
            <button mat-raised-button color="primary" routerLink="/equipment">Ver todos los equipos</button>
          }
        </mat-card>
      } @else {
        <table mat-table [dataSource]="filtered()" matSort (matSortChange)="sortData($event)" class="mat-elevation-z2 full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Nombre</th>
            <td mat-cell *matCellDef="let e">
              <a [routerLink]="['/equipment', e.equipmentId]" class="link">{{ e.name }}</a>
            </td>
          </ng-container>
          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Categoria</th>
            <td mat-cell *matCellDef="let e">{{ e.category }}</td>
          </ng-container>
          <ng-container matColumnDef="serialNumber">
            <th mat-header-cell *matHeaderCellDef>Serial</th>
            <td mat-cell *matCellDef="let e">{{ e.serialNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="assignedTo">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Asignado a</th>
            <td mat-cell *matCellDef="let e">{{ e.assignedTo || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="location">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Ubicacion</th>
            <td mat-cell *matCellDef="let e">{{ e.location?.building || '-' }} - {{ e.location?.office || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Acciones</th>
            <td mat-cell *matCellDef="let e">
              <button mat-icon-button [routerLink]="['/equipment', e.equipmentId]">
                <mat-icon color="primary">visibility</mat-icon>
              </button>
              <button mat-icon-button [routerLink]="['/equipment', e.equipmentId, 'history']">
                <mat-icon style="color:#F59E0B;">build</mat-icon>
              </button>
              <button mat-icon-button [routerLink]="['/ai', e.equipmentId]">
                <mat-icon color="accent">psychology</mat-icon>
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
    .maintenance-page { padding: 24px; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 {
      display: flex; align-items: center; gap: 8px; margin: 0;
      background: linear-gradient(135deg, #F59E0B, #EF4444);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text; font-size: 24px;
    }
    .filters { display: flex; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
    .search-field { flex: 2; min-width: 200px; }
    .filter-field { flex: 1; min-width: 140px; }
    .results-info { color: #94A3B8; font-size: 13px; margin-bottom: 12px; }
    .full-width { width: 100%; }
    .link { color: #60A5FA !important; text-decoration: none; font-weight: 600; }
    .link:hover { text-decoration: underline; }
    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #64748B; }
    .empty-state h3 { color: #E2E8F0; }
    .empty-state p { color: #94A3B8; margin-bottom: 16px; }
    @media (max-width: 768px) {
      .filters { flex-direction: column; }
      .search-field, .filter-field { min-width: 100%; }
    }
  `]
})
export class MaintenanceListComponent implements OnInit {
  allEquipments = signal<Equipment[]>([]);
  loading = signal(true);
  searchTerm = signal('');
  sedeFilter = signal('');
  categoryFilter = signal('');
  sortField = '';
  sortDirection: 'asc' | 'desc' | '' = '';
  columns = ['name', 'category', 'serialNumber', 'assignedTo', 'location', 'actions'];

  availableSedes = computed(() => {
    const sedes = new Set<string>();
    this.allEquipments().forEach(e => {
      if (e.location?.building) sedes.add(e.location.building);
    });
    return Array.from(sedes).sort();
  });

  filtered = computed(() => {
    let result = this.allEquipments();
    if (this.searchTerm()) {
      const term = this.searchTerm().toLowerCase();
      result = result.filter(e =>
        e.name?.toLowerCase().includes(term) ||
        e.serialNumber?.toLowerCase().includes(term) ||
        e.assignedTo?.toLowerCase().includes(term) ||
        e.brand?.toLowerCase().includes(term)
      );
    }
    if (this.sedeFilter()) {
      result = result.filter(e => e.location?.building === this.sedeFilter());
    }
    if (this.categoryFilter()) {
      result = result.filter(e => e.category === this.categoryFilter());
    }
    // Sort
    if (this.sortField && this.sortDirection) {
      result = [...result].sort((a: any, b: any) => {
        let valA = a[this.sortField] || '';
        let valB = b[this.sortField] || '';
        if (this.sortField === 'location') {
          valA = a.location?.building || '';
          valB = b.location?.building || '';
        }
        const cmp = valA.toString().localeCompare(valB.toString());
        return this.sortDirection === 'asc' ? cmp : -cmp;
      });
    }
    return result;
  });

  constructor(private equipmentService: EquipmentService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.equipmentService.list(0, 500).subscribe({
      next: (res) => {
        const inMaintenance = (res.content || []).filter(e => {
          const s = e.status?.toLowerCase() || '';
          return s.includes('mantenimiento') || s.includes('maintenance');
        });
        this.allEquipments.set(inMaintenance);
        this.loading.set(false);
        this.cdr.detectChanges();
      },
      error: () => {
        this.allEquipments.set([]);
        this.loading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  sortData(sort: Sort) {
    this.sortField = sort.active;
    this.sortDirection = sort.direction;
    this.cdr.detectChanges();
  }
}
