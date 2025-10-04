import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DatePipe } from '@angular/common';
import { GroupDto } from '../../group-services/models';
import { GroupApiService } from '../../services/group-api.service';
import { KeycloakService } from '../../utils/keycloak/KeycloakService';

/**
 * Group List Component - Displays and manages groups
 * Similar to ChatListComponent but for group management
 */
@Component({
  selector: 'app-group-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './group-list.component.html',
  styleUrls: ['./group-list.component.scss']
})
export class GroupListComponent implements OnInit {
  
  // ========================================
  // INPUTS & OUTPUTS
  // ========================================
  
  /** List of groups to display */
  @Input() groups: GroupDto[] = [];
  
  /** Currently selected group */
  @Input() selectedGroup: GroupDto | null = null;
  
  /** Event emitted when a group is selected */
  @Output() groupSelected = new EventEmitter<GroupDto>();
  
  /** Event emitted when create group is requested */
  @Output() createGroupRequested = new EventEmitter<void>();
  
  // ========================================
  // COMPONENT STATE
  // ========================================
  
  /** Loading state */
  isLoading = false;
  
  /** Search functionality */
  searchTerm = '';
  filteredGroups: GroupDto[] = [];
  
  /** Group statistics */
  groupStats = {
    total: 0,
    active: 0,
    archived: 0,
    userGroups: 0
  };
  
  // ========================================
  // CONSTRUCTOR
  // ========================================
  
  constructor(
    private groupApiService: GroupApiService,
    private keycloakService: KeycloakService
  ) {}
  
  // ========================================
  // LIFECYCLE METHODS
  // ========================================
  
  ngOnInit(): void {
    this.filteredGroups = this.groups;
    this.calculateStats();
  }
  
  // ========================================
  // GROUP MANAGEMENT
  // ========================================
  
  /**
   * Handles group selection
   * @param group - Selected group
   */
  selectGroup(group: GroupDto): void {
    this.groupSelected.emit(group);
  }
  
  /**
   * Creates a new group
   */
  createGroup(): void {
    this.createGroupRequested.emit();
  }
  
  /**
   * Joins a group (if user is not already a member)
   * @param group - Group to join
   */
  async joinGroup(group: GroupDto): Promise<void> {
    if (!group.id) return;
    
    try {
      this.isLoading = true;
      await this.groupApiService.addMemberToGroup(group.id, this.keycloakService.userId);
      
      // Update local group data
      const updatedGroup = { ...group, memberCount: (group.memberCount || 0) + 1 };
      const index = this.groups.findIndex(g => g.id === group.id);
      if (index !== -1) {
        this.groups[index] = updatedGroup;
      }
      
      console.log('Successfully joined group:', group.name);
    } catch (error) {
      console.error('Error joining group:', error);
      alert('Failed to join group. You may already be a member.');
    } finally {
      this.isLoading = false;
    }
  }
  
  /**
   * Leaves a group
   * @param group - Group to leave
   */
  async leaveGroup(group: GroupDto): Promise<void> {
    if (!group.id) return;
    
    const confirmed = confirm(`Are you sure you want to leave "${group.name}"?`);
    if (!confirmed) return;
    
    try {
      this.isLoading = true;
      await this.groupApiService.leaveGroup(group.id);
      
      // Remove from local groups list
      this.groups = this.groups.filter(g => g.id !== group.id);
      this.filteredGroups = this.filteredGroups.filter(g => g.id !== group.id);
      
      console.log('Successfully left group:', group.name);
    } catch (error) {
      console.error('Error leaving group:', error);
      alert('Failed to leave group.');
    } finally {
      this.isLoading = false;
    }
  }
  
  // ========================================
  // SEARCH & FILTER
  // ========================================
  
  /**
   * Filters groups based on search term
   */
  filterGroups(): void {
    if (!this.searchTerm.trim()) {
      this.filteredGroups = this.groups;
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredGroups = this.groups.filter(group =>
        group.name?.toLowerCase().includes(term) ||
        group.description?.toLowerCase().includes(term) ||
        group.subject?.toLowerCase().includes(term)
      );
    }
  }
  
