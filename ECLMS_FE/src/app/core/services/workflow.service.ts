import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/workflows`;

  getWorkflows(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getWorkflowById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createWorkflow(workflow: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, workflow);
  }

  startWorkflow(workflowId: string, contractId: string): Observable<any> {
    const params = new HttpParams()
      .set('workflowId', workflowId)
      .set('contractId', contractId);
    return this.http.post<any>(`${this.apiUrl}/start`, null, { params });
  }

  recordAction(instanceId: string, action: string, comments: string): Observable<any> {
    const body = { action, comments };
    return this.http.post<any>(`${this.apiUrl}/instances/${instanceId}/action`, body);
  }

  getApprovalHistory(instanceId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/instances/${instanceId}/history`);
  }

  getWorkflowInstances(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/instances/list`);
  }
}
