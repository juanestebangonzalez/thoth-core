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
import { SedeService, Sede } from '../../core/services/sede.service';

@Component({
  selector: 'app-sedes',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatDividerModule, MatTooltipModule],
  template: `
    <div class="sedes-page">
      <div class="header">
        <h1><mat-icon>business</mat-icon> Gestion de Sedes</h1>
      </div>

      <!-- Formulario agregar/editar -->
      <mat-card class="form-card">
        <h3>{{ editingId ? 'Editar Sede' : 'Nueva Sede' }}</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" floatLabel="always" class="field-name">
            <mat-label>Nombre de la Sede *</mat-label>
            <input matInput [(ngModel)]="sedeName" placeholder="Ej: Monterrey" (keyup.enter)="save()">
          </mat-form-field>
          <mat-form-field appearance="outline" floatLabel="always" class="field-address">
            <mat-label>Direccion (opcional)</mat-label>
            <input matInput [(ngModel)]="sedeAddress" placeholder="Calle/Carrera...">
          </mat-form-field>
          <mat-form-field appearance="outline" floatLabel="always" class="field-phone">
            <mat-label>Telefono (opcional)</mat-label>
            <input matInput [(ngModel)]="sedePhone" placeholder="604-XXX">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="save()" [disabled]="!sedeName.trim()">
            <mat-icon>{{ editingId ? 'save' : 'add' }}</mat-icon>
            {{ editingId ? 'Actualizar' : 'Agregar' }}
          </button>
          @if (editingId) {
            <button mat-stroked-button (click)="cancelEdit()">Cancelar</button>
          }
        </div>
      </mat-card>

      <!-- Lista de sedes -->
      <mat-card class="list-card">
        <h3>Sedes Registradas ({{ sedes().length }})</h3>
        @if (sedes().length === 0) {
          <p class="empty">No hay sedes registradas</p>
        } @else {
          @for (sede of sedes(); track sede.id) {
            <div class="sede-row" [class.inactive]="!sede.active">
              <div class="sede-icon" [class.active]="sede.active">
                <mat-icon>{{ sede.active ? 'business' : 'block' }}</mat-icon>
              </div>
              <div class="sede-info">
                <span class="sede-name">{{ sede.name }}</span>
                @if (sede.address) {
                  <span class="sede-address">{{ sede.address }}</span>
                }
                @if (sede.phone) {
                  <span class="sede-phone">{{ sede.phone }}</span>
                }
              </div>
              <span class="sede-status" [class.active]="sede.active">
                {{ sede.active ? 'Activa' : 'Inactiva' }}
              </span>
              <div class="sede-actions">
                <button mat-icon-button (click)="edit(sede)" title="Editar">
                  <mat-icon style="color:#60A5FA;">edit</mat-icon>
                </button>
                @if (sede.active) {
                  <button mat-icon-button (click)="deactivate(sede)" title="Desactivar">
                    <mat-icon style="color:#EF4444;">block</mat-icon>
                  </button>
                } @else {
                  <button mat-icon-button (click)="activate(sede)" title="Activar">
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
    .sedes-page { padding: 32px; max-width: 900px; margin: 0 auto; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0 0 24px; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }
    .form-card { padding: 24px; margin-bottom: 16px; }
    .form-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .form-row { display: flex; gap: 12px; align-items: flex-start; flex-wrap: wrap; }
    .field-name { flex: 1; min-width: 200px; }
    .field-address { flex: 1.5; min-width: 200px; }
    .field-phone { flex: 0.8; min-width: 150px; }
    .list-card { padding: 24px; }
    .list-card h3 { color: #E2E8F0; margin: 0 0 16px; }
    .empty { text-align: center; color: #64748B; padding: 40px; }
    .sede-row { display: flex; align-items: center; gap: 14px; padding: 14px 16px; border-radius: 10px; background: rgba(30,41,59,0.5); margin-bottom: 8px; transition: all 0.2s; }
    .sede-row:hover { background: rgba(59,130,246,0.08); }
    .sede-row.inactive { opacity: 0.5; }
    .sede-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; background: rgba(100,116,139,0.2); }
    .sede-icon.active { background: rgba(59,130,246,0.2); }
    .sede-icon mat-icon { color: #60A5FA; }
    .sede-info { flex: 1; min-width: 0; }
    .sede-name { display: block; color: #F1F5F9; font-weight: 600; font-size: 15px; }
    .sede-address { display: block; color: #94A3B8; font-size: 12px; margin-top: 2px; }
    .sede-phone { display: block; color: #64748B; font-size: 12px; }
    .sede-status { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; background: rgba(100,116,139,0.2); color: #94A3B8; flex-shrink: 0; }
    .sede-status.active { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .sede-actions { display: flex; gap: 4px; flex-shrink: 0; }
    @media (max-width: 768px) {
      .sedes-page { padding: 16px; }
      .form-row { flex-direction: column; }
      .field-name, .field-address, .field-phone { min-width: 100%; }
    }
  `]
})
export class SedesComponent implements OnInit {
  sedes = signal<Sede[]>([]);
  sedeName = '';
  sedeAddress = '';
  sedePhone = '';
  editingId = '';

  constructor(private sedeService: SedeService, private snackBar: MatSnackBar, private cdr: ChangeDetectorRef, private zone: NgZone, private appRef: ApplicationRef) {}

  ngOnInit() { this.loadSedes(); }

  loadSedes() {
    this.sedeService.listAll().subscribe({
      next: (s) => { this.sedes.set(s); this.cdr.detectChanges(); }
    });
  }

  save() {
    if (!this.sedeName.trim()) return;
    const data = { name: this.sedeName.trim(), address: this.sedeAddress, phone: this.sedePhone };

    if (this.editingId) {
      this.sedeService.update(this.editingId, data).subscribe({
        next: () => { this.snackBar.open('Sede actualizada', 'OK', { duration: 3000 }); this.cancelEdit(); this.loadSedes(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
      });
    } else {
      this.sedeService.create(data).subscribe({
        next: () => { this.snackBar.open('Sede creada', 'OK', { duration: 3000 }); this.sedeName = ''; this.sedeAddress = ''; this.sedePhone = ''; this.loadSedes(); },
        error: (e) => this.snackBar.open(e.error?.message || 'Error', 'OK', { duration: 3000 })
      });
    }
  }

  edit(sede: Sede) {
    // Primero limpiar para forzar re-render
    this.editingId = '';
    this.sedeName = '';
    this.sedeAddress = '';
    this.sedePhone = '';
    this.cdr.detectChanges();

    // Luego asignar en el siguiente tick
    setTimeout(() => {
      this.editingId = sede.id;
      this.sedeName = sede.name;
      this.sedeAddress = sede.address || '';
      this.sedePhone = sede.phone || '';
      this.cdr.detectChanges();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  cancelEdit() { this.editingId = ''; this.sedeName = ''; this.sedeAddress = ''; this.sedePhone = ''; }

  deactivate(sede: Sede) {
    if (!confirm('Desactivar sede ' + sede.name + '?')) return;
    this.sedeService.delete(sede.id).subscribe({
      next: () => { this.snackBar.open('Sede desactivada', 'OK', { duration: 3000 }); this.loadSedes(); }
    });
  }

  activate(sede: Sede) {
    this.sedeService.update(sede.id, { active: true } as any).subscribe({
      next: () => { this.snackBar.open('Sede activada', 'OK', { duration: 3000 }); this.loadSedes(); }
    });
  }
}