import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AIAnalysisResponse } from '../models/ai.model';

@Injectable({ providedIn: 'root' })
export class AIService {
  private apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  analyzeMaintenance(equipmentId: string): Observable<AIAnalysisResponse> {
    return this.http.get<AIAnalysisResponse>(`${this.apiUrl}/maintenance/${equipmentId}`);
  }

  predictFailure(equipmentId: string): Observable<AIAnalysisResponse> {
    return this.http.get<AIAnalysisResponse>(`${this.apiUrl}/predict-failure/${equipmentId}`);
  }

  recommendReplacement(equipmentId: string): Observable<AIAnalysisResponse> {
    return this.http.get<AIAnalysisResponse>(`${this.apiUrl}/recommend-replacement/${equipmentId}`);
  }
}