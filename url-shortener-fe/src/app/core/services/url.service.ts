import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Url, ShortenRequest, EditUrlRequest } from '../models/url.model';

@Injectable({
  providedIn: 'root',
})
export class UrlService {
  private readonly apiUrl = 'http://localhost:8080/api/urls';

  constructor(private http: HttpClient) {}

  shorten(request: ShortenRequest): Observable<Url> {
    return this.http.post<Url>(`${this.apiUrl}/shorten`, request);
  }

  getUserUrls(): Observable<Url[]> {
    return this.http.get<Url[]>(this.apiUrl);
  }

  editUrl(shortCode: string, request: EditUrlRequest): Observable<Url> {
    return this.http.put<Url>(`${this.apiUrl}/${shortCode}`, request);
  }

  deleteUrl(shortCode: string): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${shortCode}`, { responseType: 'text' });
  }

  getQrCode(shortCode: string): Observable<string> {
    return this.http.get(`${this.apiUrl}/${shortCode}/qrcode`, { responseType: 'text' });
  }
}
