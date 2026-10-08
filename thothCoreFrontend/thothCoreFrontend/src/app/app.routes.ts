import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { permissionGuard } from './core/guards/permission.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [authGuard] },
  { path: 'equipment', loadComponent: () => import('./pages/equipment-list/equipment-list.component').then(m => m.EquipmentListComponent), canActivate: [permissionGuard('EQUIPMENT')] },
  { path: 'equipment/new', loadComponent: () => import('./pages/equipment-form/equipment-form.component').then(m => m.EquipmentFormComponent), canActivate: [permissionGuard('EQUIPMENT', 'CREATE')] },
  { path: 'equipment/import', loadComponent: () => import('./pages/equipment-import/equipment-import.component').then(m => m.EquipmentImportComponent), canActivate: [permissionGuard('IMPORT', 'CREATE')] },
  { path: 'equipment/status/:status', loadComponent: () => import('./pages/equipment-by-status/equipment-by-status.component').then(m => m.EquipmentByStatusComponent), canActivate: [permissionGuard('EQUIPMENT')] },
  { path: 'equipment/:id/edit', loadComponent: () => import('./pages/equipment-form/equipment-form.component').then(m => m.EquipmentFormComponent), canActivate: [permissionGuard('EQUIPMENT', 'EDIT')] },
  { path: 'equipment/:id/history', loadComponent: () => import('./pages/equipment-history/equipment-history.component').then(m => m.EquipmentHistoryComponent), canActivate: [permissionGuard('MAINTENANCE')] },
  { path: 'equipment/:id', loadComponent: () => import('./pages/equipment-detail/equipment-detail.component').then(m => m.EquipmentDetailComponent), canActivate: [permissionGuard('EQUIPMENT')] },
  { path: 'maintenance', loadComponent: () => import('./pages/maintenance-list/maintenance-list.component').then(m => m.MaintenanceListComponent), canActivate: [permissionGuard('MAINTENANCE')] },
  { path: 'ai/:id', loadComponent: () => import('./pages/ai-analysis/ai-analysis.component').then(m => m.AIAnalysisComponent), canActivate: [permissionGuard('AI')] },
  { path: 'alerts', loadComponent: () => import('./pages/alerts/alerts.component').then(m => m.AlertsComponent), canActivate: [permissionGuard('ALERTS')] },
  { path: 'calendar', loadComponent: () => import('./pages/calendar/calendar.component').then(m => m.CalendarComponent), canActivate: [permissionGuard('CALENDAR')] },
  { path: 'maintenance-report', loadComponent: () => import('./pages/maintenance-report/maintenance-report.component').then(m => m.MaintenanceReportComponent), canActivate: [permissionGuard('REPORTS')] },
  { path: 'parts-report', loadComponent: () => import('./pages/parts-report/parts-report.component').then(m => m.PartsReportComponent), canActivate: [permissionGuard('REPORTS')] },
  { path: 'rented-report', loadComponent: () => import('./pages/rented-report/rented-report.component').then(m => m.RentedReportComponent), canActivate: [permissionGuard('RENTALS')] },
  { path: 'reports', loadComponent: () => import('./pages/reports/reports.component').then(m => m.ReportsComponent), canActivate: [permissionGuard('REPORTS')] },
  { path: 'scan', loadComponent: () => import('./pages/qr-scanner/qr-scanner.component').then(m => m.QrScannerComponent), canActivate: [permissionGuard('QR')] },
  { path: 'audit-log', loadComponent: () => import('./pages/audit-log/audit-log.component').then(m => m.AuditLogComponent), canActivate: [permissionGuard('AUDIT')] },
  { path: 'sedes', loadComponent: () => import('./pages/sedes/sedes.component').then(m => m.SedesComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'areas', loadComponent: () => import('./pages/areas/areas.component').then(m => m.AreasComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'cost-centers', loadComponent: () => import('./pages/cost-centers/cost-centers.component').then(m => m.CostCentersComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'device-types', loadComponent: () => import('./pages/device-types/device-types.component').then(m => m.DeviceTypesComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'peripheral-types', loadComponent: () => import('./pages/peripheral-types/peripheral-types.component').then(m => m.PeripheralTypesComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'maintenance-categories', loadComponent: () => import('./pages/maintenance-categories/maintenance-categories.component').then(m => m.MaintenanceCategoriesComponent), canActivate: [permissionGuard('CATALOGS')] },
  { path: 'users', loadComponent: () => import('./pages/user-list/user-list.component').then(m => m.UserListComponent), canActivate: [adminGuard] },
  // Publica: enlace del correo de recuperacion de contrasena (sin sesion)
  { path: 'reset-password', loadComponent: () => import('./pages/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
  { path: '**', redirectTo: '/login' }
];