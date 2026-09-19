import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { EquipmentService } from '../../core/services/equipment.service';
import { Equipment } from '../../core/models/equipment.model';

@Component({
  selector: 'app-equipment-by-status',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatButtonModule, MatIconModule, MatCardModule],
  template: `
    <div class="page">
      <div class="header">
        <button mat-icon-button routerLink="/dashboard">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>{{ icon() }}</mat-icon> Equipos {{ statusLabel() }}</h2>
      </div>
      @if (loading()) {
        <p>Cargando equipos...</p>
      } @else if (equipments().length === 0) {
        <mat-card class="empty-state">
          <mat-icon class="empty-icon">inventory_2</mat-icon>
          <h3>No hay equipos en estado {{ statusLabel() }}</h3>
        </mat-card>
      } @else {
        <table mat-table [dataSource]="equipments()" class="mat-elevation-z2 full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nombre</th>
            <td mat-cell *matCellDef="let e">
              <a [routerLink]="['/equipment', e.equipmentId]" class="link">{{ e.name }}</a>
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
          <ng-container matColumnDef="assignedTo">
            <th mat-header-cell *matHeaderCellDef>Asignado a</th>
            <td mat-cell *matCellDef="let e">{{ e.assignedTo || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="location">
            <th mat-header-cell *matHeaderCellDef>Ubicacion</th>
            <td mat-cell *matCellDef="let e">{{ e.location?.building || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Acciones</th>
            <td mat-cell *matCellDef="let e">
              <button mat-icon-button [routerLink]="['/equipment', e.equipmentId]">
                <mat-icon color="primary">visibility</mat-icon>
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
    .page { padding: 24px; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 { color: #1B4F72; display: flex; align-items: center; gap: 8px; margin: 0; }
    .full-width { width: 100%; }
    .link { color: #1B4F72; text-decoration: none; font-weight: 500; }
    .link:hover { text-decoration: underline; }
    .empty-state { text-align: center; padding: 60px 20px; }
    .empty-icon { font-size: 64px; width: 64px; height: 64px; color: #BDC3C7; }
    .empty-state h3 { color: #2C3E50; }
  `]
})
export class EquipmentByStatusComponent implements OnInit {
  equipments = signal<Equipment[]>([]);
  loading = signal(true);
  statusLabel = signal('');
  icon = signal('inventory_2');
  columns = ['name', 'category', 'serialNumber', 'assignedTo', 'location', 'actions'];
  statusFilter = '';

  constructor(
    private equipmentService: EquipmentService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.statusFilter = params.get('status') || '';
      this.setupLabels();
      this.loadEquipments();
    });
  }

  setupLabels() {
    switch (this.statusFilter) {
      case 'active':
        this.statusLabel.set('Activos');
        this.icon.set('check_circle');
        break;
      case 'inactive':
        this.statusLabel.set('Inactivos');
        this.icon.set('block');
        break;
      case 'retired':
        this.statusLabel.set('Retirados');
        this.icon.set('archive');
        break;
    }
  }

  loadEquipments() {
    this.loading.set(true);
    this.equipmentService.list(0, 100).subscribe({
      next: (res) => {
        const filtered = (res.content || []).filter(e => {
          const s = e.status?.toLowerCase() || '';
          if (this.statusFilter === 'active') return s === 'active' || s === 'activo';
          if (this.statusFilter === 'inactive') return s === 'inactive' || s === 'inactivo';
          if (this.statusFilter === 'retired') return s === 'retired' || s === 'retirado';
          return false;
        });
        this.equipments.set(filtered);
        this.loading.set(false);
        this.cdr.detectChanges();
      }
    });
  }
}