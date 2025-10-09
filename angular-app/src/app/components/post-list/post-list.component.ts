import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostApiService } from '../../services/post-api.service';
import { Post, CreatePostRequest, PagedModelPost } from '../../post-services/models';
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
  
  @ViewChild('fileInput') fileInput!: ElementRef;
  
  // ========================================
  // CONSTRUCTOR & LIFECYCLE
  // ========================================
  
  constructor(
    public keycloakService: KeycloakService,
    private postApiService: PostApiService
  ) {}
  
  ngOnInit(): void {
    this.loadPosts();
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
   * Loads posts for the current user
   */
  async loadPosts(): Promise<void> {
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.getMyPosts(this.currentPage, this.pageSize);
      console.log('Page Data:', pageData);
      this.posts = pageData.content || [];
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
      this.newPostContent = '';
      this.showEmojiPicker = false;
      this.removeSelectedFile();
      
    } catch (error) {
      console.error('Error sending post:', error);
      this.showError('Failed to send post');
    } finally {
      this.isSendingPost = false;
    }
  }
  
  /**
   * Toggles like on a post
   * @param postId - ID of the post to like/unlike
   */
  async toggleLike(postId: number): Promise<void> {
    try {
      const updatedPost = await this.postApiService.toggleLike(postId);
      
      // Update the post in the local array
      const index = this.posts.findIndex(p => p.id === postId);
      if (index !== -1) {
        this.posts[index] = updatedPost;
      }
    } catch (error) {
      console.error('Error toggling like:', error);
      this.showError('Failed to like/unlike post');
    }
  }
  
  /**
   * Toggles bookmark on a post
   * @param postId - ID of the post to bookmark/unbookmark
   */
  async toggleBookmark(postId: number): Promise<void> {
    try {
      const updatedPost = await this.postApiService.toggleBookmark(postId);
      
      // Update the post in the local array
      const index = this.posts.findIndex(p => p.id === postId);
      if (index !== -1) {
        this.posts[index] = updatedPost;
      }
    } catch (error) {
      console.error('Error toggling bookmark:', error);
      this.showError('Failed to bookmark/unbookmark post');
    }
  }
  
  /**
   * Deletes a post
   * @param postId - ID of the post to delete
   */
  async deletePost(postId: number): Promise<void> {
    const confirmed = confirm('Are you sure you want to delete this post?');
    if (!confirmed) return;
    
    try {
      await this.postApiService.deletePost(postId);
      this.posts = this.posts.filter(post => post.id !== postId);
    } catch (error) {
      console.error('Error deleting post:', error);
      this.showError('Failed to delete post');
    }
  }
  
  /**
   * Searches posts based on search term
   */
  async searchPosts(): Promise<void> {
    if (!this.searchTerm.trim()) {
      this.showSearchResults = false;
      this.loadPosts(); // Load all posts if search is cleared
      return;
    }
    
    this.isLoadingPosts = true;
    try {
      const pageData = await this.postApiService.searchPosts(this.searchTerm, this.currentPage, this.pageSize);
      this.posts = pageData.content || [];
      this.totalPages = pageData.page?.totalPages || 0;
      this.totalElements = pageData.page?.totalElements || 0;
      this.showSearchResults = true;
    } catch (error) {
      console.error('Error searching posts:', error);
      this.showError('Search failed');
    } finally {
      this.isLoadingPosts = false;
    }
  }
  
  /**
   * Clears search results
   */
  clearSearch(): void {
    this.searchTerm = '';
    this.showSearchResults = false;
    this.loadPosts(); // Load all posts
  }
  
  /**
   * Navigates to a specific page
   * @param page - Page number to navigate to
   */
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      if (this.showSearchResults) {
        this.searchPosts();
      } else {
        this.loadPosts();
      }
    }
  }
  
  /**
   * Formats date for display
   * @param date - Date string to format
   * @returns Formatted date string
   */
  formatDate(date: string | undefined): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString();
  }
  
  /**
   * TrackBy function for efficient *ngFor rendering
   * @param index - Index of the item
   * @param post - Post object
   * @returns Unique identifier for the post
   */
  trackById(index: number, post: Post): number | undefined {
    return post.id;
  }
  
  private showError(message: string): void {
    // In a real app, you'd use a toast service or modal
    console.error('Error:', message);
    alert(message);
  }
}