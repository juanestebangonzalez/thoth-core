import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { EquipmentService } from '../../core/services/equipment.service';
import { Equipment } from '../../core/models/equipment.model';

interface CalendarDay {
  date: Date;
  day: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  events: CalendarEvent[];
}
interface CalendarEvent {
  equipmentId: string;
  name: string;
  category: string;
  type: string;
  daysUntil: number;
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  template: `
    <div class="calendar-page">
      <div class="header">
        <h1><mat-icon>calendar_month</mat-icon> Calendario de Mantenimientos</h1>
      </div>

      <mat-card class="calendar-card">
        <div class="calendar-header">
          <button mat-icon-button (click)="prevMonth()"><mat-icon>chevron_left</mat-icon></button>
          <h2>{{ monthName() }} {{ currentYear() }}</h2>
          <button mat-icon-button (click)="nextMonth()"><mat-icon>chevron_right</mat-icon></button>
          <button mat-stroked-button (click)="goToday()" class="today-btn">Hoy</button>
        </div>

        <div class="weekdays">
          @for (day of weekDays; track day) {
            <div class="weekday">{{ day }}</div>
          }
        </div>

        <div class="days-grid">
          @for (day of calendarDays(); track day.date.getTime()) {
            <div class="day-cell" [class.other-month]="!day.isCurrentMonth" [class.today]="day.isToday"
                 [class.has-events]="day.events.length > 0">
              <span class="day-number">{{ day.day }}</span>
              @for (ev of day.events.slice(0, 3); track ev.equipmentId) {
                <a class="event" [routerLink]="['/equipment', ev.equipmentId]"
                   [class.overdue]="ev.daysUntil < 0" [class.today-ev]="ev.daysUntil === 0"
                   [class.soon]="ev.daysUntil > 0 && ev.daysUntil <= 7" [class.normal]="ev.daysUntil > 7"
                   [title]="ev.name + ' - ' + ev.category">
                  <mat-icon>{{ ev.daysUntil <= 0 ? 'warning' : 'build' }}</mat-icon>
                  <span>{{ ev.name }}</span>
                </a>
              }
              @if (day.events.length > 3) {
                <span class="more-events">+{{ day.events.length - 3 }} mas</span>
              }
            </div>
          }
        </div>
      </mat-card>

      <!-- Leyenda -->
      <div class="legend">
        <span class="legend-item"><span class="legend-dot overdue"></span> Vencido</span>
        <span class="legend-item"><span class="legend-dot today-ev"></span> Hoy</span>
        <span class="legend-item"><span class="legend-dot soon"></span> Proximos 7 dias</span>
        <span class="legend-item"><span class="legend-dot normal"></span> Programado</span>
      </div>

      <!-- Lista de proximos -->
      @if (upcomingEvents().length > 0) {
        <mat-card class="upcoming-card">
          <h3><mat-icon>event_upcoming</mat-icon> Proximos Mantenimientos</h3>
          @for (ev of upcomingEvents(); track ev.equipmentId) {
            <a class="upcoming-row" [routerLink]="['/equipment', ev.equipmentId]">
              <div class="upcoming-icon" [class.overdue]="ev.daysUntil < 0" [class.soon]="ev.daysUntil >= 0 && ev.daysUntil <= 7" [class.normal]="ev.daysUntil > 7">
                <mat-icon>{{ ev.daysUntil < 0 ? 'error' : 'build' }}</mat-icon>
              </div>
              <div class="upcoming-info">
                <span class="upcoming-name">{{ ev.name }}</span>
                <span class="upcoming-cat">{{ ev.category }}</span>
              </div>
              <span class="upcoming-date">{{ ev.dateStr }}</span>
              <span class="upcoming-days" [class.overdue]="ev.daysUntil < 0" [class.soon]="ev.daysUntil >= 0 && ev.daysUntil <= 7">
                {{ ev.daysUntil < 0 ? 'Hace ' + (-ev.daysUntil) + 'd' : (ev.daysUntil === 0 ? 'HOY' : 'En ' + ev.daysUntil + 'd') }}
              </span>
            </a>
          }
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .calendar-page { padding: 32px; max-width: 1200px; margin: 0 auto; }
    h1 { display: flex; align-items: center; gap: 12px; margin: 0 0 24px; font-size: 28px; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
    h1 mat-icon { font-size: 32px; width: 32px; height: 32px; }

    .calendar-card { padding: 24px; }
    .calendar-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
    .calendar-header h2 { flex: 1; color: #F1F5F9; font-size: 22px; margin: 0; text-transform: capitalize; }
    .today-btn { color: #60A5FA !important; border-color: #60A5FA !important; font-size: 12px; }

    .weekdays { display: grid; grid-template-columns: repeat(7, 1fr); margin-bottom: 4px; }
    .weekday { text-align: center; color: #94A3B8; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; padding: 8px 0; }

    .days-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px; }
    .day-cell { min-height: 100px; padding: 6px; border-radius: 8px; background: rgba(15,23,42,0.3); border: 1px solid rgba(148,163,184,0.06); transition: all 0.2s; }
    .day-cell:hover { background: rgba(59,130,246,0.05); border-color: rgba(59,130,246,0.2); }
    .day-cell.other-month { opacity: 0.3; }
    .day-cell.today { border-color: #3B82F6 !important; background: rgba(59,130,246,0.1); }
    .day-cell.has-events { border-color: rgba(245,158,11,0.3); }
    .day-number { display: block; color: #CBD5E1; font-size: 13px; font-weight: 600; margin-bottom: 4px; }
    .day-cell.today .day-number { color: #60A5FA; font-weight: 700; }

    .event { display: flex; align-items: center; gap: 4px; padding: 2px 6px; border-radius: 4px; margin-bottom: 2px; text-decoration: none; font-size: 10px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .event mat-icon { font-size: 12px; width: 12px; height: 12px; flex-shrink: 0; }
    .event span { overflow: hidden; text-overflow: ellipsis; }
    .event.overdue { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .event.today-ev { background: rgba(245,158,11,0.2); color: #FBBF24; }
    .event.soon { background: rgba(59,130,246,0.2); color: #93C5FD; }
    .event.normal { background: rgba(16,185,129,0.15); color: #6EE7B7; }
    .more-events { display: block; color: #64748B; font-size: 10px; text-align: center; }

    .legend { display: flex; gap: 20px; justify-content: center; margin: 16px 0 24px; flex-wrap: wrap; }
    .legend-item { display: flex; align-items: center; gap: 6px; color: #94A3B8; font-size: 13px; }
    .legend-dot { width: 12px; height: 12px; border-radius: 3px; }
    .legend-dot.overdue { background: rgba(239,68,68,0.5); }
    .legend-dot.today-ev { background: rgba(245,158,11,0.5); }
    .legend-dot.soon { background: rgba(59,130,246,0.5); }
    .legend-dot.normal { background: rgba(16,185,129,0.4); }

    .upcoming-card { padding: 24px; }
    .upcoming-card h3 { display: flex; align-items: center; gap: 8px; color: #E2E8F0; font-size: 16px; margin: 0 0 16px; }
    .upcoming-card h3 mat-icon { color: #60A5FA; }
    .upcoming-row { display: flex; align-items: center; gap: 14px; padding: 12px 16px; border-radius: 10px; background: rgba(30,41,59,0.5); margin-bottom: 6px; text-decoration: none; cursor: pointer; transition: all 0.2s; }
    .upcoming-row:hover { background: rgba(59,130,246,0.1); }
    .upcoming-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .upcoming-icon mat-icon { color: white; font-size: 18px; width: 18px; height: 18px; }
    .upcoming-icon.overdue { background: linear-gradient(135deg, #EF4444, #B91C1C); }
    .upcoming-icon.soon { background: linear-gradient(135deg, #F59E0B, #D97706); }
    .upcoming-icon.normal { background: linear-gradient(135deg, #10B981, #059669); }
    .upcoming-info { flex: 1; min-width: 0; }
    .upcoming-name { display: block; color: #F1F5F9; font-weight: 600; font-size: 14px; }
    .upcoming-cat { display: block; color: #64748B; font-size: 12px; }
    .upcoming-date { color: #94A3B8; font-size: 13px; flex-shrink: 0; }
    .upcoming-days { padding: 3px 10px; border-radius: 8px; font-size: 11px; font-weight: 700; flex-shrink: 0; background: rgba(16,185,129,0.15); color: #6EE7B7; }
    .upcoming-days.overdue { background: rgba(239,68,68,0.2); color: #FCA5A5; }
    .upcoming-days.soon { background: rgba(245,158,11,0.2); color: #FBBF24; }

    @media (max-width: 768px) {
      .calendar-page { padding: 16px; }
      .day-cell { min-height: 60px; padding: 4px; }
      .event span { display: none; }
      .event { justify-content: center; padding: 2px; }
      .upcoming-date { display: none; }
    }
  `]
})
export class CalendarComponent implements OnInit {
  weekDays = ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab', 'Dom'];
  currentMonth = signal(new Date().getMonth());
  currentYear = signal(new Date().getFullYear());
  allEquipments = signal<Equipment[]>([]);

