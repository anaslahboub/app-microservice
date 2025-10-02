import { Routes } from '@angular/router';
import { ForbiddenComponent } from './utils/keycloak/ForbiddenComponent';
import { MainComponent } from './pages/main/main.component';

export const routes: Routes = [
    { path: 'forbidden', component: ForbiddenComponent },
    { path: '', component:  MainComponent },

];
