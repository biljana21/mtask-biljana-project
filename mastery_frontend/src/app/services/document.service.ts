import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document } from '../models/document.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private baseUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {}

  upload(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<Document>(`${this.baseUrl}/upload`, formData);
  }

  getAll(page: number = 0, size: number = 10): Observable<Page<Document>> {
    return this.http.get<Page<Document>>(
      `${this.baseUrl}?page=${page}&size=${size}`
    );
  }

  getFile(id: number) {
    return this.http.get(`${this.baseUrl}/${id}/file/preview`, {
      responseType: 'blob'
    });
  }

  downloadFile(id: number) {
    return this.http.get(`${this.baseUrl}/${id}/file/download`, {
      responseType: 'blob'
    });
  }

  update(id: number, payload: any): Observable<Document> {
    return this.http.put<Document>(
      `${this.baseUrl}/${id}`,
      payload
    );
  }

  // delete(id: number) {
  //   return this.http.delete(`${this.baseUrl}/${id}`);
  // }

}
