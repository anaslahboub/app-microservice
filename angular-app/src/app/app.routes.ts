import { Routes } from '@angular/router';
import { ForbiddenComponent } from './utils/keycloak/ForbiddenComponent';
import { MainComponent } from './pages/main/main.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { GroupsComponent } from './pages/groups/groups.component';

export const routes: Routes = [
    { path: 'forbidden', component: ForbiddenComponent },
    { path: 'chat', component: MainComponent },
    { path: 'groups', component: GroupsComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'settings', component: SettingsComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
];