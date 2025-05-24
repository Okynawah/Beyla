import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Site } from './Site';
import { Page } from './Page';

@Injectable({
  providedIn: 'root'
})
export class QueryService {

  constructor(private http: HttpClient) { }

search(query: string, page: number = 1, size: number = 1000): Promise<Site[]> {
    return this.http.get<Page<Site>>(`/api/query?q=${query}&p=${page}&s=${size}`)
      .toPromise()
      .then(page => page!.content);
  }

}
