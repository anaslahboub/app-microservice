package com.anas.postservice.service;

import com.anas.postservice.dto.*;
import com.anas.postservice.entities.Bookmark;
import com.anas.postservice.entities.Comment;
import com.anas.postservice.entities.Like;
import com.anas.postservice.entities.Post;
import com.anas.postservice.enumeration.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    /**
     * get All poots exist in database
      * @param pageable
     * @return
     */
    Page<Post> getAllPosts(Pageable pageable);

    /**
     *get posts thst have heigher number of comment and likes
     * @param pageable
     * @return
     */
    Page<Post> getTrendingPosts(Pageable pageable);

    /**
     * get one post by id so that can lett me display it or update
     * @param id
     * @return
     */
    Post getPostById(Long id);

    /**
     *gzts all posts for every users
     * @param authorId
     * @param pageable
     * @return
     */
    Page<Post> getPostsByAuthorId(String authorId, Pageable pageable);

    /**
     *get all posts for every user
     * @param authorId
     * @param pageable
     * @return
     */
    Page<Post> getPendingPostsByAuthorId(String authorId, Pageable pageable);


    /**
     *search post using some string
     * @param query
     * @param pageable
     * @return
     */
    Page<Post> searchApprovedPosts(String query, Pageable pageable);

    /**
     * post creation with two case with or without images
      * @param request
     * @param authorId
     * @return
     */
    Post createPost(CreatePostRequest request,String authorId);

    /**
     * update post status
     * @param postId
     * @param status
     * @return
     */
    Post updatePostStatus(Long postId, PostStatus status);

    /**
     * do nothing hhhhh hhhhhhh   hhhhhhhhhhhhhh Qoder who add it so we will never now
     * @param postId
     * @param pinned
     * @return
     */
    Post pinPost(Long postId, boolean pinned);

    /**
     * delete post but we will check first if user has role nott role if hz is the creatore of this posts
     * @param postId
     */
    void deletePost(Long postId,String userId);


    /**
     * add or remove a like for postslike or
      * @param postId
     * @param userId
     * @return
     */
    LikeResponse toggleLike(Long postId, String userId);

    /**
     * check id user has already likr or not
     * @param postId
     * @param userId
     * @return
     */
    boolean isPostLikedByUser(Long postId, String userId);

    /**
     * make post markeed to se itagain
     * @param postId
     * @param userId
     * @return
     */
    BookmarkResult toggleBookmark(Long postId, String userId);

    /**
     * check if post is marked by connected user or not
     * @param postId
     * @param userId
     * @return
     */
    boolean isPostBookmarkedByUser(Long postId, String userId);

    /**
     * get all marked posts
     * @param userId
     * @param pageable
     * @return
     */
    Page<Post> getBookmarkedPostsByUser(String userId, Pageable pageable);

    /**
     *
     * @param postId
     * @return
     */
    List<CommentResponse> getCommentsWithReplies(Long postId);

    /**
     *
     * @param postId
     * @param content
     * @param userId
     * @param parentCommentId
     * @return
     */
     CommentResponse addComment(Long postId, String content, String userId, Long parentCommentId);

    /**
     *
     * @param postId
     * @param pageable
     * @return
     */
    Page<CommentResponse> getMainCommentsByPostId(Long postId, Pageable pageable);


}