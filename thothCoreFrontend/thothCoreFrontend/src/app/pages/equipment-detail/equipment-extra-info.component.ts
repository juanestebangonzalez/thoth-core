import { Component, ChangeDetectorRef, computed, effect, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { EquipmentService } from '../../core/services/equipment.service';
import { Equipment, MonitorSummary, esMonitor, textoVidaUtil } from '../../core/models/equipment.model';
import { EtiquetaPipe } from '../../core/pipes/etiqueta.pipe';

/** Secciones de responsable, red, vida util/criticidad y monitores del detalle del equipo. */
@Component({
  selector: 'app-equipment-extra-info',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule, MatDividerModule, EtiquetaPipe],
  template: `
    @let e = equipment();
    <mat-divider></mat-divider>
    <p class="section-heading"><mat-icon>person</mat-icon> RESPONSABLE</p>
    <div class="info-grid">
      <div class="info-item"><mat-icon>person</mat-icon><div><span class="label">Asignado a</span><span class="value">{{ e.assignedTo || 'Sin asignar' }}</span></div></div>
      <div class="info-item"><mat-icon>badge</mat-icon><div><span class="label">Cargo</span><span class="value">{{ e.responsiblePosition || '-' }}</span></div></div>
      <div class="info-item"><mat-icon>fingerprint</mat-icon><div><span class="label">Documento de identidad</span><span class="value">{{ e.responsibleDocument || '-' }}</span></div></div>
      <div class="info-item"><mat-icon>smartphone</mat-icon><div><span class="label">Celular</span><span class="value">{{ e.responsiblePhone || '-' }}</span></div></div>
      <div class="info-item"><mat-icon>email</mat-icon><div><span class="label">Correo</span><span class="value">{{ e.responsibleEmail || '-' }}</span></div></div>
    </div>

    <mat-divider></mat-divider>
    <p class="section-heading"><mat-icon>lan</mat-icon> RED</p>
    <div class="info-grid">
      <div class="info-item"><mat-icon>dns</mat-icon><div><span class="label">Hostname</span><span class="value">{{ e.name || '-' }}</span></div></div>
      <div class="info-item"><mat-icon>public</mat-icon><div><span class="label">Direccion IP</span><span class="value">{{ e.ipAddress || '-' }}</span></div></div>
      <div class="info-item"><mat-icon>settings_ethernet</mat-icon><div><span class="label">Asignacion IP</span><span class="value">{{ e.ipAssignment === 'FIJA' ? 'Fija' : (e.ipAssignment || '-') }}</span></div></div>
      <div class="info-item"><mat-icon>router</mat-icon><div><span class="label">{{ e.macAddress2 ? 'MAC Ethernet' : 'MAC Address' }}</span><span class="value">{{ e.macAddress || '-' }}</span></div></div>
      @if (e.macAddress2) {
        <div class="info-item"><mat-icon>wifi</mat-icon><div><span class="label">MAC WiFi</span><span class="value">{{ e.macAddress2 }}</span></div></div>
      }
    </div>

    <mat-divider></mat-divider>
    <p class="section-heading"><mat-icon>hourglass_bottom</mat-icon> VIDA UTIL Y CRITICIDAD</p>
    <div class="info-grid">
      <div class="info-item">
        <mat-icon>hourglass_bottom</mat-icon>
        <div class="grow">
          <span class="label">Vida util</span>
          <span class="value">{{ vidaUtil() }}</span>
          @if (e.usefulLife?.consumedPercent != null) {
            <div class="bar"><div class="bar-fill" [class]="'bar-fill ' + colorVida()" [style.width.%]="anchoVida()"></div></div>
          }
        </div>
      </div>
      <div class="info-item">
        <mat-icon>priority_high</mat-icon>
        <div><span class="label">Criticidad</span>
          @if (e.criticality) {
            <span class="chip" [class]="'chip crit-' + (e.criticality || '').toLowerCase()">{{ e.criticality }}</span>
          } @else { <span class="value">-</span> }
        </div>
      </div>
    </div>

    @if (monitor()) {
      <mat-divider></mat-divider>
      <p class="section-heading"><mat-icon>desktop_windows</mat-icon> PC ASOCIADO</p>
      <p class="assoc">
        Asociado a:
        @if (e.associatedEquipmentId) {
          <a class="link" [routerLink]="['/equipment', e.associatedEquipmentId]">{{ e.associatedEquipmentName || 'Ver equipo' }}{{ e.associatedEquipmentInventory ? ' (' + e.associatedEquipmentInventory + ')' : '' }}</a>
        } @else { <span class="muted">Sin asociar</span> }
      </p>
    } @else {
      <mat-divider></mat-divider>
      <p class="section-heading"><mat-icon>monitor</mat-icon> MONITORES ASOCIADOS</p>
      @if (monitors().length) {
        <div class="table-scroll">
          <table>
            <thead><tr><th>Nombre</th><th>Inventario</th><th>Marca / Modelo</th><th>Propiedad</th><th>Estado</th></tr></thead>
            <tbody>
              @for (m of monitors(); track m.equipmentId) {
                <tr>
                  <td><a class="link" [routerLink]="['/equipment', m.equipmentId]">{{ m.name }}</a></td>
                  <td>{{ m.inventoryNumber || '-' }}</td>
                  <td>{{ m.brand || '-' }} {{ m.model || '' }}</td>
                  <td>
                    @if (m.ownershipType === 'RENTED') {
                      Alquilado{{ m.rentalCompany ? ' - ' + m.rentalCompany : '' }}{{ m.monthlyValue != null ? ' - ' + cop(m.monthlyValue) + '/mes' : '' }}
                    } @else { Propio }
                  </td>
                  <td>{{ m.status | etiqueta:'estado' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      } @else {
        <p class="muted">No hay monitores asociados a este equipo.</p>
      }
    }
  `,
  styles: [`
    .section-heading { display: flex; align-items: center; gap: 8px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; margin: 20px 0 12px; }
    .section-heading mat-icon, .info-item mat-icon { color: #60A5FA; }
    .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; padding: 0 0 20px; }
    .info-item { display: flex; align-items: flex-start; gap: 12px; }
    .info-item mat-icon { margin-top: 4px; }
    .grow { flex: 1; }
    .label { display: block; font-size: 11px; color: #94A3B8; text-transform: uppercase; margin-bottom: 4px; letter-spacing: 1px; }
    .value { display: block; color: #E2E8F0; font-weight: 500; word-break: break-word; }
    .bar { height: 8px; border-radius: 4px; background: rgba(148,163,184,0.2); margin-top: 6px; overflow: hidden; max-width: 260px; }
    .bar-fill { height: 100%; border-radius: 4px; }
    .bar-fill.ok { background: #10B981; } .bar-fill.warn { background: #F59E0B; } .bar-fill.crit { background: #EF4444; }
    .chip { display: inline-block; padding: 3px 12px; border-radius: 10px; font-size: 11px; font-weight: 700; }
    .crit-alta { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .crit-media { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .crit-baja { background: rgba(16,185,129,0.2); color: #6EE7B7; }
    .assoc { color: #E2E8F0; margin: 0 0 20px; }
    .link { color: #60A5FA; font-weight: 600; text-decoration: none; }
    .link:hover { text-decoration: underline; }
    .muted { color: #64748B; font-style: italic; }
    .table-scroll { overflow-x: auto; margin-bottom: 20px; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 8px 10px; color: #94A3B8; font-size: 11px; text-transform: uppercase; border-bottom: 1px solid rgba(148,163,184,0.15); }
    td { padding: 8px 10px; color: #CBD5E1; font-size: 13px; border-bottom: 1px solid rgba(148,163,184,0.08); }
  `]
})
export class EquipmentExtraInfoComponent {
  equipment = input.required<Equipment>();
  monitors = signal<MonitorSummary[]>([]);
  monitor = computed(() => esMonitor(this.equipment().category));
  vidaUtil = computed(() => textoVidaUtil(this.equipment().usefulLife));
  anchoVida = computed(() => Math.max(0, Math.min(100, Number(this.equipment().usefulLife?.consumedPercent) || 0)));
  colorVida = computed(() => {
    const p = Number(this.equipment().usefulLife?.consumedPercent) || 0;
    return p > 100 ? 'crit' : p >= 70 ? 'warn' : 'ok';
  });
  private cop$ = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

  constructor(private equipmentService: EquipmentService, private cdr: ChangeDetectorRef) {
    effect(() => {
      const e = this.equipment();
      if (!e?.equipmentId || esMonitor(e.category)) { this.monitors.set([]); return; }
      this.equipmentService.getMonitors(e.equipmentId).subscribe({
        next: (m) => { this.monitors.set(m || []); this.cdr.markForCheck(); },
        error: () => this.monitors.set([])
      });
    });
  }

  cop(v: number | null | undefined): string {
    return v == null || isNaN(Number(v)) ? '-' : this.cop$.format(Number(v));
  }
}
