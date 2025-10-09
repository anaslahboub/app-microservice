package com.anas.postservice.controller;


import com.anas.postservice.dto.CreatePostRequest;
import com.anas.postservice.dto.PostResponse;
import com.anas.postservice.entities.Comment;
import com.anas.postservice.entities.Like;
import com.anas.postservice.entities.Post;
import com.anas.postservice.entities.Vote;
import com.anas.postservice.entities.Bookmark;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.service.PostService;
import com.anas.postservice.dto.PostRequest;
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
    public ResponseEntity<List<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<Post> postResponses = postService.getAllPosts(pageable);
        return ResponseEntity.ok(postResponses);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<Post>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        List<Post> posts = postService.getPostsByAuthorId(userId, pageable);
        return ResponseEntity.ok(posts);
    }


    @GetMapping("/group/{group-id}")
    public ResponseEntity<Page<Post>> getPostsByGroupId(
            @PathVariable("group-id") String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getApprovedPostsByGroupId(groupId, pageable);
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

    @GetMapping("/pinned/{group-id}")
    public ResponseEntity<List<Post>> getPinnedPosts(@PathVariable("group-id") String groupId) {
        List<Post> posts = postService.getPinnedPostsByGroupId(groupId);
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
    public ResponseEntity<Void> deletePost(@PathVariable("post-id") Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePosts(@RequestParam List<Long> postIds) {
        postService.deletePosts(postIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{post-id}/vote")
    public ResponseEntity<Vote> votePost(
            @PathVariable("post-id") Long postId,
            @RequestParam("upvote") boolean upvote,
            Authentication authentication) {

        String userId = authentication.getName();
        Vote vote = postService.votePost(postId, userId, upvote);
        return ResponseEntity.ok(vote);
    }

    @GetMapping("/{post-id}/voted")
    public ResponseEntity<Boolean> isPostVotedByUser(
            @PathVariable("post-id") Long postId,
            @RequestParam("upvote") boolean upvote,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean voted = postService.isPostVotedByUser(postId, userId, upvote);
        return ResponseEntity.ok(voted);
    }

    @PostMapping("/{post-id}/like")
    public ResponseEntity<Like> toggleLike(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        Like like = postService.toggleLike(postId, userId);
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
    public ResponseEntity<Bookmark> toggleBookmark(
            @PathVariable("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        Bookmark bookmark = postService.toggleBookmark(postId, userId);
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

    @GetMapping("/group/{group-id}/search")
    public ResponseEntity<Page<Post>> searchPostsInGroup(
            @PathVariable("group-id") String groupId,
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchApprovedPostsInGroup(groupId, query, pageable);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{post-id}/comment")
    public ResponseEntity<Comment> addComment(
            @PathVariable("post-id") Long postId,
            @RequestParam("content") String content,
            @RequestParam(value = "parent-comment-id", required = false) Long parentCommentId,
            Authentication authentication) {

        String userId = authentication.getName();
        Comment comment = postService.addComment(postId, content, userId, parentCommentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{post-id}/comments")
    public ResponseEntity<Page<Comment>> getComments(
            @PathVariable("post-id") Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = postService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comment/{comment-id}/replies")
    public ResponseEntity<List<Comment>> getReplies(@PathVariable("comment-id") Long commentId) {
        List<Comment> replies = postService.getRepliesByParentCommentId(commentId);
        return ResponseEntity.ok(replies);
    }
}