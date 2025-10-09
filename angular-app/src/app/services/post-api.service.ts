import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApiConfiguration } from '../post-services/api-configuration';
import { Post, CreatePostRequest, PagedModelPost, PagedModelComment, Vote, Comment, Like, Bookmark } from '../post-services/models';

// Import the generated API functions
import { createPost } from '../post-services/fn/post/create-post';
import { getMyPosts } from '../post-services/fn/post/get-my-posts';
import { deletePost } from '../post-services/fn/post/delete-post';
import { getPostsByGroupId } from '../post-services/fn/post/get-posts-by-group-id';
import { searchPosts } from '../post-services/fn/post/search-posts';
import { searchPostsInGroup } from '../post-services/fn/post/search-posts-in-group';
import { getTrendingPosts } from '../post-services/fn/post/get-trending-posts';
import { getBookmarkedPosts } from '../post-services/fn/post/get-bookmarked-posts';
import { toggleLike } from '../post-services/fn/post/toggle-like';
import { toggleBookmark } from '../post-services/fn/post/toggle-bookmark';
import { addComment } from '../post-services/fn/post/add-comment';
import { getComments } from '../post-services/fn/post/get-comments';
import { pinPost } from '../post-services/fn/post/pin-post';
import { updatePostStatus } from '../post-services/fn/post/update-post-status';
import { getMyPendingPosts } from '../post-services/fn/post/get-my-pending-posts';
import { votePost } from '../post-services/fn/post/vote-post';

@Injectable({
  providedIn: 'root'
})
export class PostApiService {
  private rootUrl: string;

  constructor(
    private http: HttpClient
  ) {
    const apiConfig = new ApiConfiguration();
    this.rootUrl = apiConfig.rootUrl;
  }

/**
 * Creates a new post (handles both text-only and posts with images)
 * @param data - Post data as CreatePostRequest OR FormData
 * @returns Created post
 */
async createPost(data: CreatePostRequest | FormData): Promise<Post> {
  try {
    if (data instanceof FormData) {
      // Handle FormData case
      const response = await firstValueFrom(
        this.http.post<Post>(`${this.rootUrl}/api/v1/posts`, data)
      );
      return response;
    } else {
      // Handle CreatePostRequest case
      const response = await firstValueFrom(
        createPost(this.http, this.rootUrl, { body: data })
      );
      return response.body;
    }
  } catch (error) {
    console.error('Error creating post:', error);
    throw error;
  }
}

  /**
   * Gets posts for the current user
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of posts
   */
  async getMyPosts(page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      getMyPosts(this.http, this.rootUrl, { page, size })
    );
    return response.body;
  }

  /**
   * Gets pending posts for the current user
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of pending posts
   */
  async getMyPendingPosts(page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      getMyPendingPosts(this.http, this.rootUrl, { page, size })
    );
    return response.body;
  }

  /**
   * Deletes a post
   * @param postId - Post ID
   */
  async deletePost(postId: number): Promise<void> {
    await firstValueFrom(
      deletePost(this.http, this.rootUrl, { 'post-id': postId })
    );
  }

  /**
   * Gets posts for a group
   * @param groupId - Group ID
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of posts
   */
  async getPostsByGroupId(groupId: string, page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      getPostsByGroupId(this.http, this.rootUrl, { 'group-id': groupId, page, size })
    );
    return response.body;
  }

  /**
   * Searches posts globally
   * @param keyword - Search keyword
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of posts
   */
  async searchPosts(keyword: string, page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      searchPosts(this.http, this.rootUrl, { query: keyword, page, size })
    );
    return response.body;
  }

  /**
   * Searches posts within a group
   * @param groupId - Group ID
   * @param keyword - Search keyword
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of posts
   */
  async searchPostsInGroup(groupId: string, keyword: string, page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      searchPostsInGroup(this.http, this.rootUrl, { 'group-id': groupId, query: keyword, page, size })
    );
    return response.body;
  }

  /**
   * Gets trending posts
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of trending posts
   */
  async getTrendingPosts(page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      getTrendingPosts(this.http, this.rootUrl, { page, size })
    );
    return response.body;
  }

  /**
   * Gets bookmarked posts for the current user
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of bookmarked posts
   */
  async getBookmarkedPosts(page?: number, size?: number): Promise<PagedModelPost> {
    const response = await firstValueFrom(
      getBookmarkedPosts(this.http, this.rootUrl, { page, size })
    );
    return response.body;
  }

  /**
   * Toggles like on a post
   * @param postId - Post ID
   * @returns Like object
   */
  async toggleLike(postId: number): Promise<Like> {
    const response = await firstValueFrom(
      toggleLike(this.http, this.rootUrl, { 'post-id': postId })
    );
    return response.body;
  }

  /**
   * Toggles bookmark on a post
   * @param postId - Post ID
   * @returns Bookmark object
   */
  async toggleBookmark(postId: number): Promise<Bookmark> {
    const response = await firstValueFrom(
      toggleBookmark(this.http, this.rootUrl, { 'post-id': postId })
    );
    return response.body;
  }

  /**
   * Adds a comment to a post
   * @param postId - Post ID
   * @param content - Comment content
   * @returns Comment object
   */
  async addComment(postId: number, content: string): Promise<Comment> {
    const response = await firstValueFrom(
      addComment(this.http, this.rootUrl, { 'post-id': postId, content })
    );
    return response.body;
  }

  /**
   * Gets comments for a post
   * @param postId - Post ID
   * @param page - Page number (optional)
   * @param size - Page size (optional)
   * @returns Page of comments
   */
  async getComments(postId: number, page?: number, size?: number): Promise<PagedModelComment> {
    const response = await firstValueFrom(
      getComments(this.http, this.rootUrl, { 'post-id': postId, page, size })
    );
    return response.body;
  }

  /**
   * Pins/unpins a post
   * @param postId - Post ID
   * @param pinned - Whether to pin or unpin the post
   * @returns Updated post
   */
  async pinPost(postId: number, pinned: boolean): Promise<Post> {
    const response = await firstValueFrom(
      pinPost(this.http, this.rootUrl, { 'post-id': postId, pinned })
    );
    return response.body;
  }

  /**
   * Updates post status
   * @param postId - Post ID
   * @param status - New status
   * @returns Updated post
   */
  async updatePostStatus(postId: number, status: 'PENDING' | 'APPROVED' | 'REJECTED'): Promise<Post> {
    const response = await firstValueFrom(
      updatePostStatus(this.http, this.rootUrl, { 'post-id': postId, status })
    );
    return response.body;
  }

  /**
   * Votes on a post
   * @param postId - Post ID
   * @param voteType - Vote type (UPVOTE or DOWNVOTE)
   * @returns Vote object
   */
  async votePost(postId: number, voteType: 'UPVOTE' | 'DOWNVOTE'): Promise<Vote> {
    const response = await firstValueFrom(
      votePost(this.http, this.rootUrl, { 'post-id': postId, upvote: voteType === 'UPVOTE' })
    );
    return response.body;
  }
}