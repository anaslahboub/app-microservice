import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostApiService } from '../../services/post-api.service';
import { Post, CreatePostRequest, PagedModelPost, Comment, CommentResponse } from '../../services/post-services/models';
import { PagedModelCommentResponse } from '../../services/post-services/models/paged-model-comment-response';
import { LikeResponse } from '../../services/post-services/models/like-response';
import { BookmarkResult } from '../../services/post-services/models/bookmark-result';
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
  // COMPONENT STATE & DATA
  // ========================================
  
  /** Different post collections */
  myPosts: Post[] = [];
  allPosts: Post[] = [];
  bookmarkedPosts: Post[] = [];
  trendingPosts: Post[] = [];
  
  /** Current view state */
  currentView: 'ALL' | 'MY_POSTS' | 'BOOKMARKED' | 'TRENDING' = 'ALL';
  
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
  
  /** Caches for like/bookmark status to prevent excessive API calls */
  private likedPostsCache: Map<number, boolean> = new Map();
  private bookmarkedPostsCache: Map<number, boolean> = new Map();
  private cacheExpiry: Map<number, number> = new Map(); // Timestamps for cache expiry
  private readonly CACHE_DURATION = 30000; // 30 seconds cache duration
  
  /** Track pending requests to prevent duplicate calls */
  private pendingRequests: Map<string, Promise<any>> = new Map();
  
  @ViewChild('fileInput') fileInput!: ElementRef;
  
  // ========================================
  // CONSTRUCTOR & LIFECYCLE
  // ========================================
  
  constructor(
    public keycloakService: KeycloakService,
    private postApiService: PostApiService
  ) {}
  
  ngOnInit(): void {
    console.log('Initializing component, currentView:', this.currentView);
    this.loadAllPosts();
    console.log('After loadAllPosts, currentView:', this.currentView);
  }
  
  // ========================================
  // VIEW MANAGEMENT
  // ========================================
  
  /**
   * Getter for currently displayed posts based on view
   */
  get displayedPosts(): Post[] {
    switch (this.currentView) {
      case 'MY_POSTS':
        return this.myPosts;
      case 'BOOKMARKED':
        return this.bookmarkedPosts;
      case 'TRENDING':
        return this.trendingPosts;
      default:
        return this.allPosts;
    }
  }
  
  /**
   * Sets the current view and loads appropriate posts if needed
   */
  setView(view: 'ALL' | 'MY_POSTS' | 'BOOKMARKED' | 'TRENDING'): void {
    console.log('Setting view from', this.currentView, 'to', view);
    // Only reload if changing to a different view
    if (this.currentView !== view) {
      this.currentView = view;
      console.log('View set to', this.currentView);
      this.currentPage = 0; // Reset to first page when changing views
      this.loadPostsForCurrentView();
    }
  }

  /**
   * Loads All Posts
   */
  async loadAllPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getAllPosts(this.currentPage, this.pageSize);
      this.allPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.allPosts);
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
      this.myPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.myPosts);
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
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.myPosts);
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
      this.trendingPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.trendingPosts);
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
      this.bookmarkedPosts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.bookmarkedPosts);
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
      await this.loadPostsForCurrentView();
      return;
    }
    
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.searchPosts(this.searchTerm, this.currentPage, this.pageSize);
      
      // Search results go to the current view
      switch (this.currentView) {
        case 'MY_POSTS':
          this.myPosts = pageData.content || [];
          break;
        case 'BOOKMARKED':
          this.bookmarkedPosts = pageData.content || [];
          break;
        case 'TRENDING':
          this.trendingPosts = pageData.content || [];
          break;
        default:
          this.allPosts = pageData.content || [];
      }
      
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      
      // Preload like/bookmark status for all posts
      this.preloadPostStatuses(this.displayedPosts);
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
      
      // Add to the appropriate list based on current view
      switch (this.currentView) {
        case 'MY_POSTS':
          this.myPosts.unshift(newPost);
          break;
        case 'BOOKMARKED':
          this.bookmarkedPosts.unshift(newPost);
          break;
        case 'TRENDING':
          this.trendingPosts.unshift(newPost);
          break;
        default:
          this.allPosts.unshift(newPost);
      }
      
      // Also add to myPosts if we're not already in My Posts view
      if (this.currentView !== 'MY_POSTS' && !this.myPosts.some(p => p.id === newPost.id)) {
        this.myPosts.unshift(newPost);
      }
      
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
      // Remove from all lists
      this.allPosts = this.allPosts.filter(post => post.id !== postId);
      this.myPosts = this.myPosts.filter(post => post.id !== postId);
      this.bookmarkedPosts = this.bookmarkedPosts.filter(post => post.id !== postId);
      this.trendingPosts = this.trendingPosts.filter(post => post.id !== postId);
      
      // Clear cache entries
      this.likedPostsCache.delete(postId);
      this.bookmarkedPostsCache.delete(postId);
      this.cacheExpiry.delete(postId);
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
    // Prevent multiple simultaneous requests for the same post
    const requestKey = `like_${postId}`;
    if (this.pendingRequests.has(requestKey)) {
      return; // Already processing a request for this post
    }
    
    try {
      // Create and store the promise
      const likePromise = this.postApiService.toggleLike(postId);
      this.pendingRequests.set(requestKey, likePromise);
      
      const response: LikeResponse = await likePromise;
      const post = this.displayedPosts.find(p => p.id === postId) || 
                   this.allPosts.find(p => p.id === postId) || 
                   this.myPosts.find(p => p.id === postId) || 
                   this.bookmarkedPosts.find(p => p.id === postId) || 
                   this.trendingPosts.find(p => p.id === postId);
      
      if (post) {
        // Update based on the actual response from the toggle API
        post.likeCount = response.likeCount;
        
        // Update cache based on the action performed
        if (response.action === 'liked') {
          this.likedPostsCache.set(postId, true);
        } else if (response.action === 'unliked') {
          this.likedPostsCache.set(postId, false);
        }
      }
      
      // Update cache expiry
      this.cacheExpiry.set(postId, Date.now() + this.CACHE_DURATION);
    } catch (error) {
      console.error('Error toggling like:', error);
      this.showError('Failed to like post');
    } finally {
      // Remove the pending request
      this.pendingRequests.delete(requestKey);
    }
  }
  
  /**
   * Toggles bookmark status for a post
   * @param postId - ID of post to bookmark/unbookmark
   */
  async toggleBookmark(postId: number): Promise<void> {
    // Prevent multiple simultaneous requests for the same post
    const requestKey = `bookmark_${postId}`;
    if (this.pendingRequests.has(requestKey)) {
      return; // Already processing a request for this post
    }
    
    try {
      // Create and store the promise
      const bookmarkPromise = this.postApiService.toggleBookmark(postId);
      this.pendingRequests.set(requestKey, bookmarkPromise);
      
      const response: BookmarkResult = await bookmarkPromise;
      const post = this.displayedPosts.find(p => p.id === postId) || 
                   this.allPosts.find(p => p.id === postId) || 
                   this.myPosts.find(p => p.id === postId) || 
                   this.bookmarkedPosts.find(p => p.id === postId) || 
                   this.trendingPosts.find(p => p.id === postId);
      
      if (post) {
        // Update based on the actual response from the toggle API
        post.bookmarkCount = response.bookmarkCount;
        
        // Update cache based on the action performed
        if (response.action === 'bookmarked') {
          this.bookmarkedPostsCache.set(postId, true);
        } else if (response.action === 'unbookmarked') {
          this.bookmarkedPostsCache.set(postId, false);
          
          // If we're currently viewing bookmarks, remove the post from the bookmarked list
          if (this.currentView === 'BOOKMARKED') {
            this.bookmarkedPosts = this.bookmarkedPosts.filter(p => p.id !== postId);
          }
        }
      }
      
      // Update cache expiry
      this.cacheExpiry.set(postId, Date.now() + this.CACHE_DURATION);
    } catch (error) {
      console.error('Error toggling bookmark:', error);
      this.showError('Failed to bookmark post');
    } finally {
      // Remove the pending request
      this.pendingRequests.delete(requestKey);
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
      
      // Update in all lists where this post exists
      const allLists = [this.allPosts, this.myPosts, this.bookmarkedPosts, this.trendingPosts];
      for (const list of allLists) {
        const post = list.find(p => p.id === postId);
        if (post) {
          post.pinned = updatedPost.pinned;
        }
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
      
      // Update in all lists where this post exists
      const allLists = [this.allPosts, this.myPosts, this.bookmarkedPosts, this.trendingPosts];
      for (const list of allLists) {
        const post = list.find(p => p.id === postId);
        if (post) {
          post.status = updatedPost.status;
        }
      }
      
      // If we're filtering by status, we might want to refresh the list
      if (this.postStatusFilter !== 'ALL' && this.postStatusFilter !== status) {
        // Remove from current list if it no longer matches the filter
        this.allPosts = this.allPosts.filter(p => p.id !== postId);
        this.myPosts = this.myPosts.filter(p => p.id !== postId);
        this.bookmarkedPosts = this.bookmarkedPosts.filter(p => p.id !== postId);
        this.trendingPosts = this.trendingPosts.filter(p => p.id !== postId);
      }
    } catch (error) {
      console.error('Error updating post status:', error);
      this.showError('Failed to update post status');
    }
  }
  
  /**
   * Checks if a post is liked by the current user (with caching)
   * @param postId - ID of post to check
   */
  isPostLiked(postId: number): boolean {
    // Check if cache is valid
    if (this.isCacheValid(postId)) {
      return this.likedPostsCache.get(postId) || false;
    }
    
    // Cache miss or expired - return false and trigger async load
    this.loadPostLikeStatusWithDeduplication(postId);
    return false;
  }
  
  /**
   * Checks if a post is bookmarked by the current user (with caching)
   * @param postId - ID of post to check
   */
  isPostBookmarked(postId: number): boolean {
    // Check if cache is valid
    if (this.isCacheValid(postId)) {
      return this.bookmarkedPostsCache.get(postId) || false;
    }
    
    // Cache miss or expired - return false and trigger async load
    this.loadPostBookmarkStatusWithDeduplication(postId);
    return false;
  }
  
  /**
   * Checks if cache is valid for a post
   * @param postId - ID of post to check
   */
  private isCacheValid(postId: number): boolean {
    const expiry = this.cacheExpiry.get(postId);
    return expiry !== undefined && Date.now() < expiry;
  }
  
  /**
   * Loads like status for a post (async) with deduplication
   * @param postId - ID of post to check
   */
  private async loadPostLikeStatusWithDeduplication(postId: number): Promise<void> {
    // Prevent multiple simultaneous requests for the same post status
    const requestKey = `load_like_${postId}`;
    if (this.pendingRequests.has(requestKey)) {
      return; // Already processing a request for this post status
    }
    
    try {
      // Create and store the promise
      const likeStatusPromise = this.postApiService.isPostLikedByUser(postId);
      this.pendingRequests.set(requestKey, likeStatusPromise);
      
      const isLiked = await likeStatusPromise;
      this.likedPostsCache.set(postId, isLiked);
      this.cacheExpiry.set(postId, Date.now() + this.CACHE_DURATION);
    } catch (error) {
      console.error('Error checking if post is liked:', error);
      // Don't cache on error
      this.likedPostsCache.delete(postId);
      this.cacheExpiry.delete(postId);
    } finally {
      // Remove the pending request
      this.pendingRequests.delete(requestKey);
    }
  }
  
  /**
   * Loads bookmark status for a post (async) with deduplication
   * @param postId - ID of post to check
   */
  private async loadPostBookmarkStatusWithDeduplication(postId: number): Promise<void> {
    // Prevent multiple simultaneous requests for the same post status
    const requestKey = `load_bookmark_${postId}`;
    if (this.pendingRequests.has(requestKey)) {
      return; // Already processing a request for this post status
    }
    
    try {
      // Create and store the promise
      const bookmarkStatusPromise = this.postApiService.isPostBookmarkedByUser(postId);
      this.pendingRequests.set(requestKey, bookmarkStatusPromise);
      
      const isBookmarked = await bookmarkStatusPromise;
      this.bookmarkedPostsCache.set(postId, isBookmarked);
      this.cacheExpiry.set(postId, Date.now() + this.CACHE_DURATION);
    } catch (error) {
      console.error('Error checking if post is bookmarked:', error);
      // Don't cache on error
      this.bookmarkedPostsCache.delete(postId);
      this.cacheExpiry.delete(postId);
    } finally {
      // Remove the pending request
      this.pendingRequests.delete(requestKey);
    }
  }
  
  /**
   * Preloads like/bookmark status for multiple posts
   * @param posts - Array of posts to preload status for
   */
  private async preloadPostStatuses(posts: Post[]): Promise<void> {
    // Clear expired cache entries
    this.clearExpiredCache();
    
    // Get posts that need status checking (not in cache or cache expired)
    const postsToCheck = posts.filter(post => post.id !== undefined && !this.isCacheValid(post.id!));
    
    // Load statuses in parallel with deduplication
    const likePromises = postsToCheck.map(post => 
      this.loadPostLikeStatusWithDeduplication(post.id!).catch(() => {}) // Ignore errors for individual posts
    );
    
    const bookmarkPromises = postsToCheck.map(post => 
      this.loadPostBookmarkStatusWithDeduplication(post.id!).catch(() => {}) // Ignore errors for individual posts
    );
    
    // Wait for all requests to complete
    await Promise.all([...likePromises, ...bookmarkPromises]);
  }
  
  /**
   * Clears expired cache entries
   */
  private clearExpiredCache(): void {
    const now = Date.now();
    for (const [postId, expiry] of this.cacheExpiry.entries()) {
      if (now >= expiry) {
        this.likedPostsCache.delete(postId);
        this.bookmarkedPostsCache.delete(postId);
        this.cacheExpiry.delete(postId);
      }
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
      
      // Initialize comments array for this post if it doesn't exist
      if (!this.comments[postId]) {
        this.comments[postId] = [];
      }
      
      // Add to local comments at the beginning of the array
      this.comments[postId].unshift(newComment);
      
      // Clear the input
      this.newComments[postId] = '';
      
      // Update post comment count in all lists
      const allLists = [this.allPosts, this.myPosts, this.bookmarkedPosts, this.trendingPosts];
      for (const list of allLists) {
        const post = list.find(p => p.id === postId);
        if (post) {
          post.commentCount = (post.commentCount || 0) + 1;
        }
      }
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
      
      // Initialize comments array for this post if it doesn't exist
      if (!this.comments[postId]) {
        this.comments[postId] = [];
      }
      
      // Replace comments with the new paginated data
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
      
      // Initialize comments array for this post if it doesn't exist
      if (!this.comments[postId]) {
        this.comments[postId] = [];
      }
      
      // Update the specific comments that have replies
      // We need to merge the replies with existing comments
      const existingComments = this.comments[postId];
      
      // For each comment with replies, update the existing comment or add it if it doesn't exist
      commentsWithReplies.forEach(commentWithReplies => {
        const existingIndex = existingComments.findIndex(c => c.id === commentWithReplies.id);
        if (existingIndex !== -1) {
          // Update existing comment with replies
          existingComments[existingIndex] = commentWithReplies;
        } else {
          // Add new comment with replies
          existingComments.push(commentWithReplies);
        }
      });
      
      // Update the comments array
      this.comments[postId] = [...existingComments];
    } catch (error) {
      console.error('Error loading comments with replies:', error);
      this.showError('Failed to load comments');
    }
  }
  
  /**
   * Toggles comment visibility for a post
   * @param postId - ID of post to toggle comments for
   */
  toggleComments(postId: number): void {
    // If comments are not loaded yet, load them
    if (!this.comments[postId]) {
      this.loadCommentsPaginated(postId);
    }
  }
  
  /**
   * Shows replies for a specific comment
   * @param postId - ID of post
   * @param commentId - ID of comment to load replies for
   */
  async showReplies(postId: number, commentId: number): Promise<void> {
    try {
      // Load all comments with replies for this post
      await this.loadCommentsWithReplies(postId);
    } catch (error) {
      console.error('Error loading replies:', error);
      this.showError('Failed to load replies');
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
   * Checks if the delete button should be visible for a post
   * @param postId - ID of the post
   * @returns boolean indicating if delete button should be visible
   */
  canDeletePost(postId: number): boolean {
    const result = this.currentView === 'MY_POSTS';
    console.log('canDeletePost check - postId:', postId, 'currentView:', this.currentView, 'result:', result);
    return result;
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

  /**
   * Goes to the next page
   */
  async nextPage(): Promise<void> {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      await this.loadPostsForCurrentView();
    }
  }

  /**
   * Goes to the previous page
   */
  async previousPage(): Promise<void> {
    if (this.currentPage > 0) {
      this.currentPage--;
      await this.loadPostsForCurrentView();
    }
  }

  /**
   * Goes to a specific page
   */
  async goToPage(page: number): Promise<void> {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      await this.loadPostsForCurrentView();
    }
  }

  /**
   * Loads posts for the current view
   */
  private async loadPostsForCurrentView(): Promise<void> {
    switch (this.currentView) {
      case 'ALL':
        await this.loadAllPosts();
        break;
      case 'MY_POSTS':
        await this.loadMyPosts();
        break;
      case 'BOOKMARKED':
        await this.loadBookmarkedPosts();
        break;
      case 'TRENDING':
        await this.loadTrendingPosts();
        break;
    }
  }

}