import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { SedeService, Sede } from '../../core/services/sede.service';
import { AreaService, Area } from '../../core/services/area.service';
import { DeviceTypeService, DeviceType } from '../../core/services/device-type.service';
import { CostCenterService, CostCenter } from '../../core/services/cost-center.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { CreateEquipmentRequest, SISTEMAS_OPERATIVOS, OS_EDITIONS, OS_VERSIONES_WINDOWS, OS_LICENSE_TYPES, Equipment, esMonitor } from '../../core/models/equipment.model';
import { HardwareThresholdsService } from '../../core/services/hardware-thresholds.service';

@Component({
  selector: 'app-equipment-form',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule, MatRadioModule, MatExpansionModule, MatDividerModule, MatAutocompleteModule],
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
            <mat-label>Nombre *</mat-label>
            <input matInput [(ngModel)]="equipment.name" required>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Categoria *</mat-label>
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
            <mat-label>Fecha de Compra</mat-label>
            <input matInput [(ngModel)]="equipment.purchaseDate" type="date" [disabled]="isEditMode()">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Valor de Compra</mat-label>
            <input matInput [(ngModel)]="equipment.purchaseValue" type="number" [disabled]="isEditMode()">
            <span matPrefix>$&nbsp;</span>
          </mat-form-field>
        </div>

        <mat-divider></mat-divider>

        <h3 class="section-title"><mat-icon>person</mat-icon> Responsable</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Asignado a</mat-label>
            <input matInput [(ngModel)]="equipment.assignedTo">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Cargo</mat-label>
            <input matInput [(ngModel)]="equipment.responsiblePosition">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Documento de identidad</mat-label>
            <input matInput [(ngModel)]="equipment.responsibleDocument" maxlength="20">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Celular</mat-label>
            <input matInput [(ngModel)]="equipment.responsiblePhone" (input)="onlyDigits($event)" inputmode="numeric" maxlength="10" placeholder="3001234567">
            @if (phoneError()) {
              <mat-hint class="hint-error">Debe tener 10 digitos (solo numeros)</mat-hint>
            } @else {
              <mat-hint>10 digitos, solo numeros</mat-hint>
            }
          </mat-form-field>
          <mat-form-field appearance="outline" class="full-column">
            <mat-label>Correo electronico</mat-label>
            <input matInput [(ngModel)]="equipment.responsibleEmail" type="email" placeholder="nombre@dominio.com">
            @if (emailError()) {
              <mat-hint class="hint-error">Correo electronico no valido</mat-hint>
            }
          </mat-form-field>
        </div>

        <mat-divider></mat-divider>

        <h3 class="section-title"><mat-icon>lan</mat-icon> Red</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Hostname</mat-label>
            <input matInput [value]="equipment.name || ''" readonly>
            <mat-hint>Igual al nombre del equipo</mat-hint>
          </mat-form-field>
          @if (isLaptop()) {
            <mat-form-field appearance="outline">
              <mat-label>MAC Ethernet</mat-label>
              <input matInput [(ngModel)]="equipment.macAddress" (input)="formatMac($event, 'macAddress')" placeholder="AABBCCDDEEFF o AA:BB:CC:DD:EE:FF" maxlength="17">
              <mat-hint>Se formatea automaticamente</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>MAC WiFi</mat-label>
              <input matInput [(ngModel)]="equipment.macAddress2" (input)="formatMac($event, 'macAddress2')" placeholder="AABBCCDDEEFF o AA:BB:CC:DD:EE:FF" maxlength="17">
              <mat-hint>Se formatea automaticamente</mat-hint>
            </mat-form-field>
          } @else {
            <mat-form-field appearance="outline">
              <mat-label>MAC Address</mat-label>
              <input matInput [(ngModel)]="equipment.macAddress" (input)="formatMac($event, 'macAddress')" placeholder="AABBCCDDEEFF o AA:BB:CC:DD:EE:FF" maxlength="17">
              <mat-hint>Se formatea automaticamente</mat-hint>
            </mat-form-field>
          }
          <mat-form-field appearance="outline">
            <mat-label>Direccion IP</mat-label>
            <input matInput [(ngModel)]="equipment.ipAddress" placeholder="192.168.1.10" maxlength="15">
            @if (ipError()) {
              <mat-hint class="hint-error">Direccion IPv4 no valida</mat-hint>
            }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Asignacion IP</mat-label>
            <mat-select [(ngModel)]="equipment.ipAssignment">
              <mat-option value="">-- Sin especificar --</mat-option>
              <mat-option value="DHCP">DHCP</mat-option>
              <mat-option value="FIJA">Fija</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        @if (isMonitor()) {
          <mat-divider></mat-divider>
          <h3 class="section-title"><mat-icon>desktop_windows</mat-icon> PC asociado</h3>
          <div class="form-grid">
            <mat-form-field appearance="outline" class="full-column">
              <mat-label>PC asociado</mat-label>
              <input matInput [ngModel]="pcText()" (ngModelChange)="onPcInput($event)" [matAutocomplete]="pcAuto" placeholder="Buscar por nombre o N inventario">
              @if (equipment.associatedEquipmentId || pcText()) {
                <button mat-icon-button matSuffix type="button" (click)="clearPc()" title="Quitar asociacion">
                  <mat-icon>close</mat-icon>
                </button>
              }
              <mat-autocomplete #pcAuto="matAutocomplete">
                @for (pc of pcOptions(); track pc.equipmentId) {
                  <mat-option [value]="pcLabel(pc)" (onSelectionChange)="$event.isUserInput && selectPc(pc)">{{ pcLabel(pc) }}</mat-option>
                }
              </mat-autocomplete>
              @if (pcError()) {
                <mat-hint class="hint-error">Seleccione un PC de la lista o deje el campo vacio</mat-hint>
              } @else {
                <mat-hint>Solo equipos que no son monitores ni estan retirados</mat-hint>
              }
            </mat-form-field>
          </div>
        }

        <mat-divider></mat-divider>

        <h3 class="section-title"><mat-icon>location_on</mat-icon> Ubicacion</h3>
        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Sede</mat-label>
            <mat-select [(ngModel)]="selectedSede" name="sede">
              <mat-option value="">-- Sin asignar --</mat-option>
              @for (sede of sedeOptions(); track sede) {
                <mat-option [value]="sede">{{ sede }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Area</mat-label>
            <mat-select [(ngModel)]="selectedArea" name="area">
              <mat-option value="">-- Sin asignar --</mat-option>
              @for (area of areaOptions(); track area) {
                <mat-option [value]="area">{{ area }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Centro de Costo</mat-label>
            <mat-select [(ngModel)]="selectedCostCenter" name="costCenter">
              <mat-option value="">-- Sin asignar --</mat-option>
              @for (cc of costCenterOptions(); track cc) {
                <mat-option [value]="cc">{{ cc }}</mat-option>
              }
            </mat-select>
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
                <mat-label>Valor mensual del alquiler</mat-label>
                <input matInput [(ngModel)]="equipment.rentalInfo!.monthlyValue" type="number" min="0">
                <span matPrefix>$&nbsp;</span>
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
            <mat-panel-description>Opcional - Procesador, RAM, disco, sistema operativo</mat-panel-description>
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
              <mat-hint>{{ hintSalud() }}</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Temperatura del Disco (C)</mat-label>
              <input matInput [(ngModel)]="equipment.hardware!.diskTemperatureCelsius" type="number" min="0" max="120">
              <mat-hint>{{ hintTemp() }}</mat-hint>
            </mat-form-field>
            <h4 class="subsection-title full-column"><mat-icon>apps</mat-icon> Software y licenciamiento</h4>
            <mat-form-field appearance="outline">
              <mat-label>Sistema Operativo</mat-label>
              <mat-select [(ngModel)]="equipment.operatingSystem" (selectionChange)="onOsChange()">
                <mat-option value="">-- Sin especificar --</mat-option>
                @for (so of sistemasOperativos; track so.value) {
                  <mat-option [value]="so.value">{{ so.label }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            @if (esWindows()) {
              <mat-form-field appearance="outline">
                <mat-label>Software</mat-label>
                <mat-select [(ngModel)]="equipment.osEdition">
                  <mat-option value="">-- Sin especificar --</mat-option>
                  @for (ed of osEditions; track ed.value) {
                    <mat-option [value]="ed.value">{{ ed.label }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Version</mat-label>
                <mat-select [(ngModel)]="equipment.osVersion">
                  <mat-option value="">-- Sin especificar --</mat-option>
                  @for (v of versionesWindows(); track v) {
                    <mat-option [value]="v">{{ v }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            } @else {
              <mat-form-field appearance="outline">
                <mat-label>Distribucion / Version</mat-label>
                <input matInput [(ngModel)]="equipment.osVersion" placeholder="Ej: UBUNTU 24.04">
              </mat-form-field>
            }
            <mat-form-field appearance="outline">
              <mat-label>Tipo de licencia</mat-label>
              <mat-select [(ngModel)]="equipment.osLicenseType">
                <mat-option value="">-- Sin especificar --</mat-option>
                @for (lt of osLicenseTypes; track lt.value) {
                  <mat-option [value]="lt.value">{{ lt.label }}</mat-option>
                }
              </mat-select>
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
    /* Auto-uppercase para inputs de texto */
    input:not([type="date"]):not([type="number"]):not([type="email"]) { text-transform: uppercase; }
    textarea { text-transform: uppercase; }
    .hint-error { color: #F87171; }
    .subsection-title { display: flex; align-items: center; gap: 6px; margin: 8px 0 0; color: #93C5FD; font-size: 14px; font-weight: 600; }
    .subsection-title mat-icon { font-size: 18px; width: 18px; height: 18px; }
  `]
})
export class EquipmentFormComponent implements OnInit {
  equipment: CreateEquipmentRequest = {
    name: '', category: '', serialNumber: '', inventoryNumber: '', brand: '', model: '', macAddress: '', macAddress2: '',
    purchaseDate: '', purchaseValue: undefined, assignedTo: '',
    ownershipType: 'OWNED',
    rentalInfo: { rentalCompany: '', contactName: '', contactPhone: '', contactEmail: '', startDate: '', endDate: '', contractNumber: '', monthlyValue: undefined, contractFileUrl: '', notes: '' },
    hardware: { processor: '', ramSizeGb: undefined, ramType: '', diskType: '', diskSizeGb: undefined, diskHealthPercent: undefined, diskTemperatureCelsius: undefined },
    operatingSystem: '',
    osVersion: '',
    osEdition: '',
    osLicenseType: '',
    responsiblePosition: '', responsibleDocument: '', responsiblePhone: '', responsibleEmail: '',
    ipAddress: '', ipAssignment: '', associatedEquipmentId: ''
  };
  /** Equipos candidatos a PC asociado (no monitores, no retirados). */
  private pcCandidates = signal<Equipment[]>([]);
  private pcCandidatesLoaded = false;
  pcText = signal('');
  pcOptions = computed(() => {
    const q = this.pcText().trim().toUpperCase();
    const list = this.pcCandidates();
    const filtered = q ? list.filter(e => (e.name || '').toUpperCase().includes(q) || (e.inventoryNumber || '').toUpperCase().includes(q)) : list;
    return filtered.slice(0, 50);
  });
  readonly sistemasOperativos = SISTEMAS_OPERATIVOS;
  readonly osEditions = OS_EDITIONS;
  readonly osLicenseTypes = OS_LICENSE_TYPES;
  /** Valor de osVersion cargado que no esta en la lista de Windows (para no perderlo). */
  private legacyOsVersion = '';

  esWindows(): boolean {
    return this.equipment.operatingSystem === 'WINDOWS';
  }

  /** Versiones de Windows + el valor antiguo si no esta en la lista. */
  versionesWindows(): string[] {
    const v = this.legacyOsVersion;
    return v && !OS_VERSIONES_WINDOWS.includes(v) ? [...OS_VERSIONES_WINDOWS, v] : OS_VERSIONES_WINDOWS;
  }

  /** Al cambiar de sistema operativo limpia los campos que no aplican. */
  onOsChange() {
    if (this.esWindows()) {
      const v = this.equipment.osVersion || '';
      if (v && !this.versionesWindows().includes(v)) this.equipment.osVersion = '';
    } else {
      this.equipment.osEdition = '';
      const v = this.equipment.osVersion || '';
      if (v && OS_VERSIONES_WINDOWS.includes(v)) this.equipment.osVersion = '';
    }
    if (!this.equipment.operatingSystem || this.equipment.operatingSystem === 'N/A') {
      this.equipment.osVersion = '';
      this.equipment.osLicenseType = '';
    }
  }
  isEditMode = signal(false);
  sedes = signal<Sede[]>([]);
  deviceTypes = signal<DeviceType[]>([]);
  areas = signal<Area[]>([]);
  selectedSede = '';
  selectedArea = '';
  costCenters = signal<CostCenter[]>([]);
  selectedCostCenter = '';
  private legacyCostCenter = signal('');
  costCenterOptions = computed(() => {
    const names = this.costCenters().map(c => c.name);
    const extra = this.legacyCostCenter();
    return extra && !names.some(n => n.toUpperCase() === extra.toUpperCase()) ? [extra, ...names] : names;
  });
  /** Valores guardados en el equipo que ya no existen en el catalogo (se conservan como opcion). */
  private legacySede = signal('');
  private legacyArea = signal('');
  sedeOptions = computed(() => {
    const names = this.sedes().map(s => s.name);
    const extra = this.legacySede();
    return extra && !names.some(n => n.toUpperCase() === extra.toUpperCase()) ? [extra, ...names] : names;
  });
  areaOptions = computed(() => {
    const names = this.areas().map(a => a.name);
    const extra = this.legacyArea();
    return extra && !names.some(n => n.toUpperCase() === extra.toUpperCase()) ? [extra, ...names] : names;
  });
  loading = signal(false);
  equipmentId = '';

  constructor(
    private equipmentService: EquipmentService,
    private sedeService: SedeService,
    private areaService: AreaService,
    private deviceTypeService: DeviceTypeService,
    private costCenterService: CostCenterService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private thresholdsService: HardwareThresholdsService
  ) {}

  /** Texto de ayuda de salud del disco, construido con los umbrales configurados. */
  hintSalud(): string {
    const t = this.thresholdsService.thresholds();
    return `Menor a ${t.diskHealthWarning}% = advertencia, menor a ${t.diskHealthCritical}% = critico`;
  }

  /** Texto de ayuda de temperatura del disco, construido con los umbrales configurados. */
  hintTemp(): string {
    const t = this.thresholdsService.thresholds();
    return `Mayor a ${t.diskTempWarning}C = advertencia, desde ${t.diskTempCritical}C = critico`;
  }

  ngOnInit() {
    this.thresholdsService.load().subscribe(() => this.cdr.detectChanges());
    this.equipmentId = this.route.snapshot.paramMap.get('id') || '';
    this.loadSedes();
    this.loadAreas();
    this.loadCostCenters();
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

  loadAreas() {
    this.areaService.listActive().subscribe({
      next: (a) => { this.areas.set(a); this.cdr.detectChanges(); }
    });
  }

  loadCostCenters() {
    this.costCenterService.listActive().subscribe({
      next: (c) => { this.costCenters.set(c); this.cdr.detectChanges(); }
    });
  }

  /** True si la categoria seleccionada corresponde a un portatil (requiere MAC Ethernet y WiFi). */
  isLaptop(): boolean {
    const c = (this.equipment.category || '').toUpperCase();
    return c.includes('LAPTOP') || c.includes('PORTATIL') || c.includes('PORTÁTIL');
  }

  /** True si la categoria seleccionada es un monitor (permite asociarlo a un PC). */
  isMonitor(): boolean {
    const m = esMonitor(this.equipment.category);
    if (m && !this.pcCandidatesLoaded) this.loadPcCandidates();
    return m;
  }

  private loadPcCandidates() {
    this.pcCandidatesLoaded = true;
    this.equipmentService.list(0, 2000).subscribe({
      next: (res) => {
        const list = (res.content || []).filter(e =>
          !esMonitor(e.category) && (e.status || '').toUpperCase() !== 'RETIRED' && e.equipmentId !== this.equipmentId);
        list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
        this.pcCandidates.set(list);
        this.cdr.detectChanges();
      },
      error: () => { this.pcCandidatesLoaded = false; }
    });
  }

  pcLabel(e: { name?: string; inventoryNumber?: string }): string {
    return (e.name || '') + (e.inventoryNumber ? ' (' + e.inventoryNumber + ')' : '');
  }

  onPcInput(v: string) {
    this.pcText.set(typeof v === 'string' ? v : '');
    // Si el texto ya no corresponde al PC seleccionado, se pierde la seleccion
    if (this.equipment.associatedEquipmentId && this.pcText() !== this.selectedPcLabel) {
      this.equipment.associatedEquipmentId = '';
    }
  }

  private selectedPcLabel = '';

  selectPc(pc: Equipment) {
    this.equipment.associatedEquipmentId = pc.equipmentId;
    this.selectedPcLabel = this.pcLabel(pc);
    this.pcText.set(this.selectedPcLabel);
  }

  clearPc() {
    this.equipment.associatedEquipmentId = '';
    this.selectedPcLabel = '';
    this.pcText.set('');
  }

  /** Deja solo digitos en el celular. */
  onlyDigits(event: any) {
    const v = String(event.target.value || '').replace(/\D/g, '').substring(0, 10);
    this.equipment.responsiblePhone = v;
    event.target.value = v;
  }

  phoneError(): boolean {
    const v = (this.equipment.responsiblePhone || '').trim();
    return !!v && !/^\d{10}$/.test(v);
  }

  emailError(): boolean {
    const v = (this.equipment.responsibleEmail || '').trim();
    return !!v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
  }

  ipError(): boolean {
    const v = (this.equipment.ipAddress || '').trim();
    if (!v) return false;
    const m = v.match(/^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/);
    return !m || m.slice(1).some(o => Number(o) > 255 || (o.length > 1 && o.startsWith('0')));
  }

  /** Hay texto en PC asociado que no corresponde a un PC seleccionado. */
  pcError(): boolean {
    return this.isMonitor() && !!this.pcText().trim() && !this.equipment.associatedEquipmentId;
  }

  loadDeviceTypes() {
    this.deviceTypeService.listActive().subscribe({
      next: (types) => { this.deviceTypes.set(types); this.cdr.detectChanges(); }
    });
  }

  loadEquipment() {
    this.equipmentService.getById(this.equipmentId).subscribe({
      next: (e: any) => {
        this.legacyOsVersion = e.operatingSystem === 'WINDOWS' ? (e.osVersion || '') : '';
        this.equipment = {
          name: e.name,
          category: e.category,
          serialNumber: e.serialNumber,
          inventoryNumber: e.inventoryNumber || '',
          brand: e.brand,
          model: e.model || '',
          macAddress: e.macAddress || '',
          macAddress2: e.macAddress2 || '',
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
            monthlyValue: e.rentalInfo?.monthlyValue ?? undefined,
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
          },
          operatingSystem: e.operatingSystem || '',
          osVersion: e.osVersion || '',
          osEdition: e.osEdition || '',
          osLicenseType: e.osLicenseType || '',
          responsiblePosition: e.responsiblePosition || '',
          responsibleDocument: e.responsibleDocument || '',
          responsiblePhone: e.responsiblePhone || '',
          responsibleEmail: e.responsibleEmail || '',
          ipAddress: e.ipAddress || '',
          ipAssignment: (e.ipAssignment || '').toUpperCase(),
          associatedEquipmentId: e.associatedEquipmentId || ''
        };
        if (e.associatedEquipmentId) {
          this.selectedPcLabel = this.pcLabel({ name: e.associatedEquipmentName || '', inventoryNumber: e.associatedEquipmentInventory });
          this.pcText.set(this.selectedPcLabel || e.associatedEquipmentId);
          if (!this.selectedPcLabel) this.selectedPcLabel = e.associatedEquipmentId;
        }
        this.selectedSede = e.location?.building || '';
        this.selectedArea = e.location?.office || '';
        this.legacySede.set(this.selectedSede);
        this.legacyArea.set(this.selectedArea);
        this.selectedCostCenter = e.costCenter || '';
        this.legacyCostCenter.set(this.selectedCostCenter);
        this.cdr.detectChanges();
      },
      error: () => {
        this.snackBar.open('Error al cargar el equipo', 'OK', { duration: 3000 });
        this.router.navigate(['/equipment']);
      }
    });
  }

  /** Construye la ubicacion a partir de Sede y Area; undefined si ambas estan vacias. */
  private buildLocation() {
    const building = this.selectedSede || '';
    const office = this.selectedArea || '';
    if (!building && !office) return undefined;
    return { building, floor: '', office };
  }

  save() {
    if (!this.equipment.name?.trim() || !this.equipment.category) {
      const missing = [!this.equipment.name?.trim() ? 'Nombre' : '', !this.equipment.category ? 'Categoria' : ''].filter(Boolean).join(' y ');
      this.snackBar.open('Campo obligatorio: ' + missing, 'OK', { duration: 4000 });
      return;
    }
    const errores: string[] = [];
    if (this.phoneError()) errores.push('Celular (10 digitos, solo numeros)');
    if (this.emailError()) errores.push('Correo electronico');
    if (this.ipError()) errores.push('Direccion IP (IPv4)');
    if (this.pcError()) errores.push('PC asociado (seleccione de la lista)');
    if (errores.length) {
      this.snackBar.open('Corrija los campos: ' + errores.join(', '), 'OK', { duration: 5000 });
      return;
    }
    const monitor = esMonitor(this.equipment.category);
    const t = (v: string | undefined) => (v || '').trim();
    const laptop = this.isLaptop();
    const macAddress2 = laptop ? (this.equipment.macAddress2 || undefined) : undefined;
    const location = this.buildLocation();
    this.loading.set(true);
    const cleanRental = this.hasRentalData() ? { ...this.equipment.rentalInfo!, monthlyValue: this.monthlyValueOrUndefined() } : undefined;
    const costCenter = this.selectedCostCenter || '';
    const cleanHardware = this.hasHardwareData() ? this.equipment.hardware : undefined;

    if (this.isEditMode()) {
      const updateData: any = {
        name: this.equipment.name,
        inventoryNumber: this.equipment.inventoryNumber || undefined,
        brand: this.equipment.brand || undefined,
        model: this.equipment.model || undefined,
        macAddress: this.equipment.macAddress || undefined,
        macAddress2,
        assignedTo: this.equipment.assignedTo || undefined,
        // En edicion siempre se envia la ubicacion para permitir desasignar Sede/Area
        location: location ?? { building: '', floor: '', office: '' },
        ownershipType: this.equipment.ownershipType,
        // En edicion se envia '' para permitir desasignar el centro de costo
        costCenter,
        rentalInfo: cleanRental,
        hardware: cleanHardware,
        // En edicion se envia '' para permitir limpiar el sistema operativo
        operatingSystem: this.equipment.operatingSystem || '',
        osVersion: (this.equipment.osVersion || '').trim(),
        osEdition: this.esWindows() ? (this.equipment.osEdition || '') : '',
        osLicenseType: this.equipment.osLicenseType || '',
        // En edicion se envia '' para permitir limpiar responsable, red y PC asociado
        responsiblePosition: t(this.equipment.responsiblePosition),
        responsibleDocument: t(this.equipment.responsibleDocument),
        responsiblePhone: t(this.equipment.responsiblePhone),
        responsibleEmail: t(this.equipment.responsibleEmail),
        ipAddress: t(this.equipment.ipAddress),
        ipAssignment: this.equipment.ipAssignment || '',
        associatedEquipmentId: monitor ? (this.equipment.associatedEquipmentId || '') : undefined
      };
      this.equipmentService.update(this.equipmentId, updateData).subscribe({
        next: () => {
          this.snackBar.open('Equipo actualizado exitosamente', 'OK', { duration: 3000 });
          this.router.navigate(['/equipment', this.equipmentId]);
        },
        error: (err) => {
          this.loading.set(false);
          this.snackBar.open('Error: ' + (err.error?.message || 'Error al actualizar'), 'OK', { duration: 5000 });
          this.cdr.detectChanges();
        }
      });
    } else {
      const createData: any = {
        ...this.equipment,
        serialNumber: this.equipment.serialNumber || undefined,
        brand: this.equipment.brand || undefined,
        model: this.equipment.model || undefined,
        name: this.equipment.name.trim(),
        macAddress: this.equipment.macAddress || undefined,
        macAddress2,
        location,
        purchaseDate: this.equipment.purchaseDate || undefined,
        purchaseValue: this.equipment.purchaseValue || undefined,
        assignedTo: this.equipment.assignedTo || undefined,
        inventoryNumber: this.equipment.inventoryNumber || undefined,
        costCenter: costCenter || undefined,
        rentalInfo: cleanRental,
        hardware: cleanHardware,
        operatingSystem: this.equipment.operatingSystem || undefined,
        osVersion: (this.equipment.osVersion || '').trim() || undefined,
        osEdition: this.esWindows() ? (this.equipment.osEdition || undefined) : undefined,
        osLicenseType: this.equipment.osLicenseType || undefined,
        responsiblePosition: t(this.equipment.responsiblePosition) || undefined,
        responsibleDocument: t(this.equipment.responsibleDocument) || undefined,
        responsiblePhone: t(this.equipment.responsiblePhone) || undefined,
        responsibleEmail: t(this.equipment.responsibleEmail) || undefined,
        ipAddress: t(this.equipment.ipAddress) || undefined,
        ipAssignment: this.equipment.ipAssignment || undefined,
        associatedEquipmentId: monitor ? (this.equipment.associatedEquipmentId || undefined) : undefined
      };
      this.equipmentService.create(createData).subscribe({
        next: () => {
          this.snackBar.open('Equipo registrado exitosamente', 'OK', { duration: 3000 });
          this.router.navigate(['/equipment']);
        },
        error: (err) => {
          this.loading.set(false);
          this.snackBar.open('Error: ' + (err.error?.message || 'Error al guardar'), 'OK', { duration: 5000 });
          this.cdr.detectChanges();
        }
      });
    }
  }

  hasRentalData(): boolean {
    if (this.equipment.ownershipType !== 'RENTED') return false;
    const r = this.equipment.rentalInfo!;
    return !!(r.rentalCompany || r.contactName || r.startDate || r.endDate || r.contractNumber || this.monthlyValueOrUndefined() != null);
  }

  /** Valor mensual del alquiler como numero, o undefined si esta vacio/invalido. */
  private monthlyValueOrUndefined(): number | undefined {
    const v: any = this.equipment.rentalInfo?.monthlyValue;
    if (v === null || v === undefined || v === '') return undefined;
    const n = Number(v);
    return isNaN(n) ? undefined : n;
  }

  hasHardwareData(): boolean {
    const h = this.equipment.hardware!;
    return !!(h.processor || h.ramSizeGb || h.diskType || h.diskSizeGb || h.diskHealthPercent != null || h.diskTemperatureCelsius != null);
  }

  cancel() {
    if (this.isEditMode()) {
      this.router.navigate(['/equipment', this.equipmentId]);
    } else {
      this.router.navigate(['/equipment']);
    }
  }

  formatMac(event: any, field: 'macAddress' | 'macAddress2' = 'macAddress') {
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
    this.equipment[field] = formatted;
    event.target.value = formatted;
  }
}