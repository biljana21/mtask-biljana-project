import { Routes } from '@angular/router';
import { DocumentComponent } from './pages/document/document.component';
import { LayoutComponent } from './layout/layout/layout.component';
import { StatisticComponent } from './pages/statistic/statistic.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'document', pathMatch: 'full' },
      { path: 'document', component: DocumentComponent },
      { path: 'statistic', component: StatisticComponent }
    ]
  }
];
