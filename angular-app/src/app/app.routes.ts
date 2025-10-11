import { Routes } from '@angular/router';
import { ForbiddenComponent } from './utils/keycloak/ForbiddenComponent';
import { MainComponent } from './pages/chat/chat.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { GroupsComponent } from './pages/groups/groups.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { PostsComponent } from './pages/posts/posts.component';

export const routes: Routes = [
    { path: 'forbidden', component: ForbiddenComponent },
    { path: 'chat', component: MainComponent },
    { path: 'groups', component: GroupsComponent },
    { path: 'notifications', component: NotificationsComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'settings', component: SettingsComponent },
    { path: 'posts', component: PostsComponent },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
];