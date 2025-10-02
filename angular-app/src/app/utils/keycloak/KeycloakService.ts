import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {


  private readonly _keycloak = inject(Keycloak);


  constructor(
  ) {
  }
  get keycloak(): Keycloak  {
    return this._keycloak;
  }

  async login() {
    await this.keycloak?.login();
  }

  get userId(): string {
    return this.keycloak?.tokenParsed?.sub as string;
  }

  get isTokenValid(): boolean {
    return !this.keycloak?.isTokenExpired();
  }

  get fullName(): string {
    return this.keycloak?.tokenParsed?.['name'] as string;
  }

  logout() {
    return this.keycloak?.logout({redirectUri: 'http://localhost:4200'});
  }

  accountManagement() {
    return this.keycloak?.accountManagement();
  }
  
}