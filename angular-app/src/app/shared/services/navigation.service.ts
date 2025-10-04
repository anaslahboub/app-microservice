import { Injectable } from '@angular/core';
import { MenuItem } from '../models/menu-item.interface';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  
  private readonly menuItems: MenuItem[] = [
    {
      icon: 'fas fa-home',
      label: 'Dashboard',
      route: '/dashboard',
      isOpen: false
    },
    {
      icon: 'fas fa-envelope',
      label: 'Messages',
      route: '/chat',
      isOpen: false
    },
    {
      icon: 'fas fa-users-class',
      label: 'Groups',
      route: '/groups',
      isOpen: false
    },
    {
      icon: 'fas fa-cog',
      label: 'Settings',
      route: '/settings',
      isOpen: false
    }
  ];

  getMenuItems(): MenuItem[] {
    return [...this.menuItems]; // Return a copy to prevent external mutations
  }

  toggleMenuItem(item: MenuItem): void {
    if (item.children) {
      item.isOpen = !item.isOpen;
    }
  }
}
