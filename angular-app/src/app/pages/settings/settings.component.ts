import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="settings-container">
      <h1>Settings</h1>
      <p>Configure your application preferences</p>
      <div class="settings-content">
        <div class="card">
          <h2>Account Settings</h2>
          <p>Manage your account information and preferences</p>
        </div>
        <div class="card">
          <h2>Notification Settings</h2>
          <p>Configure how you receive notifications</p>
        </div>
        <div class="card">
          <h2>Privacy Settings</h2>
          <p>Control your privacy and security options</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-container {
      padding: 20px;
    }
    
    .settings-container h1 {
      color: #333;
      margin-bottom: 20px;
    }
    
    .settings-content {
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
export class SettingsComponent {
}
