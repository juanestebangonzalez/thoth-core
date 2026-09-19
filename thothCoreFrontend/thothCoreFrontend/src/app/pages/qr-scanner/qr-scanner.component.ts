import { Component, signal, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-qr-scanner',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  template: `
    <div class="scanner-page">
      <div class="header">
        <button mat-icon-button routerLink="/dashboard">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>qr_code_scanner</mat-icon> Escanear QR</h2>
      </div>

      @if (!scanning()) {
        <mat-card class="start-card">
          <div class="start-content">
            <mat-icon class="big-icon">qr_code_scanner</mat-icon>
            <h3>Escanear Codigo QR del Equipo</h3>
            <p>Apunta la camara al codigo QR pegado en el equipo para acceder a su informacion.</p>
            <button mat-raised-button color="primary" (click)="startScanning()">
              <mat-icon>camera_alt</mat-icon> Abrir Camara
            </button>
          </div>
        </mat-card>
      } @else {
        <mat-card class="camera-card">
          <video #videoElement autoplay playsinline class="video-feed"></video>
          <canvas #canvasElement class="hidden-canvas"></canvas>
          <div class="scan-overlay">
            <div class="scan-frame"></div>
          </div>
          <div class="camera-controls">
            <button mat-raised-button color="warn" (click)="stopScanning()">
              <mat-icon>close</mat-icon> Cerrar Camara
            </button>
          </div>
        </mat-card>
      }

      @if (lastResult()) {
        <mat-card class="result-card">
          <mat-icon class="result-icon">check_circle</mat-icon>
          <div>
            <p class="result-label">Equipo encontrado:</p>
            <p class="result-value">{{ lastResult() }}</p>
          </div>
          <button mat-raised-button color="primary" (click)="navigateToEquipment()">
            <mat-icon>open_in_new</mat-icon> Ver Detalle
          </button>
        </mat-card>
      }

      <mat-card class="manual-card">
        <h3><mat-icon>search</mat-icon> Buscar Manualmente</h3>
        <p>Tambie puedes ir directamente a la lista de equipos.</p>
        <button mat-stroked-button routerLink="/equipment">
          <mat-icon>list</mat-icon> Ver Todos los Equipos
        </button>
      </mat-card>
    </div>
  `,
  styles: [`
    .scanner-page { padding: 24px; max-width: 600px; margin: 0 auto; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 { display: flex; align-items: center; gap: 8px; margin: 0; background: linear-gradient(135deg, #60A5FA, #A78BFA); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; font-size: 24px; }

    .start-card { padding: 40px 24px; text-align: center; }
    .start-content h3 { color: #F1F5F9; margin: 16px 0 8px; }
    .start-content p { color: #94A3B8; margin-bottom: 24px; font-size: 14px; }
    .big-icon { font-size: 72px; width: 72px; height: 72px; color: #3B82F6; }

    .camera-card { padding: 0; overflow: hidden; position: relative; border-radius: 16px; }
    .video-feed { width: 100%; display: block; border-radius: 16px; }
    .hidden-canvas { display: none; }
    .scan-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; pointer-events: none; }
    .scan-frame { width: 220px; height: 220px; border: 3px solid #3B82F6; border-radius: 16px; box-shadow: 0 0 0 9999px rgba(0,0,0,0.4); animation: scan-pulse 2s infinite; }
    @keyframes scan-pulse { 0%,100% { border-color: #3B82F6; } 50% { border-color: #60A5FA; } }
    .camera-controls { position: absolute; bottom: 16px; left: 0; right: 0; display: flex; justify-content: center; }

    .result-card { display: flex; align-items: center; gap: 16px; padding: 16px 20px; margin-top: 16px; }
    .result-icon { color: #10B981; font-size: 36px; width: 36px; height: 36px; }
    .result-label { color: #94A3B8; font-size: 12px; margin: 0; text-transform: uppercase; }
    .result-value { color: #F1F5F9; font-weight: 600; margin: 4px 0 0; }

    .manual-card { padding: 24px; margin-top: 16px; text-align: center; }
    .manual-card h3 { display: flex; align-items: center; justify-content: center; gap: 8px; color: #E2E8F0; margin-top: 0; }
    .manual-card p { color: #94A3B8; font-size: 14px; margin-bottom: 16px; }
  `]
})
export class QrScannerComponent implements OnDestroy {
  scanning = signal(false);
  lastResult = signal('');
  private stream: MediaStream | null = null;
  private scanInterval: any = null;
  private extractedId = '';

  constructor(private router: Router, private snackBar: MatSnackBar, private cdr: ChangeDetectorRef) {}

  async startScanning() {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 640 }, height: { ideal: 480 } }
      });
      this.scanning.set(true);
      this.cdr.detectChanges();

      setTimeout(() => {
        const video = document.querySelector('video') as HTMLVideoElement;
        const canvas = document.querySelector('canvas') as HTMLCanvasElement;
        if (video && this.stream) {
          video.srcObject = this.stream;
          this.startDecoding(video, canvas);
        }
      }, 100);
    } catch (err) {
      this.snackBar.open('No se pudo acceder a la camara. Verifica permisos.', 'OK', { duration: 5000 });
    }
  }

  private startDecoding(video: HTMLVideoElement, canvas: HTMLCanvasElement) {
    const ctx = canvas.getContext('2d');
    this.scanInterval = setInterval(() => {
      if (video.readyState === video.HAVE_ENOUGH_DATA && ctx) {
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Usar BarcodeDetector API (Chrome 83+)
        if ('BarcodeDetector' in window) {
          const detector = new (window as any).BarcodeDetector({ formats: ['qr_code'] });
          detector.detect(canvas).then((barcodes: any[]) => {
            if (barcodes.length > 0) {
              this.handleResult(barcodes[0].rawValue);
            }
          }).catch(() => {});
        }
      }
    }, 500);
  }

  private handleResult(url: string) {
    // Extraer el equipmentId de la URL: .../equipment/{uuid}
    const match = url.match(/equipment\/([0-9a-fA-F-]{36})/);
    if (match) {
      this.extractedId = match[1];
      this.lastResult.set(url);
      this.stopScanning();
      this.snackBar.open('QR detectado!', 'OK', { duration: 2000 });
      this.cdr.detectChanges();
    }
  }

  navigateToEquipment() {
    if (this.extractedId) {
      this.router.navigate(['/equipment', this.extractedId]);
    }
  }

  stopScanning() {
    if (this.scanInterval) {
      clearInterval(this.scanInterval);
      this.scanInterval = null;
    }
    if (this.stream) {
      this.stream.getTracks().forEach(t => t.stop());
      this.stream = null;
    }
    this.scanning.set(false);
    this.cdr.detectChanges();
  }

  ngOnDestroy() { this.stopScanning(); }
}