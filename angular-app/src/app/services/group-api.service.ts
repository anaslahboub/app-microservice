import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiConfiguration } from '../group-services/api-configuration';
import { GroupDto, GroupMemberDto, GroupPostDto } from '../group-services/models';
import { UserResponse } from '../chat-services/models';

// Import the generated API functions
import { getGroupsByUserId } from '../group-services/fn/group-controller/get-groups-by-user-id';
import { getGroupMembers } from '../group-services/fn/group-member-controller/get-group-members';
import { getStudentsByGroupId } from '../group-services/fn/group-member-controller/get-students-by-group-id';
import { getAllStudents } from '../group-services/fn/student-controller/get-all-students';
import { searchGroups } from '../group-services/fn/group-controller/search-groups';
import { getPublishedPosts } from '../group-services/fn/group-post-controller/get-published-posts';
import { createPost } from '../group-services/fn/group-post-controller/create-post';
import { deletePost } from '../group-services/fn/group-post-controller/delete-post';
import { addMember } from '../group-services/fn/group-member-controller/add-member';
import { removeMember } from '../group-services/fn/group-member-controller/remove-member';
import { designateCoAdmin } from '../group-services/fn/group-member-controller/designate-co-admin';
import { archiveGroup } from '../group-services/fn/group-controller/archive-group';
import { deleteGroup } from '../group-services/fn/group-controller/delete-group';
import { uploadFile } from '../group-services/fn/group-post-controller/upload-file';
import { downloadFile } from '../group-services/fn/group-post-controller/download-file';
import { createGroup } from '../group-services/fn/group-controller/create-group';

@Injectable({
  providedIn: 'root'
})
export class GroupApiService {
  private rootUrl: string;

  constructor(
    private http: HttpClient,
    private apiConfig: ApiConfiguration
  ) {
    this.rootUrl = this.apiConfig.rootUrl;
  }

  /**
   * Gets all groups for a user
   * @param userId - User ID
   * @returns Array of groups
   */
  async getUserGroups(userId: string): Promise<GroupDto[]> {
    const response = await firstValueFrom(
      getGroupsByUserId(this.http, this.rootUrl, { userId })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupDto[];
  }

  /**
   * Gets members of a group
   * @param groupId - Group ID
   * @returns Array of group members
   */
  async getGroupMembers(groupId: number): Promise<GroupMemberDto[]> {
    const response = await firstValueFrom(
      getGroupMembers(this.http, this.rootUrl, { groupId })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupMemberDto[];
  }

  /**
   * Creates a new group
   * @param groupData - Group data
   * @returns Created group
   */
  async createGroup(groupData: any): Promise<GroupDto> {
    const response = await firstValueFrom(
      createGroup(this.http, this.rootUrl, { body: groupData })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupDto;
  }

  /**
   * Gets available users that can be added to a group
   * @param groupId - Group ID
   * @returns Array of available users
   */
  async getAvailableUsersForGroup(groupId: number): Promise<UserResponse[]> {
    const response = await firstValueFrom(
      getStudentsByGroupId(this.http, this.rootUrl, { groupId })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as UserResponse[];
  }

  /**
   * Gets all students
   * @returns Array of all students
   */
  async getAllStudents(): Promise<UserResponse[]> {
    const response = await firstValueFrom(
      getAllStudents(this.http, this.rootUrl)
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as UserResponse[];
  }

  /**
   * Searches for groups by term
   * @param term - Search term
   * @returns Array of matching groups
   */
  async searchGroups(term: string): Promise<GroupDto[]> {
    const response = await firstValueFrom(
      searchGroups(this.http, this.rootUrl, { body: { keyword: term } })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupDto[];
  }

  /**
   * Gets posts for a group
   * @param groupId - Group ID
   * @returns Array of group posts
   */
  async getGroupPosts(groupId: number): Promise<GroupPostDto[]> {
    const response = await firstValueFrom(
      getPublishedPosts(this.http, this.rootUrl, { groupId })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupPostDto[];
  }

  /**
   * Creates a post in a group
   * @param groupId - Group ID
   * @param postData - Post data
   * @returns Created post
   */
  async createPost(groupId: number, postData: any): Promise<GroupPostDto> {
    const response = await firstValueFrom(
      createPost(this.http, this.rootUrl, { groupId, body: postData })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text) as GroupPostDto;
  }

  /**
   * Deletes a post from a group
   * @param groupId - Group ID
   * @param postId - Post ID
   */
  async deletePost(groupId: number, postId: number): Promise<void> {
    await firstValueFrom(
      deletePost(this.http, this.rootUrl, { groupId, postId })
    );
  }

  /**
   * Adds a member to a group
   * @param groupId - Group ID
   * @param userId - User ID
   */
  async addMemberToGroup(groupId: number, userId: string): Promise<void> {
    await firstValueFrom(
      addMember(this.http, this.rootUrl, { groupId, body: { userId } })
    );
  }

  /**
   * Removes a member from a group
   * @param groupId - Group ID
   * @param userId - User ID
   */
  async removeMemberFromGroup(groupId: number, userId: string): Promise<void> {
    await firstValueFrom(
      removeMember(this.http, this.rootUrl, { groupId, userId })
    );
  }

  /**
   * Makes a user co-admin of a group
   * @param groupId - Group ID
   * @param userId - User ID
   */
  async makeCoAdmin(groupId: number, userId: string): Promise<void> {
    await firstValueFrom(
      designateCoAdmin(this.http, this.rootUrl, { groupId, userId })
    );
  }

  /**
   * Archives a group
   * @param groupId - Group ID
   */
  async archiveGroup(groupId: number): Promise<void> {
    await firstValueFrom(
      archiveGroup(this.http, this.rootUrl, { id: groupId })
    );
  }

  /**
   * Deletes a group
   * @param groupId - Group ID
   */
  async deleteGroup(groupId: number): Promise<void> {
    await firstValueFrom(
      deleteGroup(this.http, this.rootUrl, { id: groupId })
    );
  }

  /**
   * Uploads a file to a group
   * @param groupId - Group ID
   * @param file - File to upload
   * @returns Upload result
   */
  async uploadFile(groupId: number, file: File): Promise<any> {
    const response = await firstValueFrom(
      uploadFile(this.http, this.rootUrl, { 
        groupId, 
        request: { fileName: file.name },
        body: { file } 
      })
    );
    const text = await (response.body as unknown as Blob).text();
    return JSON.parse(text);
  }

  /**
   * Downloads a file from a group post
   * @param groupId - Group ID
   * @param postId - Post ID
   * @returns File blob
   */
  async downloadFile(groupId: number, postId: number): Promise<Blob> {
    const response = await firstValueFrom(
      downloadFile(this.http, this.rootUrl, { groupId, postId })
    );
    return response.body;
  }

  /**
   * Gets all users (for admin purposes)
   * @returns Array of all users
   */
  async getAllUsers(): Promise<UserResponse[]> {
    // This is a placeholder implementation
    // In a real app, you would call the appropriate API endpoint
    return this.getAllStudents();
  }
}