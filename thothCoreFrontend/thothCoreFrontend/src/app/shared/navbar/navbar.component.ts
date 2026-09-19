import { Component, signal, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { AlertService } from '../../core/services/alert.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, MatIconModule, MatButtonModule, MatBadgeModule, MatTooltipModule],
  template: `
    <!-- Top Bar -->
    <div class="topbar">
      <button class="menu-toggle" (click)="toggleSidebar()">
        <mat-icon>{{ sidebarOpen() ? 'close' : 'menu' }}</mat-icon>
      </button>
      <div class="topbar-logo">
        <span class="logo-t">T</span>
        <span class="logo-text">THOTH</span>
      </div>
      <div class="topbar-spacer"></div>

      <!-- Alert badge button -->
      <button class="alert-btn" routerLink="/alerts" matTooltip="Centro de Alertas">
        <mat-icon [matBadge]="alertCount()" [matBadgeHidden]="alertCount() === 0" matBadgeColor="warn" matBadgeSize="small">notifications</mat-icon>
      </button>

      <div class="user-chip">
        <div class="avatar">{{ auth.currentUser()?.username?.charAt(0)?.toUpperCase() }}</div>
        <div class="user-text hide-mobile">
          <span class="user-name">{{ auth.currentUser()?.username }}</span>
          <span class="user-role">{{ auth.currentUser()?.role }}</span>
        </div>
      </div>
    </div>

    <!-- Sidebar -->
    <nav class="sidebar" [class.open]="sidebarOpen()">
      <div class="sidebar-content">
        <a class="nav-item" routerLink="/dashboard" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>dashboard</mat-icon><span>Dashboard</span>
        </a>
        @if (auth.canView("EQUIPMENT")) {
        <a class="nav-item" routerLink="/equipment" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>computer</mat-icon><span>Equipos</span>
        </a>
        }
        @if (auth.canView("CALENDAR")) {
        <a class="nav-item" routerLink="/calendar" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>calendar_month</mat-icon><span>Calendario</span>
        </a>
        }
        @if (auth.canView("ALERTS")) {
        <a class="nav-item" routerLink="/alerts" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>notifications</mat-icon><span>Alertas</span>
          @if (alertCount() > 0) {
            <span class="nav-badge">{{ alertCount() }}</span>
          }
        </a>
        }
        @if (auth.canView("MAINTENANCE")) {
        <a class="nav-item" routerLink="/maintenance" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>build</mat-icon><span>Mantenimiento</span>
        </a>
        }
        @if (auth.canView("REPORTS")) {
        <a class="nav-item" routerLink="/reports" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>analytics</mat-icon><span>Reportes</span>
        </a>
        <a class="nav-item" routerLink="/maintenance-report" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>assessment</mat-icon><span>Inf. Mantenim.</span>
        </a>
        }
        @if (auth.canView("QR")) {
        <a class="nav-item" routerLink="/scan" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>qr_code_scanner</mat-icon><span>Escanear QR</span>
        </a>
        }

        <a class="nav-item" routerLink="/audit-log" routerLinkActive="active" (click)="closeMobile()">
          <mat-icon>history</mat-icon><span>Auditoria</span>
        </a>

        @if (auth.currentUser()?.role === 'ADMIN') {
          <div class="nav-divider"></div>
          <span class="nav-section">ADMIN</span>
          <a class="nav-item" routerLink="/sedes" routerLinkActive="active" (click)="closeMobile()">
            <mat-icon>business</mat-icon><span>Sedes</span>
          </a>
          <a class="nav-item" routerLink="/users" routerLinkActive="active" (click)="closeMobile()">
            <mat-icon>people</mat-icon><span>Usuarios</span>
          </a>
        }
      </div>

      <div class="sidebar-footer">
        <a class="nav-item logout" (click)="logout()">
          <mat-icon>logout</mat-icon><span>Cerrar Sesion</span>
        </a>
      </div>
    </nav>

    <!-- Overlay for mobile -->
    @if (sidebarOpen()) {
      <div class="overlay" (click)="closeMobile()"></div>
    }
  `,
  styles: [`
    /* TOP BAR */
    .topbar {
      position: fixed; top: 0; left: 0; right: 0; height: 56px; z-index: 1001;
      display: flex; align-items: center; gap: 12px; padding: 0 16px;
      background: rgba(15, 23, 42, 0.95); backdrop-filter: blur(20px);
      border-bottom: 1px solid rgba(148, 163, 184, 0.1);
    }
    .menu-toggle { background: none; border: none; color: #94A3B8; cursor: pointer; display: none; }
    .topbar-logo { display: flex; align-items: center; gap: 8px; }
    .logo-t {
      width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      color: white; font-weight: 800; font-size: 18px;
    }
    .logo-text { font-weight: 700; font-size: 18px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    .topbar-spacer { flex: 1; }
    .alert-btn {
      background: none; border: none; cursor: pointer; color: #94A3B8; padding: 8px;
      border-radius: 8px; transition: all 0.2s; position: relative;
    }
    .alert-btn:hover { background: rgba(59, 130, 246, 0.1); color: #60A5FA; }
    .user-chip { display: flex; align-items: center; gap: 10px; padding: 4px 12px 4px 4px; border-radius: 20px; background: rgba(30, 41, 59, 0.8); border: 1px solid rgba(148, 163, 184, 0.15); }
    .avatar {
      width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #3B82F6, #8B5CF6);
      color: white; font-weight: 700; font-size: 14px;
    }
    .user-text { display: flex; flex-direction: column; }
    .user-name { color: #E2E8F0; font-size: 13px; font-weight: 600; }
    .user-role { color: #64748B; font-size: 10px; text-transform: uppercase; letter-spacing: 1px; }

    /* SIDEBAR */
    .sidebar {
      position: fixed; top: 56px; left: 0; bottom: 0; width: 220px; z-index: 1000;
      background: rgba(15, 23, 42, 0.98); backdrop-filter: blur(20px);
      border-right: 1px solid rgba(148, 163, 184, 0.1);
      display: flex; flex-direction: column; overflow-y: auto;
      transition: transform 0.3s ease;
    }
    .sidebar-content { flex: 1; padding: 12px 8px; }
    .sidebar-footer { padding: 12px 8px; border-top: 1px solid rgba(148, 163, 184, 0.1); }

    .nav-item {
      display: flex; align-items: center; gap: 12px; padding: 10px 14px; border-radius: 10px;
      color: #94A3B8; text-decoration: none; font-size: 14px; font-weight: 500;
      transition: all 0.2s; cursor: pointer; margin-bottom: 2px; position: relative;
    }
    .nav-item:hover { color: #E2E8F0; background: rgba(59, 130, 246, 0.1); }
    .nav-item.active { color: #60A5FA; background: rgba(59, 130, 246, 0.15); font-weight: 600; }
    .nav-item.active::before {
      content: ''; position: absolute; left: 0; top: 8px; bottom: 8px; width: 3px;
      background: linear-gradient(180deg, #3B82F6, #8B5CF6); border-radius: 0 3px 3px 0;
    }
    .nav-item mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .nav-item.logout { color: #EF4444; }
    .nav-item.logout:hover { background: rgba(239, 68, 68, 0.1); }

    .nav-badge {
      margin-left: auto; padding: 2px 8px; border-radius: 10px; font-size: 11px;
      font-weight: 700; background: rgba(239, 68, 68, 0.2); color: #FCA5A5;
    }
    .nav-divider { height: 1px; background: rgba(148, 163, 184, 0.1); margin: 12px 14px; }
    .nav-section { display: block; color: #475569; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 2px; padding: 8px 14px; }

    .overlay { display: none; }

    @media (max-width: 768px) {
      .menu-toggle { display: block; }
      .sidebar { transform: translateX(-100%); width: 260px; }
      .sidebar.open { transform: translateX(0); }
      .overlay { display: block; position: fixed; inset: 0; top: 56px; background: rgba(0,0,0,0.5); z-index: 999; }
      .hide-mobile { display: none !important; }
      .logo-text { display: none; }
    }
  `]
})
export class NavbarComponent implements OnInit {
  sidebarOpen = signal(false);
  alertCount = signal(0);

  constructor(
    public auth: AuthService,
    private alertService: AlertService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadAlertCount();
    setInterval(() => this.loadAlertCount(), 60000);
  }

  loadAlertCount() {
    this.alertService.getSummary().subscribe({
      next: (s) => { this.alertCount.set(s.total || 0); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  toggleSidebar() { this.sidebarOpen.set(!this.sidebarOpen()); }
  closeMobile() { this.sidebarOpen.set(false); }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
    this.closeMobile();
  }
}