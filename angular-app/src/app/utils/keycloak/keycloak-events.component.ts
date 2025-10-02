import { Component, inject } from '@angular/core';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType, KeycloakEvent } from 'keycloak-angular';
import { effect } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-keycloak-events',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="events-container">
      <h3>Keycloak Events</h3>
      <ul>
        <li *ngFor="let message of messages">{{ message }}</li>
      </ul>
    </div>
  `,
  styles: [`
    .events-container {
      border: 1px solid #ccc;
      padding: 1rem;
      border-radius: 0.5rem;
      max-width: 500px;
      margin: 1rem auto;
      font-family: Arial, sans-serif;
    }
    ul {
      padding-left: 1rem;
    }
    li {
      margin-bottom: 0.5rem;
      color: #007bff;
    }
  `]
})
export class KeycloakEventsComponent {
  private keycloakSignal = inject(KEYCLOAK_EVENT_SIGNAL);
  messages: string[] = [];

  constructor() {
    effect(() => {
      const event: KeycloakEvent | undefined = this.keycloakSignal();

      if (!event) return;

      switch(event.type) {
        case KeycloakEventType.Ready:
          this.messages.push('Keycloak is ready.');
          break;
        case KeycloakEventType.AuthLogout:
          this.messages.push('User logged out.');
          break;
        case KeycloakEventType.AuthRefreshSuccess:
          this.messages.push('Token successfully refreshed.');
          break;
        case KeycloakEventType.AuthRefreshError:
          this.messages.push('Token refresh failed!');
          break;
        case KeycloakEventType.AuthError:
          this.messages.push('Authentication error occurred.');
          break;
        default:
          this.messages.push(`Other Keycloak event: ${event.type}`);
      }

      // Limit the list to last 10 messages
      if (this.messages.length > 10) {
        this.messages.shift();
      }
    });
  }
}
