import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Api } from '../group-services/api';
import { KeycloakService } from '../utils/keycloak/KeycloakService';
import { 
  createGroup, 
  getAllGroups, 
  getGroupById, 
  updateGroup, 
  deleteGroup, 
  archiveGroup,
  searchGroups,
  getGroupsByUserId,
  getGroupsByTeacherId,
  addMember,
  removeMember,
  leaveGroup,
  designateCoAdmin,
  getGroupMembers,
  getStudentsByGroupId,
  createPost,
  getPublishedPosts,
  getPublishedPostsByUser,
  deletePost,
  uploadFile,
  downloadFile,
  getAllStudents,
  getGroupStatistics,
  getGroupStatisticsForTeacher
} from '../group-services/functions';
import { 
  GroupDto, 
  GroupMemberDto, 
  GroupPostDto, 
  CreateGroupRequest, 
  AddMemberRequest,
  GroupStatisticsDto
} from '../group-services/models';

/**
 * Comprehensive Group API Service
 * Wraps all group-related functionality with enhanced error handling and user experience
 */
@Injectable({
  providedIn: 'root'
})
export class GroupApiService {
  
  constructor(
    private groupApi: Api,
    private http: HttpClient,
    private keycloakService: KeycloakService
  ) {}

  // ========================================
  // GROUP MANAGEMENT
  // ========================================

  /**
   * Creates a new group with comprehensive validation
   * @param groupData - Group creation data
   * @returns Promise with created group information
   */
  async createGroup(groupData: CreateGroupRequest): Promise<GroupDto> {
    try {
      const result = await this.groupApi.invoke(createGroup, { body: groupData });
      console.log('Group created successfully:', result);
      return result;
    } catch (error) {
      console.error('Error creating group:', error);
      throw new Error('Failed to create group. Please try again.');
    }
  }

  /**
   * Retrieves all available groups with optional filtering
   * @param includeArchived - Whether to include archived groups
   * @returns Promise with array of groups
   */
  async getAllGroups(includeArchived: boolean = false): Promise<GroupDto[]> {
    try {
      const result = await this.groupApi.invoke(getAllGroups, {});
      // Filter out archived groups if not requested
      if (!includeArchived) {
        return result.filter(group => !group.archived);
      }
      return result;
    } catch (error) {
      console.error('Error fetching groups:', error);
      throw new Error('Failed to load groups. Please refresh the page.');
    }
  }

  /**
   * Gets detailed information about a specific group
   * @param groupId - ID of the group to retrieve
   * @returns Promise with group details
   */
  async getGroupDetails(groupId: number): Promise<GroupDto> {
    try {
      const result = await this.groupApi.invoke(getGroupById, { id: groupId });
      return result;
    } catch (error) {
      console.error('Error fetching group details:', error);
      throw new Error('Group not found or access denied.');
    }
  }

  /**
   * Updates group information
   * @param groupId - ID of the group to update
   * @param updateData - Updated group data
   * @returns Promise with updated group
   */
  async updateGroup(groupId: number, updateData: Partial<GroupDto>): Promise<GroupDto> {
    try {
      const result = await this.groupApi.invoke(updateGroup, { 
        id: groupId, 
        body: updateData 
      });
      return result;
    } catch (error) {
      console.error('Error updating group:', error);
      throw new Error('Failed to update group. Please check your permissions.');
    }
  }

  /**
   * Archives a group (soft delete)
   * @param groupId - ID of the group to archive
   * @returns Promise with operation result
   */
  async archiveGroup(groupId: number): Promise<void> {
    try {
      await this.groupApi.invoke(archiveGroup, { id: groupId });
      console.log('Group archived successfully');
    } catch (error) {
      console.error('Error archiving group:', error);
      throw new Error('Failed to archive group. You may not have sufficient permissions.');
    }
  }

  /**
   * Permanently deletes a group
   * @param groupId - ID of the group to delete
   * @returns Promise with operation result
   */
  async deleteGroup(groupId: number): Promise<void> {
    try {
      await this.groupApi.invoke(deleteGroup, { id: groupId });
      console.log('Group deleted successfully');
    } catch (error) {
      console.error('Error deleting group:', error);
      throw new Error('Failed to delete group. You may not have sufficient permissions.');
    }
  }

  /**
   * Searches groups by name, description, or subject
   * @param searchTerm - Term to search for
   * @returns Promise with matching groups
   */
  async searchGroups(searchTerm: string): Promise<GroupDto[]> {
    try {
      const result = await this.groupApi.invoke(searchGroups, { 
        body: { keyword: searchTerm } 
      });
      return result;
    } catch (error) {
      console.error('Error searching groups:', error);
      throw new Error('Search failed. Please try again.');
    }
  }

  // ========================================
  // MEMBER MANAGEMENT
  // ========================================

  /**
   * Gets all members of a specific group
   * @param groupId - ID of the group
   * @returns Promise with array of group members
   */
  async getGroupMembers(groupId: number): Promise<GroupMemberDto[]> {
    try {
      const result = await this.groupApi.invoke(getGroupMembers, { groupId });
      return result;
    } catch (error) {
      console.error('Error fetching group members:', error);
      throw new Error('Failed to load group members.');
    }
  }

  /**
   * Adds a new member to the group
   * @param groupId - ID of the group
   * @param userId - ID of the user to add
   * @returns Promise with operation result
   */
  async addMemberToGroup(groupId: number, userId: string): Promise<void> {
    try {
      await this.groupApi.invoke(addMember, { 
        groupId, 
        body: { userId } as AddMemberRequest 
      });
      console.log('Member added successfully');
    } catch (error) {
      console.error('Error adding member:', error);
      throw new Error('Failed to add member. User may already be in the group.');
    }
  }

