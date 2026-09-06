import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VendorService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/vendors`;

  getVendors(page = 0, size = 100, search = ''): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (search) params = params.set('search', search);

    return this.http.get<any>(this.apiUrl, { params });
  }

  getVendorById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createVendor(vendor: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, vendor);
  }

  updateVendor(id: string, vendor: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, vendor);
  }

  deleteVendor(id: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
