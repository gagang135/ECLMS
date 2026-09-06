import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ContractService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/contracts`;

  getContracts(page = 0, size = 10, search = '', status = '', departmentId = '', vendorId = ''): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    if (search) params = params.set('search', search);
    if (status) params = params.set('status', status);
    if (departmentId) params = params.set('departmentId', departmentId);
    if (vendorId) params = params.set('vendorId', vendorId);

    return this.http.get<any>(this.apiUrl, { params });
  }

  getContractById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createContract(contract: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, contract);
  }

  updateContract(id: string, contract: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, contract);
  }

  deleteContract(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  renewContract(id: string, newEndDate: string): Observable<any> {
    const params = new HttpParams().set('newEndDate', newEndDate);
    return this.http.post<any>(`${this.apiUrl}/${id}/renew`, null, { params });
  }
}
