import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="forbidden">
      <h2>Access Denied</h2>
      <p>You do not have permission to view this page.</p>
      <p>Please contact your administrator if you believe this is an error.</p>
    </div>
  `,
  styles: [`
    .forbidden { 
      text-align: center; 
      margin-top: 2rem; 
      color: #dc3545;
      padding: 2rem;
    }
    h2 {
      color: #dc3545;
      margin-bottom: 1rem;
    }
  `]
})
export class ForbiddenComponent {}