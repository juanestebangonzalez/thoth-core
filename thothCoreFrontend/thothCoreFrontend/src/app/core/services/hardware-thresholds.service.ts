import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface HardwareThresholds {
  /** Salud del disco: ADVERTENCIA si es menor a este porcentaje. */
  diskHealthWarning: number;
  /** Salud del disco: CRITICO si es menor a este porcentaje. */
  diskHealthCritical: number;
  /** Temperatura: ADVERTENCIA si es mayor a estos grados. */
  diskTempWarning: number;
  /** Temperatura: CRITICO desde estos grados. */
  diskTempCritical: number;
}

export const UMBRALES_POR_DEFECTO: HardwareThresholds = {
  diskHealthWarning: 60,
  diskHealthCritical: 30,
  diskTempWarning: 50,
  diskTempCritical: 60
};

/**
 * Lee los umbrales de salud y temperatura de disco configurados en el backend
 * (GET /settings/hardware-thresholds). Si la peticion falla se usan los
 * valores por defecto 60/30/50/60. La respuesta se cachea en memoria.
 */
@Injectable({ providedIn: 'root' })
export class HardwareThresholdsService {
  private apiUrl = `${environment.apiUrl}/settings/hardware-thresholds`;
  readonly thresholds = signal<HardwareThresholds>({ ...UMBRALES_POR_DEFECTO });
  private cache$?: Observable<HardwareThresholds>;

  constructor(private http: HttpClient) {}

  load(): Observable<HardwareThresholds> {
    if (!this.cache$) {
      this.cache$ = this.http.get<Partial<HardwareThresholds>>(this.apiUrl).pipe(
        map(r => ({
          diskHealthWarning: this.num(r?.diskHealthWarning, UMBRALES_POR_DEFECTO.diskHealthWarning),
          diskHealthCritical: this.num(r?.diskHealthCritical, UMBRALES_POR_DEFECTO.diskHealthCritical),
          diskTempWarning: this.num(r?.diskTempWarning, UMBRALES_POR_DEFECTO.diskTempWarning),
          diskTempCritical: this.num(r?.diskTempCritical, UMBRALES_POR_DEFECTO.diskTempCritical)
        })),
        catchError(() => {
          this.cache$ = undefined; // reintentar la proxima vez
          return of({ ...UMBRALES_POR_DEFECTO });
        }),
        tap(t => this.thresholds.set(t)),
        shareReplay(1)
      );
    }
    return this.cache$;
  }

  /** OK | ADVERTENCIA | CRITICO segun la salud del disco (porcentaje). */
  healthStatus(percent: number | null | undefined): string {
    if (percent === null || percent === undefined || isNaN(Number(percent))) return '';
    const t = this.thresholds();
    if (percent < t.diskHealthCritical) return 'CRITICO';
    if (percent < t.diskHealthWarning) return 'ADVERTENCIA';
    return 'OK';
  }

  /** OK | ADVERTENCIA | CRITICO segun la temperatura del disco (C). */
  tempStatus(celsius: number | null | undefined): string {
    if (celsius === null || celsius === undefined || isNaN(Number(celsius))) return '';
    const t = this.thresholds();
    if (celsius >= t.diskTempCritical) return 'CRITICO';
    if (celsius > t.diskTempWarning) return 'ADVERTENCIA';
    return 'OK';
  }

  private num(v: any, def: number): number {
    const n = Number(v);
    return v === null || v === undefined || isNaN(n) ? def : n;
  }
}
