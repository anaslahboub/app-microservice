import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { KeycloakService } from '../../utils/keycloak/KeycloakService';
import { GroupApiService } from '../../services/group-api.service';
import { GroupDto, GroupPostDto, GroupMemberDto } from '../../services/group-services/models';
import { UserResponse } from '../../services/chat-services/models';
import { PickerComponent } from '@ctrl/ngx-emoji-mart';

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Api } from '../../services/chat-services/api';
import { getAllUsers } from '../../services/chat-services/functions';

/**
 * Main Groups Component - Advanced Group Management System
 * Features: Real-time messaging, file sharing, member management, statistics
 */
@Component({
  selector: 'app-groups',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DatePipe,
    PickerComponent
  ],
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.scss']
})
export class GroupsComponent implements OnInit, OnDestroy, AfterViewChecked {
  
  // ========================================
  // COMPONENT STATE & DATA
  // ========================================
  
  /** Currently selected group for detailed view */
  selectedGroup: GroupDto | null = null;
  
  /** All available groups for the user */
  groups: GroupDto[] = [];
  
  /** Posts in the currently selected group */
  groupPosts: GroupPostDto[] = [];
  
  /** Members of the currently selected group */
  groupMembers: GroupMemberDto[] = [];
  
  /** New post content being composed */
  newPostContent: string = '';
  
  /** Loading states for different operations */
  isLoadingGroups = false;
  isLoadingPosts = false;
  isLoadingMembers = false;
  isSendingPost = false;
  
  /** UI state flags */
  showEmojiPicker = false;
  showMemberManagement = false;
  showGroupSettings = false;
  showCreateGroupModal = false;
  showSearchResults = false;
  
  /** Search functionality */
  searchTerm: string = '';
  searchResults: GroupDto[] = [];
  
  /** WebSocket connection for real-time updates */
  private stompClient: any = null;
  private isConnected = false;
  
  /** Reference to chat container for auto-scrolling */
  @ViewChild('chatContainer') chatContainer!: ElementRef;
  
  /** File upload */
  selectedFile: File | null = null;
  uploadProgress = 0;
  
  /** Available users for adding to groups */
  availableGroups: GroupMemberDto[] = [];
  availableUsers: UserResponse[] = [];
  showAddMemberModal = false;
  selectedUsersToAdd: Set<string> = new Set();
  isLoadingAvailableUsers = false;
  
  // ========================================
  // CONSTRUCTOR & LIFECYCLE
  // ========================================
  
  constructor(
    public keycloakService: KeycloakService,
    private groupApiService: GroupApiService,
    private chatApiService: Api
  ) {}
  
  ngOnInit(): void {
    this.initializeGroups();
    this.connectWebSocket();
  }
  
