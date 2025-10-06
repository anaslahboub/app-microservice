import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit {

  notifications: any[] = [
    {
      id: 1,
      title: 'New message from John Doe',
      message: 'You have received a new message in the group chat.',
      type: 'message',
      timestamp: new Date(),
      isRead: false
    },
    {
      id: 2,
      title: 'Group invitation',
      message: 'You have been invited to join "Study Group Alpha".',
      type: 'group',
      timestamp: new Date(Date.now() - 3600000), // 1 hour ago
      isRead: true
    },
    {
      id: 3,
      title: 'System update',
      message: 'The application has been updated with new features.',
      type: 'system',
      timestamp: new Date(Date.now() - 7200000), // 2 hours ago
      isRead: true
    }
  ];

  constructor() { }

  ngOnInit(): void {
    // Load notifications from service
    this.loadNotifications();
  }

  loadNotifications(): void {
    // TODO: Implement notification loading from service
    console.log('Loading notifications...');
  }

  markAsRead(notificationId: number): void {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification) {
      notification.isRead = true;
    }
  }

  markAllAsRead(): void {
    this.notifications.forEach(notification => {
      notification.isRead = true;
    });
  }

  deleteNotification(notificationId: number): void {
    this.notifications = this.notifications.filter(n => n.id !== notificationId);
  }

  getUnreadCount(): number {
    return this.notifications.filter(n => !n.isRead).length;
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'message':
        return 'fas fa-envelope';
      case 'group':
        return 'fas fa-users';
      case 'system':
        return 'fas fa-cog';
      default:
        return 'fas fa-bell';
    }
  }

  getNotificationClass(type: string): string {
    switch (type) {
      case 'message':
        return 'notification-message';
      case 'group':
        return 'notification-group';
      case 'system':
        return 'notification-system';
      default:
        return 'notification-default';
    }
  }

  trackByNotificationId(index: number, notification: any): any {
    return notification.id;
  }
}
