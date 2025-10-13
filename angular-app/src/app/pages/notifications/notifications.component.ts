import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatNotificationService } from '../../services/chat-notification.service';
import { GroupNotificationService } from '../../services/group-notification.service';
import { NotificationDto as ChatNotificationDto } from '../../services/chat-services/models/notification-dto';
import { NotificationDto as GroupNotificationDto } from '../../services/group-services/models/notification-dto';

// Define a unified interface for the component
interface UnifiedNotification {
  id: number;
  type: string;
  content: string;
  timestamp: string;
  read: boolean;
  source: 'chat' | 'group';
  originalData: ChatNotificationDto | GroupNotificationDto;
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit {

  notifications: UnifiedNotification[] = [];
  loading = false;

  constructor(
    private chatNotificationService: ChatNotificationService,
    private groupNotificationService: GroupNotificationService
  ) { }

  async ngOnInit(): Promise<void> {
    await this.loadNotifications();
  }

  async loadNotifications(): Promise<void> {
    this.loading = true;
    try {
      // Get notifications from both services in parallel
      const [chatNotifications, groupNotifications] = await Promise.all([
        this.chatNotificationService.getAllNotifications(),
        this.groupNotificationService.getAllNotifications()
      ]);
      
      // Convert to unified format and filter out any that might have been marked as deleted
      const unifiedChatNotifications = chatNotifications
        .filter(notification => notification.id !== undefined && notification.id !== null)
        .map(notification => ({
          id: notification.id!,
          type: notification.type || 'MESSAGE',
          content: notification.content || `New message from ${notification.senderId}`,
          timestamp: new Date().toISOString(),
          read: notification.read || false,
          source: 'chat' as const,
          originalData: notification
        }));
      
      const unifiedGroupNotifications = groupNotifications
        .filter(notification => notification.id !== undefined && notification.id !== null)
        .map(notification => ({
          id: notification.id!,
          type: notification.type || 'GROUP_NOTIFICATION',
          content: notification.message || `New update in ${notification.groupName}`,
          timestamp: notification.timestamp || new Date().toISOString(),
          read: notification.read || false,
          source: 'group' as const,
          originalData: notification
        }));
      
      // Combine and sort by timestamp (newest first)
      this.notifications = [...unifiedChatNotifications, ...unifiedGroupNotifications]
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
      
    } catch (error) {
      console.error('Error loading notifications:', error);
    } finally {
      this.loading = false;
    }
  }

  async markAsRead(notificationId: number): Promise<void> {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification) {
      try {
        if (notification.source === 'chat') {
          await this.chatNotificationService.markAsRead(notificationId);
        } else {
          await this.groupNotificationService.markAsRead(notificationId);
        }
        // Update local state
        notification.read = true;
      } catch (error) {
        console.error('Error marking notification as read:', error);
      }
    }
  }

  async markAllAsRead(): Promise<void> {
    try {
      // Mark all as read in both services
      await Promise.all([
        this.chatNotificationService.markAllAsRead(),
        this.groupNotificationService.markAllAsRead()
      ]);
      // Update local state
      this.notifications.forEach(notification => {
        notification.read = true;
      });
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
    }
  }

  async deleteNotification(notificationId: number): Promise<void> {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification) {
      try {
        console.log('Attempting to delete notification:', notificationId, 'from source:', notification.source);
        if (notification.source === 'chat') {
          await this.chatNotificationService.deleteNotification(notificationId);
        } else {
          await this.groupNotificationService.deleteNotification(notificationId);
        }
        console.log('Successfully deleted notification from backend, removing from UI');
        // Remove from local array
        this.notifications = this.notifications.filter(n => n.id !== notificationId);
        console.log('Notification removed from UI. Remaining notifications:', this.notifications.length);
      } catch (error) {
        console.error('Error deleting notification:', error);
      }
    } else {
      console.warn('Attempted to delete non-existent notification with id:', notificationId);
    }
  }

  getUnreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  getNotificationTitle(notification: UnifiedNotification): string {
    if (notification.source === 'chat') {
      const chatNotification = notification.originalData as ChatNotificationDto;
      return `New message from ${chatNotification.senderId || 'Unknown'}`;
    } else {
      const groupNotification = notification.originalData as GroupNotificationDto;
      return `Update in ${groupNotification.groupName || 'Unknown Group'}`;
    }
  }

  getNotificationMessage(notification: UnifiedNotification): string {
    if (notification.source === 'chat') {
      const chatNotification = notification.originalData as ChatNotificationDto;
      return chatNotification.content || 'You have a new message';
    } else {
      const groupNotification = notification.originalData as GroupNotificationDto;
      return groupNotification.message || 'There is a new update in the group';
    }
  }

  getNotificationIcon(notification: UnifiedNotification): string {
    if (notification.source === 'chat') {
      return 'fas fa-envelope';
    } else {
      return 'fas fa-users';
    }
  }

  getNotificationClass(notification: UnifiedNotification): string {
    if (notification.source === 'chat') {
      return 'notification-message';
    } else {
      return 'notification-group';
    }
  }

  trackByNotificationId(index: number, notification: UnifiedNotification): number {
    return notification.id;
  }
}