  monthName = computed(() => {
    const months = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
    return months[this.currentMonth()];
  });

  calendarDays = computed<CalendarDay[]>(() => {
    const year = this.currentYear();
    const month = this.currentMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date(); today.setHours(0,0,0,0);

    let startDay = firstDay.getDay() - 1;
    if (startDay < 0) startDay = 6;

    const days: CalendarDay[] = [];

    for (let i = startDay - 1; i >= 0; i--) {
      const d = new Date(year, month, -i);
      days.push({ date: d, day: d.getDate(), isCurrentMonth: false, isToday: false, events: [] });
    }

    for (let i = 1; i <= lastDay.getDate(); i++) {
      const d = new Date(year, month, i);
      const isToday = d.getTime() === today.getTime();
      const events = this.getEventsForDate(d);
      days.push({ date: d, day: i, isCurrentMonth: true, isToday, events });
    }

    const remaining = 42 - days.length;
    for (let i = 1; i <= remaining; i++) {
      const d = new Date(year, month + 1, i);
      days.push({ date: d, day: i, isCurrentMonth: false, isToday: false, events: [] });
    }

    return days;
  });

  upcomingEvents = computed(() => {
    const today = new Date(); today.setHours(0,0,0,0);
    return this.allEquipments()
      .filter(e => e.nextMaintenanceDate)
      .map(e => {
        const next = new Date(e.nextMaintenanceDate!); next.setHours(0,0,0,0);
        const diff = Math.floor((next.getTime() - today.getTime()) / (1000*60*60*24));
        return { equipmentId: e.equipmentId, name: e.name || '', category: e.category || '', daysUntil: diff, dateStr: e.nextMaintenanceDate || '' };
      })
      .filter(e => e.daysUntil <= 30)
      .sort((a, b) => a.daysUntil - b.daysUntil);
  });

