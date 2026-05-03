import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { CurrencyTotal } from '../models/currency-total.model';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {

  private baseUrl = `${environment.apiUrl}/documents/dashboard`;

  constructor(private http: HttpClient) {}

  getTotalsByCurrency(): Observable<CurrencyTotal[]> {
    return this.http.get<CurrencyTotal[]>(
      `${this.baseUrl}/totals-by-currency`
    );
  }
}
