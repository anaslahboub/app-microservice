package com.anas.postservice.service;

import com.anas.postservice.client.UserServiceClient;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.model.User;
import com.anas.postservice.entities.*;
import com.anas.postservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Post createPost(String content, String imageUrl, String groupId, String authorId) {
        User author = userServiceClient.getUserById(authorId);
        if (author == null) {
            throw new RuntimeException("User not found");
        }

        Post post = new Post();
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setGroupId(groupId);
        // Store the user ID directly since we're not using JPA relationship
        post.setAuthorId(authorId);

        // For teachers and admins, auto-approve posts
        if (isTeacherOrAdmin(authorId)) {
            post.setStatus(PostStatus.APPROVED);
        }

        return postRepository.save(post);
    }

    public Page<Post> getApprovedPostsByGroupId(String groupId, Pageable pageable) {
        return postRepository.findApprovedPostsByGroupId(groupId, pageable);
    }

    public Page<Post> getPostsByAuthorId(String authorId, Pageable pageable) {
        return postRepository.findPostsByAuthorId(authorId, pageable);
    }

    public Page<Post> getTrendingPosts(Pageable pageable) {
        return postRepository.findTrendingPosts(pageable);
    }

    public List<Post> getPinnedPostsByGroupId(String groupId) {
        return postRepository.findPinnedPostsByGroupId(groupId);
    }

    @Transactional
    public Post updatePostStatus(Long postId, PostStatus status) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(status);
        return postRepository.save(post);
    }

    @Transactional
    public Post pinPost(Long postId, boolean pinned) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setPinned(pinned);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Transactional
    public Like toggleLike(Long postId, String userId) {
        User user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user already liked the post
        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(like -> {
                    // Unlike
                    likeRepository.delete(like);
                    post.setLikeCount(post.getLikeCount() - 1);
                    postRepository.save(post);
                    return null;
                })
                .orElseGet(() -> {
                    // Like
                    Like like = new Like();
                    like.setPost(post);
                    like.setUserId(userId);
                    Like savedLike = likeRepository.save(like);
                    post.setLikeCount(post.getLikeCount() + 1);
                    postRepository.save(post);
                    return savedLike;
                });
    }

    public boolean isPostLikedByUser(Long postId, String userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    @Transactional
    public Vote votePost(Long postId, String userId, boolean upvote) {
        User user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user already voted on this post
        return voteRepository.findByPostIdAndUserId(postId, userId)
                .map(existingVote -> {
                    // If same vote type, remove the vote
                    if (existingVote.isUpvote() == upvote) {
                        voteRepository.delete(existingVote);
                        // Update post vote counts
                        if (upvote) {
                            post.setUpvoteCount(post.getUpvoteCount() - 1);
                        } else {
                            post.setDownvoteCount(post.getDownvoteCount() - 1);
                        }
                        postRepository.save(post);
                        return null;
                    } else {
                        // Change vote type
                        existingVote.setUpvote(upvote);
                        Vote savedVote = voteRepository.save(existingVote);
                        // Update post vote counts
                        if (upvote) {
                            post.setUpvoteCount(post.getUpvoteCount() + 1);
                            post.setDownvoteCount(post.getDownvoteCount() - 1);
                        } else {
                            post.setUpvoteCount(post.getUpvoteCount() - 1);
                            post.setDownvoteCount(post.getDownvoteCount() + 1);
                        }
                        postRepository.save(post);
                        return savedVote;
                    }
                })
                .orElseGet(() -> {
                    // Create new vote
                    Vote vote = new Vote();
                    vote.setPost(post);
                    vote.setUserId(userId);
                    vote.setUpvote(upvote);
                    Vote savedVote = voteRepository.save(vote);
                    // Update post vote counts
                    if (upvote) {
                        post.setUpvoteCount(post.getUpvoteCount() + 1);
                    } else {
                        post.setDownvoteCount(post.getDownvoteCount() + 1);
                    }
                    postRepository.save(post);
                    return savedVote;
                });
    }

    public boolean isPostVotedByUser(Long postId, String userId, boolean upvote) {
        return voteRepository.findByPostIdAndUserId(postId, userId)
                .map(vote -> vote.isUpvote() == upvote)
                .orElse(false);
    }

    @Transactional
    public Bookmark toggleBookmark(Long postId, String userId) {
        User user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if user already bookmarked the post
        return bookmarkRepository.findByPostIdAndUserId(postId, userId)
                .map(bookmark -> {
                    // Remove bookmark
                    bookmarkRepository.delete(bookmark);
                    post.setBookmarkCount(post.getBookmarkCount() - 1);
                    postRepository.save(post);
                    return null;
                })
                .orElseGet(() -> {
                    // Create bookmark
                    Bookmark bookmark = new Bookmark();
                    bookmark.setPost(post);
                    bookmark.setUserId(userId);
                    Bookmark savedBookmark = bookmarkRepository.save(bookmark);
                    post.setBookmarkCount(post.getBookmarkCount() + 1);
                    postRepository.save(post);
                    return savedBookmark;
                });
    }

    public boolean isPostBookmarkedByUser(Long postId, String userId) {
        return bookmarkRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public Page<Post> getBookmarkedPostsByUser(String userId, Pageable pageable) {
        return bookmarkRepository.findBookmarkedPostsByUserId(userId, pageable);
    }

    @Transactional
    public Comment addComment(Long postId, String content, String authorId, Long parentCommentId) {
        User author = userServiceClient.getUserById(authorId);
        if (author == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthorId(authorId);

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Update comment count on post
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return savedComment;
    }

    public Page<Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findRootCommentsByPostId(postId, pageable);
    }

    public List<Comment> getRepliesByParentCommentId(Long parentCommentId) {
        return commentRepository.findRepliesByParentCommentId(parentCommentId);
    }

    private boolean isTeacherOrAdmin(String userId) {
        // This is a simplified implementation
        // In a real application, you would check the user's role
        return true;
    }
}