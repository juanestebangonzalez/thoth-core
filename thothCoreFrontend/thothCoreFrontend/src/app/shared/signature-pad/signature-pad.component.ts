import { Component, ElementRef, ViewChild, AfterViewInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-signature-pad',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <div class="signature-container">
      <p class="signature-label">{{ label }}</p>
      <canvas #signatureCanvas
        (mousedown)="startDrawing($event)"
        (mousemove)="draw($event)"
        (mouseup)="stopDrawing()"
        (mouseleave)="stopDrawing()"
        (touchstart)="onTouchStart($event)"
        (touchmove)="onTouchDraw($event)"
        (touchend)="stopDrawing()"
        class="signature-canvas">
      </canvas>
      <div class="signature-actions">
        <button mat-stroked-button (click)="clear()" type="button">
          <mat-icon>refresh</mat-icon> Limpiar
        </button>
        <span class="signature-hint">{{ hasSigned ? 'Firma registrada' : 'Firme con el dedo o mouse' }}</span>
      </div>
    </div>
  `,
  styles: [`
    .signature-container { margin: 16px 0; }
    .signature-label { color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; margin: 0 0 8px; }
    .signature-canvas {
      width: 100%; height: 150px; border-radius: 10px;
      border: 2px dashed rgba(148,163,184,0.3);
      background: rgba(15,23,42,0.5); cursor: crosshair;
      touch-action: none;
    }
    .signature-canvas:active { border-color: #3B82F6; }
    .signature-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
    .signature-hint { color: #64748B; font-size: 12px; font-style: italic; }
  `]
})
export class SignaturePadComponent implements AfterViewInit {
  @ViewChild('signatureCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;
  @Output() signatureChange = new EventEmitter<string | null>();
  @Input() label = 'Firma del Responsable';

  private ctx!: CanvasRenderingContext2D;
  private drawing = false;
  hasSigned = false;

  ngAfterViewInit() {
    const canvas = this.canvasRef.nativeElement;
    canvas.width = canvas.offsetWidth;
    canvas.height = 150;
    this.ctx = canvas.getContext('2d')!;
    this.ctx.strokeStyle = '#60A5FA';
    this.ctx.lineWidth = 2.5;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
  }

  startDrawing(event: MouseEvent) {
    this.drawing = true;
    this.hasSigned = true;
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    this.ctx.beginPath();
    this.ctx.moveTo(event.clientX - rect.left, event.clientY - rect.top);
  }

  draw(event: MouseEvent) {
    if (!this.drawing) return;
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    this.ctx.lineTo(event.clientX - rect.left, event.clientY - rect.top);
    this.ctx.stroke();
    this.emitSignature();
  }

  onTouchStart(event: TouchEvent) {
    event.preventDefault();
    this.drawing = true;
    this.hasSigned = true;
    const touch = event.touches[0];
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    this.ctx.beginPath();
    this.ctx.moveTo(touch.clientX - rect.left, touch.clientY - rect.top);
  }

  onTouchDraw(event: TouchEvent) {
    event.preventDefault();
    if (!this.drawing) return;
    const touch = event.touches[0];
    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    this.ctx.lineTo(touch.clientX - rect.left, touch.clientY - rect.top);
    this.ctx.stroke();
    this.emitSignature();
  }

  stopDrawing() {
    this.drawing = false;
  }

  clear() {
    const canvas = this.canvasRef.nativeElement;
    this.ctx.clearRect(0, 0, canvas.width, canvas.height);
    this.hasSigned = false;
    this.signatureChange.emit(null);
  }

  private emitSignature() {
    const dataUrl = this.canvasRef.nativeElement.toDataURL('image/png');
    this.signatureChange.emit(dataUrl);
  }

  getSignatureBase64(): string | null {
    if (!this.hasSigned) return null;
    return this.canvasRef.nativeElement.toDataURL('image/png');
  }
}