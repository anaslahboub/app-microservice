package com.anas.postservice.controller;


import com.anas.postservice.dto.*;
import com.anas.postservice.entities.Comment;
import com.anas.postservice.entities.Like;
import com.anas.postservice.entities.Post;
import com.anas.postservice.entities.Bookmark;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            @Valid @ModelAttribute CreatePostRequest postRequest, // Changed to @ModelAttribute
            Authentication authentication) {

        String userId = authentication.getName();
        Post post = postService.createPost(postRequest, userId);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postResponses = postService.getAllPosts(pageable);
        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<Post>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPostsByAuthorId(userId, pageable);
        return ResponseEntity.ok(posts);
    }




    @GetMapping("/trending")
    public ResponseEntity<Page<Post>> getTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getTrendingPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my-pending")
    public ResponseEntity<Page<Post>> getMyPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPendingPostsByAuthorId(userId, pageable);
        return ResponseEntity.ok(posts);
    }



    @PatchMapping("/{post-id}/status")
    public ResponseEntity<Post> updatePostStatus(
            @PathVariable("post-id") Long postId,
            @RequestParam("status") PostStatus status) {

        Post post = postService.updatePostStatus(postId, status);
        return ResponseEntity.ok(post);
    }

    @PatchMapping("/{post-id}/pin")
    public ResponseEntity<Post> pinPost(
            @PathVariable("post-id") Long postId,
            @RequestParam("pinned") boolean pinned) {

        Post post = postService.pinPost(postId, pinned);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{post-id}")
    public ResponseEntity<Void> deletePost(@PathVariable("post-id") Long postId,
                                           Authentication authentication) {
        String userId = authentication.getName();
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{post-id}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        LikeResponse like = postService.toggleLike(postId, userId);
        return ResponseEntity.ok(like);
    }

    @GetMapping("/{post-id}/liked")
    public ResponseEntity<Boolean> isPostLikedByUser(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean liked = postService.isPostLikedByUser(postId, userId);
        return ResponseEntity.ok(liked);
    }

    @PostMapping("/{post-id}/bookmark")
    public ResponseEntity<BookmarkResult> toggleBookmark(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        BookmarkResult bookmark = postService.toggleBookmark(postId, userId);
        return ResponseEntity.ok(bookmark);
    }

    @GetMapping("/{post-id}/bookmarked")
    public ResponseEntity<Boolean> isPostBookmarkedByUser(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean bookmarked = postService.isPostBookmarkedByUser(postId, userId);
        return ResponseEntity.ok(bookmarked);
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<Page<Post>> getBookmarkedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getBookmarkedPostsByUser(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchApprovedPosts(query, pageable);
        return ResponseEntity.ok(posts);
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentCommentId,
            Authentication authentication) {

        String userId = authentication.getName();
        CommentResponse comment = postService.addComment(postId, content, userId, parentCommentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsWithReplies(
            @PathVariable Long postId) {

        List<CommentResponse> comments = postService.getCommentsWithReplies(postId);
        return ResponseEntity.ok(comments);
    }

    // Si vous voulez garder la pagination pour les commentaires principaux seulement
    @GetMapping("/{postId}/comments/paginated")
    public ResponseEntity<Page<CommentResponse>> getCommentsPaginated(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CommentResponse> comments = postService.getMainCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(comments);
    }
}