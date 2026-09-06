import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Template {
  id?: string;
  name: string;
  description: string;
  content: string;
  variables: string;
  versionString: string;
  status?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: any;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${environment.apiUrl}/contract-templates`;

  /**
   * Get All Templates
   */
  getTemplates(): Observable<ApiResponse<Template[]>> {

    return this.http.get<ApiResponse<Template[]>>(
      this.apiUrl
    );

  }

  /**
   * Get Template By Id
   */
  getTemplateById(id: string): Observable<ApiResponse<Template>> {

    return this.http.get<ApiResponse<Template>>(
      `${this.apiUrl}/${id}`
    );

  }

  /**
   * Create Template
   */
  createTemplate(template: Template): Observable<ApiResponse<Template>> {

    return this.http.post<ApiResponse<Template>>(
      this.apiUrl,
      template
    );

  }

  /**
   * Update Template
   */
  updateTemplate(
    id: string,
    template: Template
  ): Observable<ApiResponse<Template>> {

    return this.http.put<ApiResponse<Template>>(
      `${this.apiUrl}/${id}`,
      template
    );

  }

  /**
   * Delete Template
   */
  deleteTemplate(id: string): Observable<ApiResponse<void>> {

    return this.http.delete<ApiResponse<void>>(
      `${this.apiUrl}/${id}`
    );

  }

  /**
   * Publish Template
   */
  publishTemplate(id: string): Observable<ApiResponse<Template>> {

    return this.http.post<ApiResponse<Template>>(
      `${this.apiUrl}/${id}/publish`,
      {}
    );

  }

  /**
   * Archive Template
   */
  archiveTemplate(id: string): Observable<ApiResponse<Template>> {

    return this.http.post<ApiResponse<Template>>(
      `${this.apiUrl}/${id}/archive`,
      {}
    );

  }

}