import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ServerStats } from './ServerStats';

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  constructor(private http: HttpClient) { }

  getStats(): Promise<ServerStats | undefined> {
    return this.http.get<ServerStats>(`/api/stats/`).toPromise()
  }

}
