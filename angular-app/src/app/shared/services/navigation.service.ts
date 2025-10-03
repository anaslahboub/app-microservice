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
      icon: 'fas fa-cog',
      label: 'Settings',
      route: '/settings',
      isOpen: false
    },
    {
      icon: 'fas fa-envelope',
      label: 'Messages',
      route: '/chat'
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
