import { Routes } from '@angular/router';
import { ForbiddenComponent } from './utils/keycloak/ForbiddenComponent';
import { MainComponent } from './pages/main/main.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { GroupsComponent } from './pages/groups/groups.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { AdminComponent } from './pages/admin/admin.component';

export const routes: Routes = [
    { path: 'forbidden', component: ForbiddenComponent },
    { path: 'chat', component: MainComponent },
    { path: 'groups', component: GroupsComponent },
    { path: 'notifications', component: NotificationsComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'settings', component: SettingsComponent },
    { path: 'admin', component: AdminComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
];