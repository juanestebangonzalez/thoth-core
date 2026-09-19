import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Technician {
  id: string;
  username: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class TechnicianService {
  private apiUrl = `${environment.apiUrl}/technicians`;

  constructor(private http: HttpClient) {}

  list(): Observable<Technician[]> {
    return this.http.get<Technician[]>(this.apiUrl);
  }
}
