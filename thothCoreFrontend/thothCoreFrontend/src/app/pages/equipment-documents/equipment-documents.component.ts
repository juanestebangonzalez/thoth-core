import { Component, Input, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { DocumentService, EquipmentDocument } from '../../core/services/document.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-equipment-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatSelectModule, MatFormFieldModule, MatInputModule, MatSnackBarModule, MatProgressBarModule],
  template: `
    <div class="docs-section">
      <div class="docs-header">
        <h3><mat-icon>folder</mat-icon> Documentos ({{ documents().length }})</h3>
        @if (auth.hasPermission('DOCUMENTS', 'CREATE')) {
          <button mat-stroked-button color="primary" (click)="toggleUpload()">
            <mat-icon>{{ showUpload() ? 'close' : 'upload_file' }}</mat-icon>
            {{ showUpload() ? 'Cerrar' : 'Subir Documento' }}
          </button>
        }
      </div>

      @if (showUpload()) {
        <div class="upload-form">
          <div class="upload-row">
            <mat-form-field appearance="outline" class="type-field">
              <mat-label>Tipo</mat-label>
              <mat-select [(ngModel)]="docType">
                <mat-option value="FACTURA">Factura</mat-option>
                <mat-option value="CONTRATO">Contrato</mat-option>
                <mat-option value="GARANTIA">Garantia</mat-option>
                <mat-option value="ACTA">Acta</mat-option>
                <mat-option value="OTRO">Otro</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline" class="desc-field">
              <mat-label>Descripcion (opcional)</mat-label>
              <input matInput [(ngModel)]="docDescription" placeholder="Ej: Factura compra 2026">
            </mat-form-field>
          </div>
          <div class="file-input-row">
            <input type="file" #fileInput (change)="onFileSelected($event)"
                   accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx" class="file-input">
            <button mat-raised-button color="primary" (click)="upload()" [disabled]="!selectedFile || uploading()">
              <mat-icon>cloud_upload</mat-icon>
              {{ uploading() ? 'Subiendo...' : 'Subir' }}
            </button>
          </div>
          @if (uploading()) {
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
          }
          <p class="upload-hint">PDF, imagenes, Word o Excel. Max 10MB. Las imagenes se comprimen automaticamente.</p>
        </div>
      }

      @if (documents().length === 0) {
        <p class="empty">Sin documentos adjuntos</p>
      } @else {
        <div class="docs-list">
          @for (doc of documents(); track doc.id) {
            <div class="doc-row">
              <div class="doc-icon" [class]="getTypeClass(doc.documentType)">
                <mat-icon>{{ getTypeIcon(doc.documentType) }}</mat-icon>
              </div>
              <div class="doc-info">
                <span class="doc-name">{{ doc.originalName }}</span>
                <span class="doc-meta">
                  <span class="doc-type-badge" [class]="getTypeClass(doc.documentType)">{{ doc.documentType }}</span>
                  {{ formatSize(doc.fileSize) }} |
                  {{ doc.uploadedAt | date:'dd/MM/yyyy HH:mm' }}
                </span>
                @if (doc.description) {
                  <span class="doc-desc">{{ doc.description }}</span>
                }
              </div>
              <div class="doc-actions">
                <a mat-icon-button [href]="getDownloadUrl(doc.id)" target="_blank" title="Descargar">
                  <mat-icon style="color:#10B981;">download</mat-icon>
                </a>
                @if (auth.hasPermission('DOCUMENTS', 'DELETE')) {
                  <button mat-icon-button (click)="deleteDoc(doc)" title="Eliminar">
                    <mat-icon style="color:#EF4444;">delete</mat-icon>
                  </button>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .docs-section { margin-top: 20px; }
    .docs-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .docs-header h3 { display: flex; align-items: center; gap: 8px; color: #94A3B8; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; margin: 0; }
    .docs-header h3 mat-icon { color: #60A5FA; font-size: 18px; width: 18px; height: 18px; }

    .upload-form { padding: 16px; background: rgba(15,23,42,0.5); border-radius: 12px; border: 1px dashed rgba(59,130,246,0.3); margin-bottom: 16px; }
    .upload-row { display: flex; gap: 12px; flex-wrap: wrap; }
    .type-field { width: 180px; }
    .desc-field { flex: 1; min-width: 200px; }
    .file-input-row { display: flex; gap: 12px; align-items: center; margin-top: 8px; }
    .file-input { flex: 1; color: #CBD5E1; font-size: 13px; }
    .file-input::file-selector-button { padding: 8px 16px; border-radius: 8px; border: 1px solid rgba(59,130,246,0.3); background: rgba(59,130,246,0.1); color: #93C5FD; cursor: pointer; margin-right: 12px; }
    .upload-hint { color: #64748B; font-size: 11px; margin: 8px 0 0; }

    .empty { text-align: center; color: #64748B; font-style: italic; padding: 20px; font-size: 14px; }

    .docs-list { display: flex; flex-direction: column; gap: 8px; }
    .doc-row { display: flex; align-items: center; gap: 14px; padding: 12px 16px; border-radius: 10px; background: rgba(30,41,59,0.5); transition: all 0.2s; }
    .doc-row:hover { background: rgba(59,130,246,0.08); }
    .doc-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .doc-icon mat-icon { color: white; font-size: 20px; width: 20px; height: 20px; }
    .doc-icon.factura { background: linear-gradient(135deg, #10B981, #059669); }
    .doc-icon.contrato { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
    .doc-icon.garantia { background: linear-gradient(135deg, #3B82F6, #1E40AF); }
    .doc-icon.acta { background: linear-gradient(135deg, #F59E0B, #D97706); }
    .doc-icon.otro { background: linear-gradient(135deg, #64748B, #475569); }
    .doc-info { flex: 1; min-width: 0; }
    .doc-name { display: block; color: #F1F5F9; font-weight: 600; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .doc-meta { display: flex; align-items: center; gap: 8px; color: #64748B; font-size: 12px; margin-top: 3px; flex-wrap: wrap; }
    .doc-type-badge { padding: 2px 8px; border-radius: 6px; font-size: 10px; font-weight: 700; text-transform: uppercase; }
    .doc-type-badge.factura { background: rgba(16,185,129,0.15); color: #6EE7B7; }
    .doc-type-badge.contrato { background: rgba(139,92,246,0.15); color: #C4B5FD; }
    .doc-type-badge.garantia { background: rgba(59,130,246,0.15); color: #93C5FD; }
    .doc-type-badge.acta { background: rgba(245,158,11,0.15); color: #FBBF24; }
    .doc-type-badge.otro { background: rgba(100,116,139,0.15); color: #CBD5E1; }
    .doc-desc { display: block; color: #94A3B8; font-size: 12px; margin-top: 2px; font-style: italic; }
    .doc-actions { display: flex; gap: 4px; flex-shrink: 0; }

    @media (max-width: 768px) {
      .upload-row { flex-direction: column; }
      .type-field { width: 100%; }
    }
  `]
})
export class EquipmentDocumentsComponent implements OnInit {
  @Input() equipmentId = '';

