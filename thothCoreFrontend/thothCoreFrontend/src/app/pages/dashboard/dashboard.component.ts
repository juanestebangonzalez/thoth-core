import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { EquipmentService } from '../../core/services/equipment.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  template: `
    <div class="dashboard">
      <div class="welcome-section">
        <div class="welcome-content">
          <h1>Bienvenido, {{ auth.currentUser()?.username }}</h1>
          <p>Centro de Operaciones THOTH C.O.R.E</p>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title"><span class="dot"></span> Acciones Rapidas</h3>
        <div class="cards-grid actions-grid">
          <a class="action-card" routerLink="/equipment/new">
            <div class="icon-wrapper green-bg"><mat-icon>add_circle_outline</mat-icon></div>
            <div class="card-content"><h3>Nuevo Equipo</h3><p>Registrar activo</p></div>
            <mat-icon class="arrow">arrow_forward</mat-icon>
          </a>
          <a class="action-card" routerLink="/calendar">
            <div class="icon-wrapper blue-bg"><mat-icon>calendar_month</mat-icon></div>
            <div class="card-content"><h3>Calendario</h3><p>Ver programacion</p></div>
            <mat-icon class="arrow">arrow_forward</mat-icon>
          </a>
          <a class="action-card" routerLink="/reports">
            <div class="icon-wrapper purple-bg"><mat-icon>analytics</mat-icon></div>
            <div class="card-content"><h3>Reportes</h3><p>Dashboard gerencial</p></div>
            <mat-icon class="arrow">arrow_forward</mat-icon>
          </a>
          <a class="action-card" routerLink="/scan">
            <div class="icon-wrapper orange-bg"><mat-icon>qr_code_scanner</mat-icon></div>
            <div class="card-content"><h3>Escanear QR</h3><p>Buscar equipo</p></div>
            <mat-icon class="arrow">arrow_forward</mat-icon>
          </a>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title"><span class="dot"></span> Estado de Equipos</h3>
        <div class="cards-grid stats-grid">
          <a class="stat-card" routerLink="/equipment">
            <div class="stat-icon-wrapper blue-gradient"><mat-icon>computer</mat-icon></div>
            <div class="stat-content"><span class="stat-number">{{ totalEquipment() }}</span><span class="stat-label">Total</span></div>
            <div class="stat-bar blue-bar"></div>
          </a>
          <a class="stat-card" routerLink="/equipment/status/active">
            <div class="stat-icon-wrapper green-gradient"><mat-icon>check_circle</mat-icon></div>
            <div class="stat-content"><span class="stat-number">{{ activeCount() }}</span><span class="stat-label">Activos</span></div>
            <div class="stat-bar green-bar"></div>
          </a>
          <a class="stat-card" routerLink="/maintenance">
            <div class="stat-icon-wrapper orange-gradient"><mat-icon>build</mat-icon></div>
            <div class="stat-content"><span class="stat-number">{{ maintenanceCount() }}</span><span class="stat-label">Mantenimiento</span></div>
            <div class="stat-bar orange-bar"></div>
          </a>
          <a class="stat-card" routerLink="/equipment/status/inactive">
            <div class="stat-icon-wrapper red-gradient"><mat-icon>block</mat-icon></div>
            <div class="stat-content"><span class="stat-number">{{ inactiveCount() }}</span><span class="stat-label">Inactivos</span></div>
            <div class="stat-bar red-bar"></div>
          </a>
          <a class="stat-card" routerLink="/equipment/status/retired">
            <div class="stat-icon-wrapper gray-gradient"><mat-icon>archive</mat-icon></div>
            <div class="stat-content"><span class="stat-number">{{ retiredCount() }}</span><span class="stat-label">Retirados</span></div>
            <div class="stat-bar gray-bar"></div>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { padding: 32px; max-width: 1200px; margin: 0 auto; }
    .welcome-section { margin-bottom: 32px; padding: 28px; background: linear-gradient(135deg, rgba(59,130,246,0.15), rgba(139,92,246,0.15)); border-radius: 20px; border: 1px solid rgba(148,163,184,0.1); }
    .welcome-content h1 { margin: 0 0 8px; font-size: 28px; font-weight: 700; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    .welcome-content p { margin: 0; color: #94A3B8; font-size: 14px; }
    .section { margin-bottom: 32px; }
    .section-title { display: flex; align-items: center; gap: 10px; color: #E2E8F0; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 16px; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: linear-gradient(135deg, #3B82F6, #8B5CF6); box-shadow: 0 0 10px rgba(59,130,246,0.6); }
    .cards-grid { display: grid; gap: 16px; }
    .actions-grid { grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); }
    .stats-grid { grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); }
    .action-card { display: flex; align-items: center; gap: 16px; padding: 20px; border-radius: 16px; background: rgba(30,41,59,0.7); backdrop-filter: blur(20px); border: 1px solid rgba(148,163,184,0.1); text-decoration: none; cursor: pointer; transition: all 0.3s; overflow: hidden; }
    .action-card:hover { transform: translateY(-4px); box-shadow: 0 12px 40px rgba(0,0,0,0.4); border-color: rgba(148,163,184,0.3); }
    .icon-wrapper { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .icon-wrapper mat-icon { font-size: 24px; width: 24px; height: 24px; color: white; }
    .green-bg { background: linear-gradient(135deg, #10B981, #059669); }
    .blue-bg { background: linear-gradient(135deg, #3B82F6, #1E40AF); }
    .purple-bg { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
    .orange-bg { background: linear-gradient(135deg, #F59E0B, #D97706); }
    .card-content { flex: 1; }
    .card-content h3 { margin: 0 0 2px; color: #F1F5F9; font-size: 16px; font-weight: 600; }
    .card-content p { margin: 0; color: #94A3B8; font-size: 12px; }
    .arrow { color: #64748B; transition: transform 0.3s; }
    .action-card:hover .arrow { transform: translateX(4px); color: #E2E8F0; }
    .stat-card { position: relative; padding: 20px; border-radius: 16px; background: rgba(30,41,59,0.7); backdrop-filter: blur(20px); border: 1px solid rgba(148,163,184,0.1); text-decoration: none; cursor: pointer; transition: all 0.3s; overflow: hidden; }
    .stat-card:hover { transform: translateY(-4px); box-shadow: 0 12px 40px rgba(0,0,0,0.4); border-color: rgba(148,163,184,0.3); }
    .stat-icon-wrapper { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
    .stat-icon-wrapper mat-icon { font-size: 22px; width: 22px; height: 22px; color: white; }
    .blue-gradient { background: linear-gradient(135deg, #3B82F6, #1E40AF); }
    .green-gradient { background: linear-gradient(135deg, #10B981, #047857); }
    .orange-gradient { background: linear-gradient(135deg, #F59E0B, #D97706); }
    .red-gradient { background: linear-gradient(135deg, #EF4444, #B91C1C); }
    .gray-gradient { background: linear-gradient(135deg, #64748B, #334155); }
    .stat-content { display: flex; flex-direction: column; }
    .stat-number { font-size: 32px; font-weight: 700; color: #F1F5F9; line-height: 1; margin-bottom: 4px; }
    .stat-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; }
    .stat-bar { position: absolute; bottom: 0; left: 0; height: 3px; width: 100%; transform: scaleX(0); transform-origin: left; transition: transform 0.4s ease; }
    .stat-card:hover .stat-bar { transform: scaleX(1); }
    .blue-bar { background: linear-gradient(90deg, #3B82F6, #1E40AF); }
    .green-bar { background: linear-gradient(90deg, #10B981, #047857); }
    .orange-bar { background: linear-gradient(90deg, #F59E0B, #D97706); }
    .red-bar { background: linear-gradient(90deg, #EF4444, #B91C1C); }
    .gray-bar { background: linear-gradient(90deg, #64748B, #334155); }
    @media (max-width: 768px) {
      .dashboard { padding: 16px; }
      .actions-grid { grid-template-columns: 1fr; }
      .stats-grid { grid-template-columns: repeat(2, 1fr); }
    }
  `]
})
export class DashboardComponent implements OnInit {
  totalEquipment = signal(0);
  activeCount = signal(0);
  maintenanceCount = signal(0);
  inactiveCount = signal(0);
  retiredCount = signal(0);

  constructor(public auth: AuthService, private equipmentService: EquipmentService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.loadStats(); }

  loadStats() {
    this.equipmentService.list(0, 200).subscribe({
      next: (res) => {
        const eq = res.content || [];
        this.totalEquipment.set(res.totalElements || 0);
        this.activeCount.set(eq.filter(e => { const s = e.status?.toLowerCase() || ''; return s === 'active' || s === 'activo'; }).length);
        this.maintenanceCount.set(eq.filter(e => { const s = e.status?.toLowerCase() || ''; return s.includes('mantenimiento') || s.includes('maintenance'); }).length);
        this.inactiveCount.set(eq.filter(e => { const s = e.status?.toLowerCase() || ''; return s === 'inactive' || s === 'inactivo'; }).length);
        this.retiredCount.set(eq.filter(e => { const s = e.status?.toLowerCase() || ''; return s === 'retired' || s === 'retirado'; }).length);
        this.cdr.detectChanges();
      }
    });
  }
}