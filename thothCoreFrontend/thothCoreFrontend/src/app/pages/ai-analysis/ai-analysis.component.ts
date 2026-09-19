import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AIService } from '../../core/services/ai.service';
import { EquipmentService } from '../../core/services/equipment.service';
import { AIAnalysisResponse } from '../../core/models/ai.model';
import { Equipment } from '../../core/models/equipment.model';

@Component({
  selector: 'app-ai-analysis',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="ai-page">
      <div class="header">
        <button mat-icon-button routerLink="/equipment">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2><mat-icon>psychology</mat-icon> Analisis IA</h2>
      </div>
      @if (equipment()) {
        <mat-card class="equipment-info">
          <h3>{{ equipment()?.name }}</h3>
          <p>{{ equipment()?.category }} | {{ equipment()?.brand }} {{ equipment()?.model }} | Serial: {{ equipment()?.serialNumber }}</p>
        </mat-card>
      }
      <div class="analysis-grid">
        <mat-card class="analysis-card">
          <h3><mat-icon>build</mat-icon> Mantenimiento</h3>
          <p class="desc">Analisis del estado actual del equipo y recomendaciones de mantenimiento.</p>
          <button mat-raised-button color="primary" (click)="runMaintenance()" [disabled]="loadingMaint()">
            @if (loadingMaint()) { Analizando... } @else { Analizar }
          </button>
          @if (loadingMaint()) { <mat-spinner diameter="30"></mat-spinner> }
          @if (maintenance()) { <pre class="result">{{ maintenance()?.result }}</pre> }
        </mat-card>
        <mat-card class="analysis-card">
          <h3><mat-icon>warning</mat-icon> Prediccion de Fallos</h3>
          <p class="desc">Probabilidad de fallos y componentes en riesgo segun edad y uso.</p>
          <button mat-raised-button color="warn" (click)="runPrediction()" [disabled]="loadingPred()">
            @if (loadingPred()) { Prediciendo... } @else { Predecir }
          </button>
          @if (loadingPred()) { <mat-spinner diameter="30"></mat-spinner> }
          @if (prediction()) { <pre class="result">{{ prediction()?.result }}</pre> }
        </mat-card>
        <mat-card class="analysis-card">
          <h3><mat-icon>swap_horiz</mat-icon> Reemplazo</h3>
          <p class="desc">Recomendacion de reemplazo con analisis de depreciacion y ROI.</p>
          <button mat-raised-button color="accent" (click)="runReplacement()" [disabled]="loadingRepl()">
            @if (loadingRepl()) { Calculando... } @else { Recomendar }
          </button>
          @if (loadingRepl()) { <mat-spinner diameter="30"></mat-spinner> }
          @if (replacement()) { <pre class="result">{{ replacement()?.result }}</pre> }
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .ai-page { padding: 24px; }
    .header { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
    h2 { color: #1B4F72; display: flex; align-items: center; gap: 8px; margin: 0; }
    .equipment-info { margin-bottom: 20px; padding: 16px; }
    .equipment-info h3 { margin: 0; color: #2C3E50; }
    .equipment-info p { margin: 4px 0 0; color: #7F8C8D; }
    .analysis-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 20px; }
    .analysis-card { padding: 20px; }
    .analysis-card h3 { display: flex; align-items: center; gap: 8px; color: #2C3E50; margin-top: 0; }
    .desc { color: #7F8C8D; font-size: 13px; margin-bottom: 16px; }
    .result { background: #F8F9FA; padding: 16px; border-radius: 8px; white-space: pre-wrap; font-size: 13px; line-height: 1.6; margin-top: 16px; border-left: 4px solid #1B4F72; max-height: 400px; overflow-y: auto; }
    mat-spinner { margin: 12px 0; }
  `]
})
export class AIAnalysisComponent implements OnInit {
  equipment = signal<Equipment | null>(null);
  equipmentId = '';
  maintenance = signal<AIAnalysisResponse | null>(null);
  prediction = signal<AIAnalysisResponse | null>(null);
  replacement = signal<AIAnalysisResponse | null>(null);
  loadingMaint = signal(false);
  loadingPred = signal(false);
  loadingRepl = signal(false);

  constructor(
    private route: ActivatedRoute,
    private aiService: AIService,
    private equipmentService: EquipmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.equipmentId = this.route.snapshot.paramMap.get('id') || '';
    if (this.equipmentId) {
      this.equipmentService.getById(this.equipmentId).subscribe({
        next: (e) => { this.equipment.set(e); this.cdr.detectChanges(); }
      });
    }
  }

  runMaintenance() {
    this.loadingMaint.set(true);
    this.aiService.analyzeMaintenance(this.equipmentId).subscribe({
      next: (r) => { this.maintenance.set(r); this.loadingMaint.set(false); this.cdr.detectChanges(); },
      error: () => { this.loadingMaint.set(false); this.cdr.detectChanges(); }
    });
  }

  runPrediction() {
    this.loadingPred.set(true);
    this.aiService.predictFailure(this.equipmentId).subscribe({
      next: (r) => { this.prediction.set(r); this.loadingPred.set(false); this.cdr.detectChanges(); },
      error: () => { this.loadingPred.set(false); this.cdr.detectChanges(); }
    });
  }

  runReplacement() {
    this.loadingRepl.set(true);
    this.aiService.recommendReplacement(this.equipmentId).subscribe({
      next: (r) => { this.replacement.set(r); this.loadingRepl.set(false); this.cdr.detectChanges(); },
      error: () => { this.loadingRepl.set(false); this.cdr.detectChanges(); }
    });
  }
}