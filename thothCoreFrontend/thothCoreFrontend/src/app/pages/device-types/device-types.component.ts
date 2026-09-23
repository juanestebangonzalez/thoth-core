import { Component, OnInit, signal, ChangeDetectorRef, NgZone, ApplicationRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DeviceTypeService, DeviceType } from '../../core/services/device-type.service';

@Component({
  selector: 'app-device-types',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatDividerModule, MatTooltipModule],
  template: `
    <div class="page-container">
      <div class="header">
        <h1><mat-icon>devices</mat-icon> Tipos de Dispositivo</h1>
      </div>

      <!-- Formulario agregar/editar -->
      <mat-card class="form-card">
        <h3>{{ editingId ? 'Editar Tipo' : 'Nuevo Tipo de Dispositivo' }}</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" floatLabel="always" class="field-name">
            <mat-label>Nombre *</mat-label>
            <input matInput [(ngModel)]="itemName" placeholder="Ej: Laptop" (keyup.enter)="save()">
          </mat-form-field>
          <mat-form-field appearance="outline" floatLabel="always" class="field-desc">
            <mat-label>Descripcion (opcional)</mat-label>
            <input matInput [(ngModel)]="itemDescription" placeholder="Descripcion del tipo">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="save()" [disabled]="!itemName.trim()">
            <mat-icon>{{ editingId ? 'save' : 'add' }}</mat-icon>
            {{ editingId ? 'Actualizar' : 'Agregar' }}
          </button>
          @if (editingId) {
            <button mat-stroked-button (click)="cancelEdit()">Cancelar</button>
          }
        </div>
      </mat-card>

      <!-- Lista -->
      <mat-card class="list-card">
        <h3>Tipos Registrados ({{ items().length }})</h3>
        @if (items().length === 0) {
          <p class="empty">No hay tipos de dispositivo registrados</p>
        } @else {
          @for (item of items(); track item.id) {
            <div class="item-row" [class.inactive]="!item.active">
              <div class="item-icon" [class.active]="item.active">
                <mat-icon>{{ item.active ? 'devices' : 'block' }}</mat-icon>
              </div>
              <div class="item-info">
                <span class="item-name">{{ item.name }}</span>
                @if (item.description) {
                  <span class="item-desc">{{ item.description }}</span>
                }
              </div>
              <span class="item-status" [class.active]="item.active">
                {{ item.active ? 'Activo' : 'Inactivo' }}
              </span>
              <div class="item-actions">
                <button mat-icon-button (click)="edit(item)" title="Editar">
                  <mat-icon style="color:#60A5FA;">edit</mat-icon>
                </button>
                @if (item.active) {
                  <button mat-icon-button (click)="deactivate(item)" title="Desactivar">
                    <mat-icon style="color:#EF4444;">block</mat-icon>
                  </button>
                } @else {
                  <button mat-icon-button (click)="activate(item)" title="Activar">
                    <mat-icon style="color:#10B981;">check_circle</mat-icon>
                  </button>
                }
              </div>
            </div>
          }
        }
      </mat-card>
    </div>
  `,
  styles: [`
    .page-container { padding: 32px; max-width: 900px; margin: 0 auto; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0 0 24px; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .form-card { padding: 24px; margin-bottom: 16px; }
    .form-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .form-row { display: flex; gap: 12px; align-items: flex-start; flex-wrap: wrap; }
    .field-name { flex: 1; min-width: 200px; }
    .field-desc { flex: 1.5; min-width: 200px; }
    .list-card { padding: 24px; }
    .list-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .empty { text-align: center; color: #64748B; padding: 40px; }
    .item-row { display: flex; align-items: center; gap: 14px; padding: 14px 16px; border-radius: 10px; background: rgba(30,41,59,0.5); margin-bottom: 8px; transition: all 0.2s; }
    .item-row:hover { background: rgba(59,130,246,0.08); }
    .item-row.inactive { opacity: 0.5; }
    .item-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: rgba(100,116,139,0.2); }
    .item-icon.active { background: rgba(59,130,246,0.2); }
    .item-icon mat-icon { color: #60A5FA; }
    .item-info { flex: 1; min-width: 0; }
    .item-name { display: block; color: #F1F5F9; font-weight: 600; font-size: 15px; }
    .item-desc { display: block; color: #94A3B8; font-size: 12px; margin-top: 2px; }
    .item-status { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; background: rgba(100,116,139,0.2); color: #94A3B8; flex-shrink: 0; }
    .item-status.active { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .item-actions { display: flex; gap: 4px; flex-shrink: 0; }
    @media (max-width: 768px) {
      .page-container { padding: 16px; }
      .form-row { flex-direction: column; }
      .field-name, .field-desc { min-width: 100%; }
    }
  `]
})
export class DeviceTypesComponent implements OnInit {
  items = signal<DeviceType[]>([]);
  itemName = '';
  itemDescription = '';
  editingId = '';

  constructor(private service: DeviceTypeService, private snackBar: MatSnackBar, private cdr: ChangeDetectorRef, private zone: NgZone, private appRef: ApplicationRef) {}

  ngOnInit() { this.loadItems(); }

  loadItems() {
    this.service.listAll().subscribe({
      next: (data) => { this.items.set(data); this.cdr.detectChanges(); }
    });
  }

  save() {
    if (!this.itemName.trim()) return;
    const data = { name: this.itemName.trim(), description: this.itemDescription };

    if (this.editingId) {
      this.service.update(this.editingId, data).subscribe({
        next: () => { this.snackBar.open('Tipo actualizado', 'OK', { duration: 3000 }); this.cancelEdit(); this.loadItems(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
      });
    } else {
      this.service.create(data).subscribe({
        next: () => { this.snackBar.open('Tipo creado', 'OK', { duration: 3000 }); this.itemName = ''; this.itemDescription = ''; this.loadItems(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
      });
    }
  }

  edit(item: DeviceType) {
    this.editingId = '';
    this.itemName = '';
    this.itemDescription = '';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.editingId = item.id;
      this.itemName = item.name;
      this.itemDescription = item.description || '';
      this.cdr.detectChanges();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  cancelEdit() { this.editingId = ''; this.itemName = ''; this.itemDescription = ''; }

  deactivate(item: DeviceType) {
    if (!confirm('Desactivar tipo de dispositivo ' + item.name + '?')) return;
    this.service.delete(item.id).subscribe({
      next: () => { this.snackBar.open('Tipo desactivado', 'OK', { duration: 3000 }); this.loadItems(); }
    });
  }

  activate(item: DeviceType) {
    this.service.update(item.id, { active: true } as any).subscribe({
      next: () => { this.snackBar.open('Tipo activado', 'OK', { duration: 3000 }); this.loadItems(); }
    });
  }
}
