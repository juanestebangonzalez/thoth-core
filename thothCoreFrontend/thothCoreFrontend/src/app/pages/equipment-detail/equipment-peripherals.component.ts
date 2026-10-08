import { Component, ChangeDetectorRef, OnInit, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EquipmentService } from '../../core/services/equipment.service';
import { AuthService } from '../../core/services/auth.service';
import { Peripheral } from '../../core/models/equipment.model';
import { PeripheralDialogComponent } from './peripheral-dialog.component';

/** Seccion de perifericos del detalle del equipo. */
@Component({
  selector: 'app-equipment-peripherals',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, MatDividerModule, MatDialogModule, MatSnackBarModule],
  template: `
    <mat-divider></mat-divider>
    <div class="head">
      <p class="section-heading"><mat-icon>keyboard</mat-icon> PERIFERICOS</p>
      @if (auth.hasPermission('EQUIPMENT', 'CREATE')) {
        <button mat-stroked-button class="add-btn" (click)="open()">
          <mat-icon>add</mat-icon> Agregar periferico
        </button>
      }
    </div>
    @if (peripherals().length) {
      <div class="table-scroll">
        <table>
          <thead><tr><th>Tipo</th><th>Marca</th><th class="act"></th></tr></thead>
          <tbody>
            @for (p of peripherals(); track p.id) {
              <tr>
                <td>{{ p.type }}</td>
                <td>{{ p.brand || '-' }}</td>
                <td class="act">
                  @if (auth.hasPermission('EQUIPMENT', 'EDIT')) {
                    <button mat-icon-button matTooltip="Editar" (click)="open(p)"><mat-icon class="edit">edit</mat-icon></button>
                  }
                  @if (auth.hasPermission('EQUIPMENT', 'DELETE')) {
                    <button mat-icon-button matTooltip="Quitar" (click)="remove(p)"><mat-icon class="del">delete</mat-icon></button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    } @else {
      <p class="muted">No hay perifericos registrados.</p>
    }
  `,
  styles: [`
    .head { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
    .section-heading { display: flex; align-items: center; gap: 8px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; margin: 20px 0 12px; }
    .section-heading mat-icon { color: #60A5FA; }
    .add-btn { color: #60A5FA !important; border-color: #60A5FA !important; }
    .muted { color: #64748B; font-style: italic; }
    .table-scroll { overflow-x: auto; margin-bottom: 20px; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 8px 10px; color: #94A3B8; font-size: 11px; text-transform: uppercase; border-bottom: 1px solid rgba(148,163,184,0.15); }
    td { padding: 4px 10px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
    .act { text-align: right; white-space: nowrap; width: 110px; }
    .edit { color: #60A5FA; } .del { color: #EF4444; }
  `]
})
export class EquipmentPeripheralsComponent implements OnInit {
  equipmentId = input.required<string>();
  equipmentName = input<string>('');
  peripherals = signal<Peripheral[]>([]);

  constructor(
    public auth: AuthService,
    private equipmentService: EquipmentService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.equipmentService.getPeripherals(this.equipmentId()).subscribe({
      next: (p) => { this.peripherals.set(p || []); this.cdr.markForCheck(); },
      error: () => this.peripherals.set([])
    });
  }

  open(p?: Peripheral) {
    const ref = this.dialog.open(PeripheralDialogComponent, {
      width: '460px',
      data: { equipmentId: this.equipmentId(), equipmentName: this.equipmentName(), peripheral: p }
    });
    ref.afterClosed().subscribe(ok => {
      if (ok) {
        this.snackBar.open(p ? 'Periferico actualizado' : 'Periferico agregado', 'OK', { duration: 3000 });
        this.load();
      }
    });
  }

  remove(p: Peripheral) {
    if (!confirm('Quitar el periferico ' + p.type + (p.brand ? ' ' + p.brand : '') + ' de este equipo?')) return;
    this.equipmentService.deletePeripheral(this.equipmentId(), p.id).subscribe({
      next: () => { this.snackBar.open('Periferico quitado', 'OK', { duration: 3000 }); this.load(); },
      error: (e) => this.snackBar.open('Error: ' + (e.error?.message || 'No se pudo quitar el periferico'), 'OK', { duration: 5000 })
    });
  }
}