  /**
   * Removes a member from the group
   * @param groupId - ID of the group
   * @param userId - ID of the user to remove
   * @returns Promise with operation result
   */
  async removeMemberFromGroup(groupId: number, userId: string): Promise<void> {
    try {
      await this.groupApi.invoke(removeMember, { groupId, userId });
      console.log('Member removed successfully');
    } catch (error) {
      console.error('Error removing member:', error);
      throw new Error('Failed to remove member. You may not have sufficient permissions.');
    }
  }

  /**
   * Leaves a group (for current user)
   * @param groupId - ID of the group to leave
   * @returns Promise with operation result
   */
  async leaveGroup(groupId: number): Promise<void> {
    try {
      const userId = this.keycloakService.userId;
      if (!userId) {
        throw new Error('User not authenticated');
      }
      
      await this.groupApi.invoke(leaveGroup, { groupId, userId });
      console.log('Left group successfully');
    } catch (error) {
      console.error('Error leaving group:', error);
      throw new Error('Failed to leave group.');
    }
  }

  /**
   * Designates a member as co-admin
   * @param groupId - ID of the group
   * @param userId - ID of the user to make co-admin
   * @returns Promise with operation result
   */
  async makeCoAdmin(groupId: number, userId: string): Promise<void> {
    try {
      await this.groupApi.invoke(designateCoAdmin, { groupId, userId });
      console.log('Co-admin designated successfully');
    } catch (error) {
      console.error('Error designating co-admin:', error);
      throw new Error('Failed to designate co-admin.');
    }
  }

  // ========================================
  // POST MANAGEMENT
  // ========================================

  /**
   * Creates a new post in the group
   * @param groupId - ID of the group
   * @param postData - Post content and metadata
   * @returns Promise with created post
   */
  async createPost(groupId: number, postData: { content: string; type: 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT' | 'LINK' }): Promise<GroupPostDto> {
    try {
      const result = await this.groupApi.invoke(createPost, { 
        groupId, 
        body: postData 
      });
      return result;
    } catch (error) {
      console.error('Error creating post:', error);
      throw new Error('Failed to create post.');
    }
  }

  /**
   * Gets all published posts in a group
   * @param groupId - ID of the group
   * @param page - Page number for pagination
   * @param size - Number of posts per page
   * @returns Promise with array of posts
   */
  async getGroupPosts(groupId: number, page: number = 0, size: number = 20): Promise<GroupPostDto[]> {
    try {
      const result = await this.groupApi.invoke(getPublishedPosts, { 
        groupId, 
        page, 
        size 
      });
      return result;
    } catch (error) {
      console.error('Error fetching group posts:', error);
      throw new Error('Failed to load group posts.');
    }
  }

  /**
   * Deletes a post
   * @param groupId - ID of the group
   * @param postId - ID of the post to delete
   * @returns Promise with operation result
   */
  async deletePost(groupId: number, postId: number): Promise<void> {
    try {
      await this.groupApi.invoke(deletePost, { groupId, postId });
      console.log('Post deleted successfully');
    } catch (error) {
      console.error('Error deleting post:', error);
      throw new Error('Failed to delete post. You may not have sufficient permissions.');
    }
  }

  // ========================================
  // FILE MANAGEMENT
  // ========================================

  /**
   * Uploads a file to the group
   * @param groupId - ID of the group
   * @param file - File to upload
   * @param content - Optional content for the post
   * @returns Promise with upload result
   */
  async uploadFile(groupId: number, file: File, content?: string): Promise<any> {
    try {
      const uploadRequest = {
        content: content || '',
        fileName: file.name
      };
      
      const result = await this.groupApi.invoke(uploadFile, { 
        groupId, 
        request: uploadRequest,
        body: { file: file }
      });
      return result;
    } catch (error) {
      console.error('Error uploading file:', error);
      throw new Error('Failed to upload file. Please check file size and format.');
    }
  }

  /**
   * Downloads a file from a group post
   * @param groupId - ID of the group
   * @param postId - ID of the post containing the file
   * @returns Promise with file data
   */
  async downloadFile(groupId: number, postId: number): Promise<Blob> {
    try {
      const result = await this.groupApi.invoke$Response(downloadFile, { 
        groupId, 
        postId 
      });
      return result.body;
    } catch (error) {
      console.error('Error downloading file:', error);
      throw new Error('Failed to download file.');
    }
  }

  // ========================================
  // STATISTICS & ANALYTICS
  // ========================================

  /**
   * Gets group statistics
   * @param groupId - ID of the group
   * @returns Promise with group statistics
   */
  async getGroupStatistics(groupId: number): Promise<GroupStatisticsDto> {
    try {
      const result = await this.groupApi.invoke(getGroupStatistics, { groupId });
      return result;
    } catch (error) {
      console.error('Error fetching group statistics:', error);
      throw new Error('Failed to load group statistics.');
    }
  }

  // ========================================
  // USER-SPECIFIC OPERATIONS
  // ========================================

  /**
   * Gets groups for a specific user
   * @param userId - ID of the user
   * @returns Promise with user's groups
   */
  async getUserGroups(userId: string): Promise<GroupDto[]> {
    try {
      const result = await this.groupApi.invoke(getGroupsByUserId, { userId });
      return result;
    } catch (error) {
      console.error('Error fetching user groups:', error);
      throw new Error('Failed to load user groups.');
    }
  }

  /**
   * Gets all students (for admin/teacher use)
   * @returns Promise with array of students
   */
  async getAllStudents(): Promise<any[]> {
    try {
      const result = await this.groupApi.invoke(getAllStudents, {});
      return result;
    } catch (error) {
      console.error('Error fetching students:', error);
      throw new Error('Failed to load students.');
    }
  }
}
