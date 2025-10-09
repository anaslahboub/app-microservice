package com.anas.postservice.service;

import com.anas.postservice.dto.CreatePostRequest;
import com.anas.postservice.dto.PostResponse;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.entities.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {


    List<Post> getAllPosts(Pageable pageable);
    Page<Post> getTrendingPosts(Pageable pageable);

    List<Post> getPinnedPostsByGroupId(String groupId);

    List<Post> getPostsByAuthorId(String authorId, Pageable pageable);

    Page<Post> getPendingPostsByAuthorId(String authorId, Pageable pageable);

    Page<Post> getApprovedPostsByGroupId(String groupId, Pageable pageable);

    Page<Post> searchApprovedPosts(String query, Pageable pageable);

    Page<Post> searchApprovedPostsInGroup(String groupId, String query, Pageable pageable);

    // Post creation and modification methods
    Post createPost(CreatePostRequest request,String authorId);

    Post updatePostStatus(Long postId, PostStatus status);

    Post pinPost(Long postId, boolean pinned);

    void deletePost(Long postId);

    void deletePosts(List<Long> postIds);

    // Like methods
    Like toggleLike(Long postId, String userId);

    boolean isPostLikedByUser(Long postId, String userId);

    // Vote methods
    Vote votePost(Long postId, String userId, boolean upvote);

    boolean isPostVotedByUser(Long postId, String userId, boolean upvote);

    // Bookmark methods
    Bookmark toggleBookmark(Long postId, String userId);

    boolean isPostBookmarkedByUser(Long postId, String userId);

    Page<Post> getBookmarkedPostsByUser(String userId, Pageable pageable);

    // Comment methods
    Comment addComment(Long postId, String content, String authorId, Long parentCommentId);

    Page<Comment> getCommentsByPostId(Long postId, Pageable pageable);

    List<Comment> getRepliesByParentCommentId(Long parentCommentId);
}