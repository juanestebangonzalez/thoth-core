import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { SedeService, Sede } from '../../core/services/sede.service';
import { DeviceTypeService, DeviceType } from '../../core/services/device-type.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { CreateEquipmentRequest } from '../../core/models/equipment.model';

@Component({
  selector: 'app-equipment-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatRadioModule, MatExpansionModule, MatDividerModule],
  template: `
    <div class="form-page">
      <mat-card class="form-card">
        <h2>
          <mat-icon>{{ isEditMode() ? 'edit' : 'add_circle' }}</mat-icon>
          {{ isEditMode() ? 'Editar Equipo' : 'Registrar Nuevo Equipo' }}
        </h2>

        <h3 class="section-title"><mat-icon>info</mat-icon> Informacion Basica</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput [(ngModel)]="equipment.name" required>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Categoria</mat-label>
            <mat-select [(ngModel)]="equipment.category" required [disabled]="isEditMode()">
              @for (dt of deviceTypes(); track dt.id) {
                <mat-option [value]="dt.name">{{ dt.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Numero de Serie</mat-label>
            <input matInput [(ngModel)]="equipment.serialNumber" [disabled]="isEditMode()">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>N Inventario Interno</mat-label>
            <input matInput [(ngModel)]="equipment.inventoryNumber" placeholder="Ej: INV-2026-001">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Marca</mat-label>
            <input matInput [(ngModel)]="equipment.brand">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Modelo</mat-label>
            <input matInput [(ngModel)]="equipment.model">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>MAC Address</mat-label>
            <input matInput [(ngModel)]="equipment.macAddress" (input)="formatMac($event)" placeholder="AABBCCDDEEFF o AA:BB:CC:DD:EE:FF" maxlength="17">
            <mat-hint>Se formatea automaticamente</mat-hint>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha de Compra</mat-label>
            <input matInput [(ngModel)]="equipment.purchaseDate" type="date" [disabled]="isEditMode()">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Valor de Compra</mat-label>
            <input matInput [(ngModel)]="equipment.purchaseValue" type="number" [disabled]="isEditMode()">
            <span matPrefix>$&nbsp;</span>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Asignado a</mat-label>
            <input matInput [(ngModel)]="equipment.assignedTo">
          </mat-form-field>
        </div>

        <mat-divider></mat-divider>

        <h3 class="section-title"><mat-icon>location_on</mat-icon> Ubicacion</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Sede</mat-label>
            <mat-select [(ngModel)]="selectedSede" (ngModelChange)="equipment.location.building = $event" required name="sede">
              @for (sede of sedes(); track sede.id) {
                <mat-option [value]="sede.name">{{ sede.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Piso</mat-label>
            <input matInput [(ngModel)]="equipment.location.floor" required>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Oficina</mat-label>
            <input matInput [(ngModel)]="equipment.location.office" required>
          </mat-form-field>
        </div>

        <mat-divider></mat-divider>

        <h3 class="section-title"><mat-icon>business_center</mat-icon> Propiedad</h3>
        <mat-radio-group [(ngModel)]="equipment.ownershipType" class="radio-group">
          <mat-radio-button value="OWNED">
            <mat-icon>verified</mat-icon> Propio
          </mat-radio-button>
          <mat-radio-button value="RENTED">
            <mat-icon>schedule</mat-icon> Alquilado
          </mat-radio-button>
        </mat-radio-group>

        @if (equipment.ownershipType === 'RENTED') {
          <mat-expansion-panel expanded class="section-panel">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-icon>description</mat-icon>
                <span>Datos del Alquiler</span>
              </mat-panel-title>
              <mat-panel-description>Opcional - Rellena solo lo que necesites</mat-panel-description>
            </mat-expansion-panel-header>
            <div class="form-grid">
              <mat-form-field appearance="outline">
                <mat-label>Empresa Arrendadora</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.rentalCompany">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Numero de Contrato</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.contractNumber">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Contacto (Nombre)</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.contactName">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Telefono</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.contactPhone">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Email</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.contactEmail" type="email">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Fecha Inicio Contrato</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.startDate" type="date">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Fecha Fin Contrato</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.endDate" type="date">
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>URL del Contrato (PDF)</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.contractFileUrl" placeholder="https://...">
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-column">
                <mat-label>Notas del Contrato</mat-label>
                <textarea matInput [(ngModel)]="equipment.rentalInfo!.notes" rows="2"></textarea>
              </mat-form-field>
            </div>
          </mat-expansion-panel>
        }

        <mat-divider></mat-divider>

        <mat-expansion-panel class="section-panel">
          <mat-expansion-panel-header>
            <mat-panel-title>
              <mat-icon>memory</mat-icon>
              <span>Especificaciones de Hardware</span>
            </mat-panel-title>
            <mat-panel-description>Opcional - Procesador, RAM, disco</mat-panel-description>
          </mat-expansion-panel-header>
          <div class="form-grid">
            <mat-form-field appearance="outline" class="full-column">
              <mat-label>Procesador</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.processor" placeholder="Ej: Intel Core i7-12700H">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>RAM (GB)</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.ramSizeGb" type="number" min="1">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Tipo de RAM</mat-label>
              <mat-select [(ngModel)]="equipment.hardware!.ramType">
                <mat-option value="">-- Sin especificar --</mat-option>
                <mat-option value="DDR3">DDR3</mat-option>
                <mat-option value="DDR4">DDR4</mat-option>
                <mat-option value="DDR5">DDR5</mat-option>
                <mat-option value="LPDDR4">LPDDR4</mat-option>
                <mat-option value="LPDDR5">LPDDR5</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Tipo de Disco</mat-label>
              <mat-select [(ngModel)]="equipment.hardware!.diskType">
                <mat-option value="">-- Sin especificar --</mat-option>
                <mat-option value="HDD">HDD</mat-option>
                <mat-option value="SSD">SSD</mat-option>
                <mat-option value="NVME">NVME</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Tamano del Disco (GB)</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.diskSizeGb" type="number" min="1">
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Salud del Disco (%)</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.diskHealthPercent" type="number" min="0" max="100">
              <mat-hint>0-100. Menor a 30% = critico</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Temperatura del Disco (C)</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.diskTemperatureCelsius" type="number" min="0" max="120">
              <mat-hint>Mayor o igual a 70C = critico</mat-hint>
            </mat-form-field>
          </div>
        </mat-expansion-panel>

        @if (isEditMode()) {
          <p class="hint">
            <mat-icon>info</mat-icon>
            Los campos deshabilitados (serial, categoria, fecha, valor) no pueden modificarse
          </p>
        }

        <div class="actions">
          <button mat-button (click)="cancel()">Cancelar</button>
          <button mat-raised-button color="primary" (click)="save()" [disabled]="loading()">
            @if (loading()) {
              {{ isEditMode() ? 'Actualizando...' : 'Guardando...' }}
            } @else {
              {{ isEditMode() ? 'Actualizar Equipo' : 'Guardar Equipo' }}
            }
          </button>
        </div>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-page { padding: 24px; max-width: 900px; margin: 0 auto; }
    .form-card { padding: 32px; }
    h2 {
      display: flex; align-items: center; gap: 8px; margin-top: 0;
      background: linear-gradient(135deg, #60A5FA, #A78BFA);
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .section-title {
      display: flex; align-items: center; gap: 8px;
      color: #94A3B8; font-size: 14px; font-weight: 600;
      text-transform: uppercase; letter-spacing: 1px;
      margin: 24px 0 16px;
    }
    .section-title mat-icon { color: #60A5FA; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
    .full-column { grid-column: 1 / -1; }
    .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
    .hint {
      display: flex; align-items: center; gap: 8px;
      color: #94A3B8; font-size: 13px;
      background: rgba(59, 130, 246, 0.1);
      padding: 12px 16px; border-radius: 8px;
      border-left: 4px solid #60A5FA; margin-top: 16px;
    }
    .hint mat-icon { color: #60A5FA; font-size: 20px; width: 20px; height: 20px; }
    .radio-group { display: flex; gap: 24px; margin: 8px 0 24px; }
    .radio-group mat-radio-button { display: flex; align-items: center; }
    .section-panel {
      background: rgba(15, 23, 42, 0.4) !important;
      margin: 16px 0 !important;
      border: 1px solid rgba(148, 163, 184, 0.15) !important;
    }
    .section-panel mat-panel-title {
      display: flex; align-items: center; gap: 8px;
      color: #E2E8F0;
    }
    .section-panel mat-panel-title mat-icon { color: #60A5FA; }
    .section-panel mat-panel-description { color: #94A3B8; }
    mat-divider { margin: 20px 0 !important; border-top-color: rgba(148, 163, 184, 0.15) !important; }
  `]
})
export class EquipmentFormComponent implements OnInit {
  equipment: CreateEquipmentRequest = {
    name: '', category: '', serialNumber: '', inventoryNumber: '', brand: '', model: '', macAddress: '',
    location: { building: '', floor: '', office: '' },
    purchaseDate: '', purchaseValue: 0, assignedTo: '',
    ownershipType: 'OWNED',
    rentalInfo: { rentalCompany: '', contactName: '', contactPhone: '', contactEmail: '', startDate: '', endDate: '', contractNumber: '', contractFileUrl: '', notes: '' },
    hardware: { processor: '', ramSizeGb: undefined, ramType: '', diskType: '', diskSizeGb: undefined, diskHealthPercent: undefined, diskTemperatureCelsius: undefined }
  };
  isEditMode = signal(false);
  sedes = signal<Sede[]>([]);
  deviceTypes = signal<DeviceType[]>([]);
  selectedSede = '';
  loading = signal(false);
  equipmentId = '';

