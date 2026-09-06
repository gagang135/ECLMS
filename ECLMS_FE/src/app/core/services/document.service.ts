import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/documents`;

  uploadDocument(file: File, contractId?: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    if (contractId && contractId !== 'general') {
      formData.append('contractId', contractId);
    }
    return this.http.post<any>(`${this.apiUrl}/upload`, formData);
  }

  downloadDocument(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' });
  }

  getPreviewUrl(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/preview`);
  }

  getDocumentMetadata(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  deleteDocument(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getDocumentsByContract(contractId: string): Observable<any> {
    if (contractId === 'general') {
      return this.http.get<any>(this.apiUrl);
    }
    return this.http.get<any>(`${this.apiUrl}/contract/${contractId}`);
  }
}
