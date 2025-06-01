import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Site } from './Site';
import { QueryService } from './query.service';
import { StatsService } from './stats.service';
import { ServerStats } from './ServerStats';

@Component({
  selector: 'app-root',
  imports: [FormsModule, CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  stats?: ServerStats;
  searchQuery: string = '';
  timeTaken: number = 0;
  title: string = 'Beyla';
  results: Site[] = [{ title: "test", url: "", indexed_at: Date.now(), description: "", score: 0 }];

  constructor(private queryService: QueryService, private statsService: StatsService) { 
    this.statsRefresh()
  }  

  async search() {
    if (this.searchQuery) {
      console.log('Recherche pour:', this.searchQuery);
      let startTime = Date.now()
      this.results = await this.queryService.search(this.searchQuery.trim().toLowerCase());
      let endTime = Date.now()
      this.timeTaken = endTime - startTime
    }
  }

  async statsRefresh() {
    this.statsService.getStats().then(a => {this.stats = a; console.log(a); console.log(this.stats)})
  }
}
