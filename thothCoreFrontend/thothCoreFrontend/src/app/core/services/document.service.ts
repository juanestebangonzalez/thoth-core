import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EquipmentDocument {
  id: string;
  equipmentId: string;
  fileName: string;
  originalName: string;
  contentType: string;
  fileSize: number;
  documentType: string;
  description: string;
  uploadedBy: string;
  uploadedAt: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private apiUrl = `${environment.apiUrl}/documents`;
  constructor(private http: HttpClient) {}

  listByEquipment(equipmentId: string): Observable<EquipmentDocument[]> {
    return this.http.get<EquipmentDocument[]>(`${this.apiUrl}/equipment/${equipmentId}`);
  }

  upload(equipmentId: string, file: File, documentType: string, description: string): Observable<EquipmentDocument> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('equipmentId', equipmentId);
    formData.append('documentType', documentType);
    formData.append('description', description);
    return this.http.post<EquipmentDocument>(this.apiUrl + '/upload', formData);
  }

  getDownloadUrl(docId: string): string {
    return `${environment.apiUrl}/documents/${docId}/download`;
  }

  delete(docId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${docId}`);
  }
}