  constructor(
    private equipmentService: EquipmentService,
    private sedeService: SedeService,
    private deviceTypeService: DeviceTypeService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.equipmentId = this.route.snapshot.paramMap.get('id') || '';
    this.loadSedes();
    this.loadDeviceTypes();
    if (this.equipmentId) {
      this.isEditMode.set(true);
      this.loadEquipment();
    }
  }

  loadSedes() {
    this.sedeService.listActive().subscribe({
      next: (s) => { this.sedes.set(s); this.cdr.detectChanges(); }
    });
  }

  loadDeviceTypes() {
    this.deviceTypeService.listActive().subscribe({
      next: (types) => { this.deviceTypes.set(types); this.cdr.detectChanges(); }
    });
  }

  loadEquipment() {
    this.equipmentService.getById(this.equipmentId).subscribe({
      next: (e: any) => {
        this.equipment = {
          name: e.name,
          category: e.category,
          serialNumber: e.serialNumber,
          inventoryNumber: e.inventoryNumber || '',
          brand: e.brand,
          model: e.model || '',
          macAddress: e.macAddress || '',
          location: {
            building: e.location?.building || '',
            floor: e.location?.floor || '',
            office: e.location?.office || ''
          },
          purchaseDate: e.purchaseDate,
          purchaseValue: e.purchaseValue,
          assignedTo: e.assignedTo || '',
          ownershipType: e.ownershipType || 'OWNED',
          rentalInfo: {
            rentalCompany: e.rentalInfo?.rentalCompany || '',
            contactName: e.rentalInfo?.contactName || '',
            contactPhone: e.rentalInfo?.contactPhone || '',
            contactEmail: e.rentalInfo?.contactEmail || '',
            startDate: e.rentalInfo?.startDate || '',
            endDate: e.rentalInfo?.endDate || '',
            contractNumber: e.rentalInfo?.contractNumber || '',
            contractFileUrl: e.rentalInfo?.contractFileUrl || '',
            notes: e.rentalInfo?.notes || ''
          },
          hardware: {
            processor: e.hardware?.processor || '',
            ramSizeGb: e.hardware?.ramSizeGb,
            ramType: e.hardware?.ramType || '',
            diskType: e.hardware?.diskType || '',
            diskSizeGb: e.hardware?.diskSizeGb,
            diskHealthPercent: e.hardware?.diskHealthPercent,
            diskTemperatureCelsius: e.hardware?.diskTemperatureCelsius
          }
        };
        this.cdr.detectChanges();
      },
      error: () => {
        this.snackBar.open('Error al cargar el equipo', 'OK', { duration: 3000 });
        this.router.navigate(['/equipment']);
      }
    });
  }