  documents = signal<EquipmentDocument[]>([]);
  uploading = signal(false);
  showUpload = signal(false);
  selectedFile: File | null = null;
  docType = 'FACTURA';
  docDescription = '';

  constructor(
    public auth: AuthService,
    private documentService: DocumentService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadDocuments(); }

  toggleUpload() {
    this.showUpload.set(!this.showUpload());
    this.cdr.detectChanges();
  }

  loadDocuments() {
    if (!this.equipmentId) return;
    this.documentService.listByEquipment(this.equipmentId).subscribe({
      next: (docs) => { this.documents.set(docs); this.cdr.detectChanges(); }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] || null;
  }

  upload() {
    if (!this.selectedFile) return;
    this.uploading.set(true);
    this.documentService.upload(this.equipmentId, this.selectedFile, this.docType, this.docDescription).subscribe({
      next: () => {
        this.uploading.set(false);
        this.snackBar.open('Documento subido exitosamente', 'OK', { duration: 3000 });
        this.selectedFile = null;
        this.docDescription = '';
        this.showUpload = signal(false);
        this.loadDocuments();
      },
      error: (err) => {
        this.uploading.set(false);
        this.snackBar.open(err.error?.message || 'Error al subir', 'OK', { duration: 5000 });
        this.cdr.detectChanges();
      }
    });
  }

  deleteDoc(doc: EquipmentDocument) {
    if (!confirm('Eliminar documento: ' + doc.originalName + '?')) return;
    this.documentService.delete(doc.id).subscribe({
      next: () => { this.snackBar.open('Documento eliminado', 'OK', { duration: 3000 }); this.loadDocuments(); }
    });
  }

  getDownloadUrl(docId: string): string {
    return this.documentService.getDownloadUrl(docId);
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = { FACTURA: 'receipt', CONTRATO: 'description', GARANTIA: 'verified', ACTA: 'assignment', OTRO: 'attach_file' };
    return icons[type] || 'attach_file';
  }

  getTypeClass(type: string): string { return type?.toLowerCase() || 'otro'; }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }
}