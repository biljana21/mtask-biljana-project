import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { CurrencyTotal } from '../../models/currency-total.model';
import { StatisticService } from '../../services/statistic.service';

Chart.register(...registerables);

@Component({
  selector: 'app-statistic',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistic.component.html',
  styleUrl: './statistic.component.css'
})
export class StatisticComponent implements OnInit {

  data: CurrencyTotal[] = [];

  constructor(private staisticService: StatisticService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.staisticService.getTotalsByCurrency()
      .subscribe(data => {
        this.data = data;
        this.renderChart(data);
      });
  }

  renderChart(data: any[]) {

    const labels = data.map(x => x.currency);
    const values = data.map(x => x.total);

    new Chart('currencyPieChart', {
      type: 'pie',
      data: {
        labels,
        datasets: [{
          data: values
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }
}