  save() {
    this.loading.set(true);
    const cleanRental = this.hasRentalData() ? this.equipment.rentalInfo : undefined;
    const cleanHardware = this.hasHardwareData() ? this.equipment.hardware : undefined;

    if (this.isEditMode()) {
      const updateData: any = {
        name: this.equipment.name,
        inventoryNumber: this.equipment.inventoryNumber || undefined,
        brand: this.equipment.brand || undefined,
        model: this.equipment.model || undefined,
        macAddress: this.equipment.macAddress || undefined,
        assignedTo: this.equipment.assignedTo || undefined,
        location: this.equipment.location,
        ownershipType: this.equipment.ownershipType,
        rentalInfo: cleanRental,
        hardware: cleanHardware
      };
      this.equipmentService.update(this.equipmentId, updateData).subscribe({
        next: () => {
          this.snackBar.open('Equipo actualizado exitosamente', 'OK', { duration: 3000 });
          this.router.navigate(['/equipment', this.equipmentId]);
        },
        error: (err) => {
          this.loading.set(false);
          this.snackBar.open('Error: ' + (err.error?.message || 'Error al actualizar'), 'OK', { duration: 5000 });
          this.selectedSede = this.equipment.location?.building || '';
          this.cdr.detectChanges();
        }
      });
    } else {
      const createData: any = {
        ...this.equipment,
        serialNumber: this.equipment.serialNumber || undefined,
        brand: this.equipment.brand || undefined,
        model: this.equipment.model || undefined,
        macAddress: this.equipment.macAddress || undefined,
        purchaseDate: this.equipment.purchaseDate || undefined,
        purchaseValue: this.equipment.purchaseValue || undefined,
        assignedTo: this.equipment.assignedTo || undefined,
        inventoryNumber: this.equipment.inventoryNumber || undefined,
        rentalInfo: cleanRental,
        hardware: cleanHardware
      };
      this.equipmentService.create(createData).subscribe({
        next: () => {
          this.snackBar.open('Equipo registrado exitosamente', 'OK', { duration: 3000 });
          this.router.navigate(['/equipment']);
        },
        error: (err) => {
          this.loading.set(false);
          this.snackBar.open('Error: ' + (err.error?.message || 'Error al guardar'), 'OK', { duration: 5000 });
          this.selectedSede = this.equipment.location?.building || '';
          this.cdr.detectChanges();
        }
      });
    }
  }

  hasRentalData(): boolean {
    if (this.equipment.ownershipType !== 'RENTED') return false;
    const r = this.equipment.rentalInfo!;
    return !!(r.rentalCompany || r.contactName || r.startDate || r.endDate || r.contractNumber);
  }

  hasHardwareData(): boolean {
    const h = this.equipment.hardware!;
    return !!(h.processor || h.ramSizeGb || h.diskType || h.diskSizeGb || h.diskHealthPercent !== undefined || h.diskTemperatureCelsius !== undefined);
  }

  cancel() {
    if (this.isEditMode()) {
      this.router.navigate(['/equipment', this.equipmentId]);
    } else {
      this.router.navigate(['/equipment']);
    }
  }

  formatMac(event: any) {
    let value = event.target.value.replace(/[:\-\s]/g, '').toUpperCase();
    if (value.length > 12) value = value.substring(0, 12);
    if (!/^[0-9A-F]*$/.test(value)) {
      value = value.replace(/[^0-9A-F]/g, '');
    }
    let formatted = '';
    for (let i = 0; i < value.length; i++) {
      if (i > 0 && i % 2 === 0) formatted += ':';
      formatted += value[i];
    }
    this.equipment.macAddress = formatted;
    event.target.value = formatted;
  }
}