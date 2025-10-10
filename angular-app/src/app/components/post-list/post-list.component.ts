import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostApiService } from '../../services/post-api.service';
import { Post, CreatePostRequest, PagedModelPost, Comment } from '../../post-services/models';
import { PagedModelCommentResponse } from '../../post-services/models/paged-model-comment-response';
import { CommentResponse } from '../../post-services/models/comment-response';
import { LikeResponse } from '../../post-services/models/like-response';
import { BookmarkResult } from '../../post-services/models/bookmark-result';
import { KeycloakService } from '../../utils/keycloak/KeycloakService';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent implements OnInit {
  // ========================================
  // COMPONENT STATE & DATA
  // ========================================
  
  /** All available posts */
  myPosts: Post[] = [];
  posts: Post[] = [];
  
  /** New post content being composed */
  newPostContent: string = '';
  
  /** Loading states for different operations */
  isLoadingPosts = false;
  isSendingPost = false;
  
  /** UI state flags */
  showEmojiPicker = false;
  
  /** Pagination */
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  
  /** Search functionality */
  searchTerm: string = '';
  showSearchResults = false;
  
  /** File handling */
  selectedFile: File | null = null;
  selectedFilePreview: string | null = null;
  selectedFileName: string = '';
  
  /** Comments */
  comments: { [key: number]: CommentResponse[] } = {};
  newComments: { [key: number]: string } = {};
  
  /** Post status */
  postStatusFilter: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' = 'ALL';
  
  @ViewChild('fileInput') fileInput!: ElementRef;
  
  // ========================================
  // CONSTRUCTOR & LIFECYCLE
  // ========================================
  
  constructor(
    public keycloakService: KeycloakService,
    private postApiService: PostApiService
  ) {}
  
  ngOnInit(): void {
    this.loadAllPosts();
  }
  
  // ========================================
  // FILE HANDLING
  // ========================================
  
  /**
   * Handles file selection event
   * @param event - File input change event
   */
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      // Check if the file is an image
      if (!file.type.startsWith('image/')) {
        this.showError('Please select an image file (JPEG, PNG, GIF, etc.)');
        return;
      }
      
      // Check file size (optional, but good for UX)
      const maxSize = 1000 * 1024 * 1024; // 1000MB in bytes
      if (file.size > maxSize) {
        this.showError(`File size exceeds ${maxSize / (1024 * 1024)}MB limit`);
        return;
      }
      
      this.selectedFile = file;
      this.selectedFileName = file.name;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.selectedFilePreview = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }
  
  /**
   * Removes the selected file
   */
  removeSelectedFile(): void {
    this.selectedFile = null;
    this.selectedFilePreview = null;
    this.selectedFileName = '';
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }
  
  // ========================================
  // POST MANAGEMENT
  // ========================================

  /**
   * Loads All Posts
   */
  async loadAllPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getAllPosts(this.currentPage, this.pageSize);
      this.posts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    }
    catch (error) {
      console.error('Error loading posts:', error);
      this.showError('Failed to load posts');
    }
    finally {
      this.isLoadingPosts = false;
    }
  }

  /**
   * Loads posts for the current user
   */
  async loadMyPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getMyPosts(this.currentPage, this.pageSize);
      console.log('Page Data:', pageData);
      this.myPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    } catch (error) {
      console.error('Error loading posts:', error);
      this.showError('Failed to load posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }

  /**
   * Loads pending posts for the current user
   */
  async loadMyPendingPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getMyPendingPosts(this.currentPage, this.pageSize);
      this.myPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    } catch (error) {
      console.error('Error loading pending posts:', error);
      this.showError('Failed to load pending posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }

  /**
   * Loads trending posts
   */
  async loadTrendingPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getTrendingPosts(this.currentPage, this.pageSize);
      this.posts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    } catch (error) {
      console.error('Error loading trending posts:', error);
      this.showError('Failed to load trending posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }

  /**
   * Loads bookmarked posts
   */
  async loadBookmarkedPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getBookmarkedPosts(this.currentPage, this.pageSize);
      this.posts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    } catch (error) {
      console.error('Error loading bookmarked posts:', error);
      this.showError('Failed to load bookmarked posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }

  /**
   * Searches posts
   */
  async searchPosts(): Promise<void> {
    if (!this.searchTerm.trim()) {
      await this.loadAllPosts();
      return;
    }
    
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.searchPosts(this.searchTerm, this.currentPage, this.pageSize);
      this.posts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
    } catch (error) {
      console.error('Error searching posts:', error);
      this.showError('Failed to search posts');
    } finally {
      this.isLoadingPosts = false;
    }
  }
  
  /**
   * Sends a new post
   */
  async sendPost(): Promise<void> {
    if (!this.newPostContent.trim() && !this.selectedFile) {
      return;
    }
    
    this.isSendingPost = true;
    try {
      let newPost: Post;
      
      if (this.selectedFile) {
        // Check file size before upload
        const maxSize = 1000 * 1024 * 1024; // 1000MB in bytes
        if (this.selectedFile.size > maxSize) {
          this.showError(`File size exceeds ${maxSize / (1024 * 1024)}MB limit`);
          this.isSendingPost = false;
          return;
        }
        
        // Create FormData for file upload
        const formData = new FormData();
        formData.append('content', this.newPostContent.trim() || '');
        formData.append('imageFile', this.selectedFile);
        
        // Use FormData for file uploads
        newPost = await this.postApiService.createPost(formData);
      } else {
        // Create post data for text-only posts
        const postData: CreatePostRequest = {
          content: this.newPostContent.trim() || ''
        };
        
        // Use CreatePostRequest for text-only posts
        newPost = await this.postApiService.createPost(postData);
      }
      
      // Add to local posts array at the beginning
      this.posts.unshift(newPost);
      
      // Reset form
      this.newPostContent = '';
      this.selectedFile = null;
      this.selectedFilePreview = null;
      this.selectedFileName = '';
      if (this.fileInput) {
        this.fileInput.nativeElement.value = '';
      }
    } catch (error) {
      console.error('Error sending post:', error);
      this.showError('Failed to send post');
    } finally {
      this.isSendingPost = false;
    }
  }
  
  /**
   * Deletes a post
   * @param postId - ID of post to delete
   */
  async deletePost(postId: number): Promise<void> {
    try {
      await this.postApiService.deletePost(postId);
      // Remove from local array
      this.posts = this.posts.filter(post => post.id !== postId);
      this.myPosts = this.myPosts.filter(post => post.id !== postId);
    } catch (error) {
      console.error('Error deleting post:', error);
      this.showError('Failed to delete post');
    }
  }
  
  /**
   * Toggles like status for a post
   * @param postId - ID of post to like/unlike
   */
  async toggleLike(postId: number): Promise<void> {
    try {
      const response: LikeResponse = await this.postApiService.toggleLike(postId);
      const post = this.posts.find(p => p.id === postId) || this.myPosts.find(p => p.id === postId);
      
      if (post) {
        // Update based on the actual response
        if (response.action === 'LIKED') {
          post.likeCount = response.likeCount;
          // You might want to track which posts are liked by the user
        } else if (response.action === 'UNLIKED') {
          post.likeCount = response.likeCount;
        }
      }
    } catch (error) {
      console.error('Error toggling like:', error);
      this.showError('Failed to like post');
    }
  }
  
  /**
   * Toggles bookmark status for a post
   * @param postId - ID of post to bookmark/unbookmark
   */
  async toggleBookmark(postId: number): Promise<void> {
    try {
      const response: BookmarkResult = await this.postApiService.toggleBookmark(postId);
      const post = this.posts.find(p => p.id === postId) || this.myPosts.find(p => p.id === postId);
      
      if (post) {
        // Update based on the actual response
        if (response.action === 'BOOKMARKED') {
          post.bookmarkCount = response.bookmarkCount;
          // You might want to track which posts are bookmarked by the user
        } else if (response.action === 'UNBOOKMARKED') {
          post.bookmarkCount = response.bookmarkCount;
        }
      }
    } catch (error) {
      console.error('Error toggling bookmark:', error);
      this.showError('Failed to bookmark post');
    }
  }
  
  /**
   * Pins/unpins a post
   * @param postId - ID of post to pin/unpin
   * @param pinned - Whether to pin or unpin
   */
  async pinPost(postId: number, pinned: boolean): Promise<void> {
    try {
      const updatedPost: Post = await this.postApiService.pinPost(postId, pinned);
      const post = this.posts.find(p => p.id === postId) || this.myPosts.find(p => p.id === postId);
      
      if (post) {
        // Update the pinned status
        post.pinned = updatedPost.pinned;
      }
    } catch (error) {
      console.error('Error pinning post:', error);
      this.showError(`Failed to ${pinned ? 'pin' : 'unpin'} post`);
    }
  }
  
  /**
   * Updates post status
   * @param postId - ID of post to update
   * @param status - New status
   */
  async updatePostStatus(postId: number, status: 'PENDING' | 'APPROVED' | 'REJECTED'): Promise<void> {
    try {
      const updatedPost: Post = await this.postApiService.updatePostStatus(postId, status);
      const post = this.posts.find(p => p.id === postId) || this.myPosts.find(p => p.id === postId);
      
      if (post) {
        // Update the status
        post.status = updatedPost.status;
      }
      
      // If we're filtering by status, we might want to refresh the list
      if (this.postStatusFilter !== 'ALL' && this.postStatusFilter !== status) {
        // Remove from current list if it no longer matches the filter
        this.posts = this.posts.filter(p => p.id !== postId);
        this.myPosts = this.myPosts.filter(p => p.id !== postId);
      }
    } catch (error) {
      console.error('Error updating post status:', error);
      this.showError('Failed to update post status');
    }
  }
  
  /**
   * Checks if a post is liked by the current user
   * @param postId - ID of post to check
   */
  async checkIfPostLiked(postId: number): Promise<boolean> {
    try {
      return await this.postApiService.isPostLikedByUser(postId);
    } catch (error) {
      console.error('Error checking if post is liked:', error);
      return false;
    }
  }
  
  /**
   * Checks if a post is bookmarked by the current user
   * @param postId - ID of post to check
   */
  async checkIfPostBookmarked(postId: number): Promise<boolean> {
    try {
      return await this.postApiService.isPostBookmarkedByUser(postId);
    } catch (error) {
      console.error('Error checking if post is bookmarked:', error);
      return false;
    }
  }
  
  // ========================================
  // COMMENT MANAGEMENT
  // ========================================
  
  /**
   * Adds a comment to a post
   * @param postId - ID of post to comment on
   */
  async addComment(postId: number): Promise<void> {
    const commentContent = this.newComments[postId];
    if (!commentContent?.trim()) {
      return;
    }
    
    try {
      const newComment: CommentResponse = await this.postApiService.addComment(postId, commentContent.trim());
      
      // Add to local comments
      if (!this.comments[postId]) {
        this.comments[postId] = [];
      }
      this.comments[postId].push(newComment);
      
      // Clear the input
      this.newComments[postId] = '';
    } catch (error) {
      console.error('Error adding comment:', error);
      this.showError('Failed to add comment');
    }
  }
  
  /**
   * Loads paginated comments for a post
   * @param postId - ID of post to load comments for
   */
  async loadCommentsPaginated(postId: number): Promise<void> {
    try {
      const pageData: PagedModelCommentResponse = await this.postApiService.getCommentsPaginated(postId, 0, 10);
      this.comments[postId] = pageData.content || [];
    } catch (error) {
      console.error('Error loading comments:', error);
      this.showError('Failed to load comments');
    }
  }
  
  /**
   * Loads comments with replies for a post
   * @param postId - ID of post to load comments for
   */
  async loadCommentsWithReplies(postId: number): Promise<void> {
    try {
      const commentsWithReplies: CommentResponse[] = await this.postApiService.getCommentsWithReplies(postId);
      this.comments[postId] = commentsWithReplies;
    } catch (error) {
      console.error('Error loading comments with replies:', error);
      this.showError('Failed to load comments');
    }
  }
  
  // ========================================
  // UTILITY METHODS
  // ========================================
  
  /**
   * Formats a date string for display
   * @param dateString - Date string to format
   * @returns Formatted date string
   */
  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
  
  /**
   * Shows an error message to the user
   * @param message - Error message to display
   */
  showError(message: string): void {
    // In a real app, you would use a proper notification service
    alert(message);
  }
  
  /**
   * TrackBy function for posts list
   * @param index - Index of item
   * @param post - Post object
   * @returns Unique identifier for post
   */
  trackById(index: number, post: Post): number | undefined {
    return post.id;
  }
  
  /**
   * Gets the full image URL with the correct base URL
   * @param imageUrl - Relative image URL
   * @returns Full image URL
   */
  getFullImageUrl(imageUrl: string | undefined): string | undefined {
    if (!imageUrl) {
      return undefined;
    }
    
    // If it's already an absolute URL, return as is
    if (imageUrl.startsWith('http')) {
      return imageUrl;
    }
    
    // If it's a relative URL, prepend the post-service base URL
    const postServiceBaseUrl = 'http://localhost:8083';
    return postServiceBaseUrl + imageUrl;
  }
  
  /**
   * Sets the post status filter and reloads posts
   * @param status - Status to filter by
   */
  setPostStatusFilter(status: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'): void {
    this.postStatusFilter = status;
    // You might want to implement actual filtering here
    // For now, we'll just reload all posts
    this.loadAllPosts();
  }
}