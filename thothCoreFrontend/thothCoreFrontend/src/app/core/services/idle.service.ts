import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class IdleService {
  private timeoutMs = 30 * 60 * 1000; // 30 minutos
  private warningMs = 5 * 60 * 1000; // Aviso 5 min antes
  private timeoutId: any;
  private warningId: any;
  private events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
  private running = false;
  private readonly boundReset = () => this.reset();

  constructor(
    private auth: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private ngZone: NgZone
  ) {}

  start() {
    if (this.running) return;
    this.running = true;
    this.events.forEach(event =>
      document.addEventListener(event, this.boundReset, { passive: true } as AddEventListenerOptions)
    );
    this.reset();
  }

  stop() {
    this.running = false;
    this.events.forEach(event =>
      document.removeEventListener(event, this.boundReset)
    );
    clearTimeout(this.timeoutId);
    clearTimeout(this.warningId);
  }

  private reset() {
    if (!this.running) return;
    clearTimeout(this.timeoutId);
    clearTimeout(this.warningId);

    this.ngZone.runOutsideAngular(() => {
      this.warningId = setTimeout(() => {
        this.ngZone.run(() => {
          this.snackBar.open(
            '⚠️ Tu sesión se cerrará en 5 minutos por inactividad',
            'Entendido',
            { duration: 60000, panelClass: ['warning-snackbar'] }
          );
        });
      }, this.timeoutMs - this.warningMs);

      this.timeoutId = setTimeout(() => {
        this.ngZone.run(() => {
          this.auth.logout();
          this.router.navigate(['/login']);
          this.snackBar.open(
            'Sesión cerrada por inactividad (30 min)',
            'OK',
            { duration: 5000 }
          );
          this.stop();
        });
      }, this.timeoutMs);
    });
  }
}