  ngOnDestroy(): void {
    this.disconnectWebSocket();
  }
  
  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }
  
  // ========================================
  // INITIALIZATION METHODS
  // ========================================
  
  /**
   * Loads all groups for the current user
   */
  async initializeGroups(): Promise<void> {
    this.isLoadingGroups = true;
    try {
      const userId = this.keycloakService.userId;
      if (userId) {
        this.groups = await this.groupApiService.getUserGroups(userId);
        console.log('Groups loaded:', this.groups);
      }
    } catch (error) {
      console.error('Error loading groups:', error);
      this.showError('Failed to load groups');
    } finally {
      this.isLoadingGroups = false;
    }
  }
  
  /**
   * Establishes WebSocket connection for real-time updates
   */
  private connectWebSocket(): void {
    try {
      const socket = new SockJS('http://localhost:8082/ws');
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {},
        debug: (str) => {
          console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.stompClient.onConnect = (frame: any) => {
        console.log('Connected to group WebSocket:', frame);
        this.isConnected = true;
        
        // Subscribe to group updates
        this.stompClient.subscribe('/topic/group-updates', (message: any) => {
          const update = JSON.parse(message.body);
          this.handleGroupUpdate(update);
        });
        
        // Subscribe to post updates
        this.stompClient.subscribe('/topic/group-posts', (message: any) => {
          const post = JSON.parse(message.body);
          this.handleNewPost(post);
        });
      };

      this.stompClient.onStompError = (error: any) => {
        console.error('WebSocket connection error:', error);
        this.isConnected = false;
      };

      this.stompClient.activate();
    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
    }
  }
  
  /**
   * Disconnects WebSocket connection
   */
  private disconnectWebSocket(): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.disconnect();
      this.isConnected = false;
    }
  }
  
  // ========================================
  // GROUP MANAGEMENT
  // ========================================
  
  /**
   * Handles group selection and loads group details
   * @param group - Selected group
   */
  async selectGroup(group: GroupDto): Promise<void> {
    this.selectedGroup = group;
    this.showMemberManagement = false;
    this.showGroupSettings = false;
    
    await Promise.all([
      this.loadGroupPosts(group.id!),
      this.loadGroupMembers(group.id!)
    ]);
  }
  
  /**
   * Loads posts for the selected group
   * @param groupId - ID of the group
   */
  async loadGroupPosts(groupId: number): Promise<void> {
    this.isLoadingPosts = true;
    try {
      this.groupPosts = await this.groupApiService.getGroupPosts(groupId);
    } catch (error) {
      console.error('Error loading group posts:', error);
      this.showError('Failed to load group posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }
  
  /**
   * Loads members for the selected group
   * @param groupId - ID of the group
   */
  async loadGroupMembers(groupId: number): Promise<void> {
    this.isLoadingMembers = true;
    try {
      this.groupMembers = await this.groupApiService.getGroupMembers(groupId);
    } catch (error) {
      console.error('Error loading group members:', error);
      this.showError('Failed to load group members');
    } finally {
      this.isLoadingMembers = false;
    }
  }
  
  /**
   * Creates a new group
   * @param groupName - Name of the new group
   * @param groupDescription - Description of the new group
   * @param groupSubject - Subject of the new group
   */
  async createNewGroup(groupName: string, groupDescription: string, groupSubject: string): Promise<void> {
    if (!groupName.trim()) {
      this.showError('Group name is required');
      return;
    }

    try {
      const groupData = {
        name: groupName.trim(),
        description: groupDescription.trim(),
        subject: groupSubject.trim()
      };

      const newGroup = await this.groupApiService.createGroup(groupData);
      
      // Add the new group to the local groups array
      this.groups.push(newGroup);
      
      // Close the modal
      this.closeCreateGroupModal();
      
      // Show success message
      console.log('Group created successfully');
    } catch (error) {
      console.error('Error creating group:', error);
      this.showError('Failed to create group');
    }
  }

  // ========================================
  // POST MANAGEMENT
  // ========================================
  
  /**
   * Sends a new post to the selected group
   */
  async sendPost(): Promise<void> {
    if (!this.selectedGroup || !this.newPostContent.trim()) {
      return;
    }
    
    this.isSendingPost = true;
    try {
      const postData = {
        content: this.newPostContent.trim(),
        type: 'TEXT'
      };
      
      const newPost = await this.groupApiService.createPost(
        this.selectedGroup.id!, 
        {
          ...postData,
          type: 'TEXT' as 'TEXT'
        }
      );
      
      // Add to local posts array
      this.groupPosts.unshift(newPost);
      this.newPostContent = '';
      this.showEmojiPicker = false;
      
      // Scroll to bottom to show new post
      setTimeout(() => this.scrollToBottom(), 100);
      
    } catch (error) {
      console.error('Error sending post:', error);
      this.showError('Failed to send post');
    } finally {
      this.isSendingPost = false;
    }
  }
  
  /**
   * Handles file selection for upload
   * @param event - File input event
   */
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadFile();
    }
  }
  
  /**
   * Uploads selected file to the group
   */
  async uploadFile(): Promise<void> {
    if (!this.selectedFile || !this.selectedGroup) {
      return;
    }
    
    try {
      this.uploadProgress = 0;
      
      // Simulate upload progress
      const progressInterval = setInterval(() => {
        this.uploadProgress += 10;
        if (this.uploadProgress >= 90) {
          clearInterval(progressInterval);
        }
      }, 200);
      
      const result = await this.groupApiService.uploadFile(
        this.selectedGroup.id!, 
        this.selectedFile
      );
      
      clearInterval(progressInterval);
      this.uploadProgress = 100;
      
      // Create post for uploaded file
      const postData = {
        content: `📎 ${this.selectedFile.name}`,
        type: this.getFileType(this.selectedFile.type)
      };
      
      const newPost = await this.groupApiService.createPost(
        this.selectedGroup.id!, 
        postData as { content: string; type: 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT' | 'LINK' }
      );
      
      this.groupPosts.unshift(newPost);
      this.selectedFile = null;
      this.uploadProgress = 0;
      
      setTimeout(() => this.scrollToBottom(), 100);
      
    } catch (error) {
      console.error('Error uploading file:', error);
      this.showError('Failed to upload file');
      this.uploadProgress = 0;
    }
  }
  
  /**
   * Determines file type based on MIME type
   * @param mimeType - File MIME type
   * @returns Group post type
   */
  private getFileType(mimeType: string): string {
    if (mimeType.startsWith('image/')) return 'IMAGE';
    if (mimeType.startsWith('video/')) return 'VIDEO';
    if (mimeType.startsWith('audio/')) return 'AUDIO';
    return 'DOCUMENT';
  }
  
  // ========================================
  // SEARCH FUNCTIONALITY
  // ========================================
  
  /**
   * Searches for groups based on search term
   */
  async searchGroups(): Promise<void> {
    if (!this.searchTerm.trim()) {
      this.showSearchResults = false;
      return;
    }
    
    try {
      this.searchResults = await this.groupApiService.searchGroups(this.searchTerm);
      this.showSearchResults = true;
    } catch (error) {
      console.error('Error searching groups:', error);
      this.showError('Search failed');
    }
  }
  
  /**
   * Clears search results
   */
  clearSearch(): void {
    this.searchTerm = '';
    this.searchResults = [];
    this.showSearchResults = false;
  }
  
  // ========================================
  // WEBSOCKET HANDLERS
  // ========================================
  
  /**
   * Handles real-time group updates
   * @param update - Group update data
   */
  private handleGroupUpdate(update: any): void {
    console.log('Received group update:', update);
    
    // Update groups list if needed
    const index = this.groups.findIndex(g => g.id === update.groupId);
    if (index !== -1) {
      this.groups[index] = { ...this.groups[index], ...update };
    }
  }
  
  /**
   * Handles new post notifications
   * @param post - New post data
   */
  private handleNewPost(post: GroupPostDto): void {
    if (this.selectedGroup && post.groupId === this.selectedGroup.id) {
      this.groupPosts.unshift(post);
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }
  
  // ========================================
  // UI HELPERS
  // ========================================
  
  /**
   * Scrolls chat container to bottom
   */
  private scrollToBottom(): void {
    if (this.chatContainer) {
      this.chatContainer.nativeElement.scrollTop = 
        this.chatContainer.nativeElement.scrollHeight;
    }
  }
  
  /**
   * Shows error message to user
   * @param message - Error message
   */
  private showError(message: string): void {
    // In a real app, you'd use a toast service or modal
    console.error('Error:', message);
    alert(message);
  }
  
  /**
   * Gets user display name
   * @param userId - User ID
   * @returns Display name
   */
  getUserDisplayName(userId: string): string {
    if (userId === this.keycloakService.userId) {
      return 'You';
    }
    
    const member = this.groupMembers.find(m => m.userId === userId);
    return member ? `${member.userId}` : 'Unknown User';
  }
  
  /**
   * Checks if current user is admin of selected group
   * @returns True if user is admin
   */
  isCurrentUserAdmin(): boolean {
    if (!this.selectedGroup || !this.keycloakService.userId) {
      return false;
    }
    
    const member = this.groupMembers.find(
      m => m.userId === this.keycloakService.userId
    );
    
    return member?.admin === true;
  }
  
  /**
   * Checks if current user is co-admin of selected group
   * @returns True if user is co-admin
   */
  isCurrentUserCoAdmin(): boolean {
    if (!this.selectedGroup || !this.keycloakService.userId) {
      return false;
    }
    
    const member = this.groupMembers.find(
      m => m.userId === this.keycloakService.userId
    );
    
    return member?.coAdmin === true;
  }
  
  /**
   * Checks if current user has admin privileges
   * @returns True if user is admin or co-admin
   */
  hasAdminPrivileges(): boolean {
    return this.isCurrentUserAdmin() || this.isCurrentUserCoAdmin();
  }
  
  // ========================================
  // MODAL CONTROLS
  // ========================================
  
  /**
   * Opens group creation modal
   */
  openCreateGroupModal(): void {
    this.showCreateGroupModal = true;
  }
  
  /**
   * Closes group creation modal
   */
  closeCreateGroupModal(): void {
    this.showCreateGroupModal = false;
  }
  
  /**
   * Toggles member management panel
   */
  toggleMemberManagement(): void {
    this.showMemberManagement = !this.showMemberManagement;
  }
  
  /**
   * Toggles group settings panel
   */
  toggleGroupSettings(): void {
    this.showGroupSettings = !this.showGroupSettings;
  }
  
  /**
   * Toggles emoji picker
   */
  toggleEmojiPicker(): void {
    this.showEmojiPicker = !this.showEmojiPicker;
  }
  
  /**
   * Adds emoji to post content
   * @param emoji - Selected emoji
   */
  addEmoji(emoji: any): void {
    this.newPostContent += emoji.emoji.native;
    this.showEmojiPicker = false;
  }

  // ========================================
  // MISSING METHODS FOR HTML TEMPLATE
  // ========================================

  /**
   * Deletes a post from the group
   * @param groupId - ID of the group
   * @param postId - ID of the post to delete
   */
  async deletePost(groupId: number, postId: number): Promise<void> {
    const confirmed = confirm('Are you sure you want to delete this post?');
    if (!confirmed) return;

    try {
      await this.groupApiService.deletePost(groupId, postId);
      this.groupPosts = this.groupPosts.filter(post => post.id !== postId);
    } catch (error) {
      console.error('Error deleting post:', error);
      this.showError('Failed to delete post');
    }
  }

  /**
   * Downloads a file from a post
   * @param groupId - ID of the group
   * @param fileId - ID of the file to download
   */
  async downloadFile(groupId: number, fileId: number): Promise<void> {
    try {
      const blob = await this.groupApiService.downloadFile(groupId, fileId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `file-${fileId}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error downloading file:', error);
      this.showError('Failed to download file');
    }
  }

  /**
   * Archives a group
   * @param groupId - ID of the group to archive
   */
  async archiveGroup(groupId: number): Promise<void> {
    const confirmed = confirm('Are you sure you want to archive this group?');
    if (!confirmed) return;

    try {
      await this.groupApiService.archiveGroup(groupId);
      this.groups = this.groups.filter(group => group.id !== groupId);
      if (this.selectedGroup?.id === groupId) {
        this.selectedGroup = null;
      }
      console.log('Group archived successfully');
    } catch (error) {
      console.error('Error archiving group:', error);
      this.showError('Failed to archive group');
    }
  }

  /**
   * Deletes a group permanently
   * @param groupId - ID of the group to delete
   */
  async deleteGroup(groupId: number): Promise<void> {
    const confirmed = confirm('Are you sure you want to permanently delete this group? This action cannot be undone.');
    if (!confirmed) return;

    try {
      await this.groupApiService.deleteGroup(groupId);
      this.groups = this.groups.filter(group => group.id !== groupId);
      if (this.selectedGroup?.id === groupId) {
        this.selectedGroup = null;
      }
      console.log('Group deleted successfully');
    } catch (error) {
      console.error('Error deleting group:', error);
      this.showError('Failed to delete group');
    }
  }

  /**
   * Updates a group's information
   * @param group - Group data to update
   * @returns Updated group
   */
  async updateGroup(group: GroupDto): Promise<void> {
    if (!group || !group.id) {
      this.showError('Invalid group data');
      return;
    }

    try {
      const updatedGroup = await this.groupApiService.updateGroup(group);
      console.log('Group updated successfully',updatedGroup);
    } catch (error) {
      console.error('Error updating group:', error);
      this.showError('Failed to update group');
    }
  }

  /**
   * Makes a member co-admin
   * @param groupId - ID of the group
   * @param userId - ID of the user to make co-admin
   */
  async makeCoAdmin(groupId: number, userId: string): Promise<void> {
    try {
      await this.groupApiService.makeCoAdmin(groupId, userId);
      // Update local member data
      const member = this.groupMembers.find(m => m.userId === userId);
      if (member) {
        member.coAdmin = true;
      }
      console.log('Co-admin designated successfully');
    } catch (error) {
      console.error('Error designating co-admin:', error);
      this.showError('Failed to designate co-admin');
    }
  }

  /**
   * Removes a member from the group
   * @param groupId - ID of the group
   * @param userId - ID of the user to remove
   */
  async removeMemberFromGroup(groupId: number, userId: string): Promise<void> {
    const confirmed = confirm('Are you sure you want to remove this member from the group?');
    if (!confirmed) return;

    try {
      await this.groupApiService.removeMemberFromGroup(groupId, userId);
      this.groupMembers = this.groupMembers.filter(member => member.userId !== userId);
      console.log('Member removed successfully');
    } catch (error) {
      console.error('Error removing member:', error);
      this.showError('Failed to remove member');
    }
  }
/**
 * Loads all available users that can be added to a group
 */
async loadAvailableUsers(): Promise<void> {
  this.isLoadingAvailableUsers = true;

  try {
    if (!this.selectedGroup || !this.selectedGroup.id) {
      this.availableUsers = [];
      return;
    }

    this.availableGroups = await this.groupApiService.getGroupMembers(this.selectedGroup.id);

    this.availableUsers = await this.chatApiService.invoke(getAllUsers, {});

    const memberIds = new Set(
      [...(this.availableGroups || []), ...(this.groupMembers || [])].map(m => m.userId)
    );

    this.availableUsers = this.availableUsers.filter(user => !memberIds.has(user.id));

    console.log("✅ Available users:", this.availableUsers);

  } catch (error) {
    console.error('❌ Error loading available users:', error);
    this.showError('Failed to load available users');
    this.availableUsers = [];
  } finally {
    this.isLoadingAvailableUsers = false;
  }
}


  /**
   * Opens the add member modal
   */
  async openAddMemberModal(): Promise<void> {
    if (!this.selectedGroup) return;
    
    this.showAddMemberModal = true;
    this.selectedUsersToAdd.clear();
    
    // Ensure we have the latest group members data
    await this.loadGroupMembers(this.selectedGroup.id!);
    await this.loadAvailableUsers();
  }

  /**
   * Closes the add member modal
   */
  closeAddMemberModal(): void {
    this.showAddMemberModal = false;
    this.selectedUsersToAdd.clear();
  }

  /**
   * Toggles selection of a user to add
   * @param userId - ID of the user to toggle
   */
  toggleUserSelection(userId: string): void {
    if (this.selectedUsersToAdd.has(userId)) {
      this.selectedUsersToAdd.delete(userId);
    } else {
      this.selectedUsersToAdd.add(userId);
    }
  }

  /**
   * Adds selected users to the current group
   */
  async addSelectedUsersToGroup(): Promise<void> {
    if (!this.selectedGroup || this.selectedUsersToAdd.size === 0) return;

    try {
      const userIds = Array.from(this.selectedUsersToAdd);
      let successCount = 0;
      
      // Add each user to the group
      for (const userId of userIds) {
        try {
          await this.groupApiService.addMemberToGroup(this.selectedGroup.id!, userId);
          successCount++;
        } catch (error) {
          console.error(`Error adding user ${userId} to group:`, error);
          // Continue with other users even if one fails
        }
      }

      // Reload group members to reflect changes
      await this.loadGroupMembers(this.selectedGroup.id!);
      
      // Close modal and clear selection
      this.closeAddMemberModal();
      
      console.log(`${successCount} users added to group successfully`);
      if (successCount < userIds.length) {
        this.showError(`${successCount} of ${userIds.length} users added successfully. Some users could not be added.`);
      }
    } catch (error) {
      console.error('Error adding users to group:', error);
      this.showError('Failed to add users to group. Please try again.');
    }
  }

}