  constructor(private equipmentService: EquipmentService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.equipmentService.list(0, 200).subscribe({
      next: (res) => { this.allEquipments.set(res.content || []); this.cdr.detectChanges(); }
    });
  }

  getEventsForDate(date: Date): CalendarEvent[] {
    const today = new Date(); today.setHours(0,0,0,0);
    return this.allEquipments()
      .filter(e => {
        if (!e.nextMaintenanceDate) return false;
        const nd = new Date(e.nextMaintenanceDate);
        return nd.getDate() === date.getDate() && nd.getMonth() === date.getMonth() && nd.getFullYear() === date.getFullYear();
      })
      .map(e => {
        const nd = new Date(e.nextMaintenanceDate!); nd.setHours(0,0,0,0);
        return { equipmentId: e.equipmentId, name: e.name || '', category: e.category || '', type: 'maintenance', daysUntil: Math.floor((nd.getTime() - today.getTime()) / (1000*60*60*24)) };
      });
  }

  prevMonth() {
    if (this.currentMonth() === 0) { this.currentMonth.set(11); this.currentYear.set(this.currentYear() - 1); }
    else { this.currentMonth.set(this.currentMonth() - 1); }
  }

  nextMonth() {
    if (this.currentMonth() === 11) { this.currentMonth.set(0); this.currentYear.set(this.currentYear() + 1); }
    else { this.currentMonth.set(this.currentMonth() + 1); }
  }

  goToday() { this.currentMonth.set(new Date().getMonth()); this.currentYear.set(new Date().getFullYear()); }
}