  /**
   * Clears search filter
   */
  clearSearch(): void {
    this.searchTerm = '';
    this.filteredGroups = this.groups;
  }
  
  /**
   * Filters groups by status (active/archived)
   * @param showArchived - Whether to show archived groups
   */
  filterByStatus(showArchived: boolean): void {
    if (showArchived) {
      this.filteredGroups = this.groups.filter(group => group.archived);
    } else {
      this.filteredGroups = this.groups.filter(group => !group.archived);
    }
  }
  
  // ========================================
  // UTILITY METHODS
  // ========================================
  
  /**
   * Calculates group statistics
   */
  private calculateStats(): void {
    this.groupStats = {
      total: this.groups.length,
      active: this.groups.filter(g => !g.archived).length,
      archived: this.groups.filter(g => g.archived).length,
      userGroups: this.groups.filter(g => g.createdBy === this.keycloakService.userId).length
    };
  }
  
  /**
   * Gets group status text
   * @param group - Group to get status for
   * @returns Status text
   */
  getGroupStatus(group: GroupDto): string {
    if (group.archived) {
      return 'Archived';
    }
    return 'Active';
  }
  
  /**
   * Gets group status class for styling
   * @param group - Group to get status class for
   * @returns CSS class name
   */
  getGroupStatusClass(group: GroupDto): string {
    if (group.archived) {
      return 'status-archived';
    }
    return 'status-active';
  }
  
  /**
   * Checks if current user is the creator of the group
   * @param group - Group to check
   * @returns True if user is creator
   */
  isGroupCreator(group: GroupDto): boolean {
    return group.createdBy === this.keycloakService.userId;
  }
  
  /**
   * Gets group creation date formatted
   * @param dateString - Date string
   * @returns Formatted date
   */
  getFormattedDate(dateString: string | undefined): string {
    if (!dateString) return 'Unknown';
    
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else if (diffDays < 30) {
      const weeks = Math.floor(diffDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    } else {
      return date.toLocaleDateString();
    }
  }
  
  /**
   * Gets group avatar color based on group name
   * @param groupName - Name of the group
   * @returns CSS color class
   */
  getGroupAvatarColor(groupName: string | undefined): string {
    if (!groupName) return 'avatar-default';
    
    const colors = [
      'avatar-blue', 'avatar-green', 'avatar-purple', 
      'avatar-orange', 'avatar-red', 'avatar-teal'
    ];
    
    const hash = groupName.split('').reduce((a, b) => {
      a = ((a << 5) - a) + b.charCodeAt(0);
      return a & a;
    }, 0);
    
    return colors[Math.abs(hash) % colors.length];
  }
  
  /**
   * Gets group icon based on subject or type
   * @param group - Group to get icon for
   * @returns FontAwesome icon class
   */
  getGroupIcon(group: GroupDto): string {
    if (group.archived) {
      return 'fas fa-archive';
    }
    
    if (group.subject) {
      const subject = group.subject.toLowerCase();
      if (subject.includes('math') || subject.includes('mathematics')) {
        return 'fas fa-calculator';
      } else if (subject.includes('science')) {
        return 'fas fa-flask';
      } else if (subject.includes('history')) {
        return 'fas fa-landmark';
      } else if (subject.includes('language') || subject.includes('english')) {
        return 'fas fa-language';
      } else if (subject.includes('art')) {
        return 'fas fa-palette';
      } else if (subject.includes('music')) {
        return 'fas fa-music';
      } else if (subject.includes('sport') || subject.includes('physical')) {
        return 'fas fa-running';
      }
    }
    
    return 'fas fa-users';
  }
  
  /**
   * Gets member count text with proper pluralization
   * @param count - Member count
   * @returns Formatted member count text
   */
  getMemberCountText(count: number | undefined): string {
    const memberCount = count || 0;
    return `${memberCount} member${memberCount !== 1 ? 's' : ''}`;
  }
}
