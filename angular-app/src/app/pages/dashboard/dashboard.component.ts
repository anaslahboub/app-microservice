import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <h1>Dashboard</h1>
      <p>Welcome to the dashboard page!</p>
      <div class="dashboard-content">
        <div class="card">
          <h2>Statistics</h2>
          <p>Overview of your application metrics</p>
        </div>
        <div class="card">
          <h2>Recent Activity</h2>
          <p>Latest updates and notifications</p>
        </div>
        <div class="card">
          <h2>Quick Actions</h2>
          <p>Common tasks and shortcuts</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
    }
    
    .dashboard-container h1 {
      color: #333;
      margin-bottom: 20px;
    }
    
    .dashboard-content {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }
    
    .card {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .card h2 {
      margin-top: 0;
      color: #444;
    }
  `]
})
export class DashboardComponent {
}