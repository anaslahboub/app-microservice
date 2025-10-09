package com.anas.postservice.service;

import com.anas.postservice.client.UserServiceClient;
import com.anas.postservice.dto.CreatePostRequest;
import com.anas.postservice.dto.PostResponse;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.exception.PostNotFoundException;
import com.anas.postservice.exception.UserServiceException;
import com.anas.postservice.mapper.PostMapper;
import com.anas.postservice.model.User;
import com.anas.postservice.entities.*;
import com.anas.postservice.file.FileService;
import com.anas.postservice.notification.NotificationDTO;
import com.anas.postservice.notification.NotificationService;
import com.anas.postservice.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import feign.FeignException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserServiceClient userServiceClient;
    private final FileService fileService;
    private final NotificationService notificationService;




    public List<Post> getAllPosts(Pageable pageable) {
        log.info("Fetching posts from database - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        List<Post>  posts = postRepository.findAllOrderByCreatedDateDesc(pageable);
         return posts;
    }
    public List<Post> getPostsByAuthorId(String authorId, Pageable pageable) {
        log.info("fetchin posts for current user with id {}", authorId);
        return postRepository.findPostsByAuthorId(authorId, pageable);
    }

    public Page<Post> getTrendingPosts(Pageable pageable) {

        return postRepository.findTrendingPosts(pageable);
    }

    public List<Post> getPinnedPostsByGroupId(String groupId) {
        return postRepository.findPinnedPostsByGroupId(groupId);
    }



    public Page<Post> getPendingPostsByAuthorId(String authorId, Pageable pageable) {
        return postRepository.findPendingPostsByAuthorId(authorId, pageable);
    }

    public Page<Post> getApprovedPostsByGroupId(String groupId, Pageable pageable) {
        return postRepository.findApprovedPostsByGroupId(groupId, pageable);
    }

    public Page<Post> searchApprovedPosts(String query, Pageable pageable) {
        return postRepository.searchApprovedPosts(query, pageable);
    }

    public Page<Post> searchApprovedPostsInGroup(String groupId, String query, Pageable pageable) {
        return postRepository.searchApprovedPostsInGroup(groupId, query, pageable);
    }

    @Transactional
    public Post createPost(CreatePostRequest request, String authorId) {
        // 1. Validate user
        User author = fetchAndValidateUser(authorId);

        // 2. Handle image upload FIRST
        String imageUrl = request.getImageUrl(); // Use pre-existing URL if provided

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            // For file upload, we need to create post first to get ID, but we'll handle this differently
            // We'll upload the file after saving the post but in a single transaction
            imageUrl = null; // Will set after upload
        }

        // 3. Create post entity
        Post post = new Post();
        post.setContent(request.getContent());
        post.setAuthorId(authorId);
        post.setStatus(determinePostStatus());
        post.setImageUrl(imageUrl); // Set pre-existing URL or null for file upload

        // 4. Save post to get ID
        Post savedPost = postRepository.save(post);

        // 5. Handle image file upload (if any)
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            try {
                String uploadedImagePath = fileService.saveFile(
                        request.getImageFile(),
                        savedPost.getId(),
                        authorId
                );
                savedPost.setImageUrl(uploadedImagePath);
                // The post will be automatically saved when transaction commits due to @Transactional
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image for post: " + savedPost.getId(), e);
            }
        }

        // 6. Send notification if approved
        if (savedPost.isApproved()) {
            sendNewPostNotification(savedPost, author);
        }

        return savedPost;
    }



    @Transactional
    public Post updatePostStatus(Long postId, PostStatus status) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        PostStatus oldStatus = post.getStatus();
        post.setStatus(status);
        Post updatedPost = postRepository.save(post);

        // Send notification if post is approved
        if (PostStatus.APPROVED.equals(status) && !PostStatus.APPROVED.equals(oldStatus)) {
            User author = null;
            try {
                author = userServiceClient.getUserById(post.getAuthorId());
            } catch (FeignException e) {
                // Log error but continue
                System.err.println("Error fetching user information: " + e.getMessage());
            }

            if (author != null) {
                sendPostApprovedNotification(updatedPost, author);
            }
        }

        return updatedPost;
    }

    @Transactional
    public Post pinPost(Long postId, boolean pinned) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        post.setPinned(pinned);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
        postRepository.deleteById(postId);
    }

    @Transactional
    public void deletePosts(List<Long> postIds) {
        postRepository.deleteAllById(postIds);
    }

    @Transactional
    public Like toggleLike(Long postId, String userId) {
        final User user;
        try {
             user = userServiceClient.getUserById(userId);
        } catch (FeignException e) {
            throw new RuntimeException("Error fetching user information: " + e.getMessage());
        }
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Check if user already liked the post
        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(like -> {
                    // Unlike
                    likeRepository.delete(like);
                    post.setLikeCount(post.getLikeCount() - 1);
                    Post updatedPost = postRepository.save(post);

                    // Send notification to post author about unlike
                    sendUnlikeNotification(updatedPost, user);

                    return (Like) null;
                })
                .orElseGet(() -> {
                    // Like
                    Like like = new Like();
                    like.setPost(post);
                    like.setUserId(userId);
                    Like savedLike = likeRepository.save(like);
                    post.setLikeCount(post.getLikeCount() + 1);
                    Post updatedPost = postRepository.save(post);

                    // Send notification to post author about like
                    sendLikeNotification(updatedPost, user);

                    return savedLike;
                });
    }

    public boolean isPostLikedByUser(Long postId, String userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    @Transactional
    public Vote votePost(Long postId, String userId, boolean upvote) {
        final User user ;
        try {
            user = userServiceClient.getUserById(userId);
        } catch (FeignException e) {
            throw new RuntimeException("Error fetching user information: " + e.getMessage());
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

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
                        Post updatedPost = postRepository.save(post);

                        // Send notification to post author about vote removal
                        sendVoteRemovedNotification(updatedPost, user, upvote);

                        return (Vote) null;
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
                        Post updatedPost = postRepository.save(post);

                        // Send notification to post author about vote change
                        sendVoteChangedNotification(updatedPost, user, upvote);

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
                    Post updatedPost = postRepository.save(post);

                    // Send notification to post author about new vote
                    sendNewVoteNotification(updatedPost, user, upvote);

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
        final User user;
        try {
            user = userServiceClient.getUserById(userId);
        } catch (FeignException e) {
            throw new RuntimeException("Error fetching user information: " + e.getMessage());
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Check if user already bookmarked the post
        return bookmarkRepository.findByPostIdAndUserId(postId, userId)
                .map(bookmark -> {
                    // Remove bookmark
                    bookmarkRepository.delete(bookmark);
                    post.setBookmarkCount(post.getBookmarkCount() - 1);
                    Post updatedPost = postRepository.save(post);

                    // Send notification to post author about bookmark removal
                    sendBookmarkRemovedNotification(updatedPost, user);

                    return (Bookmark) null;
                })
                .orElseGet(() -> {
                    // Create bookmark
                    Bookmark bookmark = new Bookmark();
                    bookmark.setPost(post);
                    bookmark.setUserId(userId);
                    Bookmark savedBookmark = bookmarkRepository.save(bookmark);
                    post.setBookmarkCount(post.getBookmarkCount() + 1);
                    Post updatedPost = postRepository.save(post);

                    // Send notification to post author about new bookmark
                    sendNewBookmarkNotification(updatedPost, user);

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
        User author = null;
        try {
            author = userServiceClient.getUserById(authorId);
        } catch (FeignException e) {
            throw new RuntimeException("Error fetching user information: " + e.getMessage());
        }

        if (author == null) {
            throw new RuntimeException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

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
        Post updatedPost = postRepository.save(post);

        // Send notification to post author about new comment
        sendNewCommentNotification(updatedPost, comment, author);

        return savedComment;
    }

    public Page<Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findRootCommentsByPostId(postId, pageable);
    }

    public List<Comment> getRepliesByParentCommentId(Long parentCommentId) {
        return commentRepository.findRepliesByParentCommentId(parentCommentId);
    }




    // Méthode privée centralisée
    private Post createPostInternal(String content, MultipartFile imageFile, String imageUrl, String groupId, String authorId) {
        // 1. Validate user
        User author = fetchAndValidateUser(authorId);

        // 2. Create post entity
        Post post = new Post();
        post.setContent(content);
        post.setGroupId(groupId);
        post.setAuthorId(authorId);
        post.setStatus(determinePostStatus());

        // 3. Handle image URL if provided directly
        if (imageUrl != null) {
            post.setImageUrl(imageUrl);
        }

        // 4. Save post first to get ID
        Post savedPost = postRepository.save(post);

        // 5. Handle image file upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadedImageUrl = fileService.saveFile(imageFile, savedPost.getId(), authorId);
            savedPost.setImageUrl(uploadedImageUrl);
            // Pas besoin de re-save ici si tu utilises un entity manager géré
        }

        // 6. Send notification if approved
        if (savedPost.isApproved()) {
            sendNewPostNotification(savedPost, author);
        }

        return savedPost;
    }

    // Helper methods
    private User fetchAndValidateUser(String authorId) {
        try {
            User author = userServiceClient.getUserById(authorId);
            if (author == null) {
                throw new UsernameNotFoundException("User not found: " + authorId);
            }
            return author;
        } catch (FeignException e) {
            throw new UserServiceException("Error fetching user: " + e.getMessage(), e);
        }
    }

    private PostStatus determinePostStatus() {
        return isTeacherOrAdmin() ? PostStatus.APPROVED : PostStatus.PENDING;
    }

    private boolean isTeacherOrAdmin() {
        // Check if the current user has teacher or admin roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_TEACHER".equals(role) || "ROLE_ADMIN".equals(role) || "ROLE_INSTRUCTOR".equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendNewPostNotification(Post post, User author) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("NEW_POST");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage("New post created by " + author.getFullName());
            notification.setUserId(post.getAuthorId());
            notification.setUserName(author.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            // Send notification to group members (this would typically be implemented with a group service)
            // For now, we'll just log it
            System.out.println("New post notification would be sent for post ID: " + post.getId());
        } catch (Exception e) {
            System.err.println("Error sending new post notification: " + e.getMessage());
        }
    }

    private void sendPostApprovedNotification(Post post, User author) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("POST_APPROVED");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage("Your post has been approved");
            notification.setUserId(post.getAuthorId());
            notification.setUserName(author.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending post approved notification: " + e.getMessage());
        }
    }

    private void sendLikeNotification(Post post, User liker) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("POST_LIKED");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage(liker.getFullName() + " liked your post");
            notification.setUserId(post.getAuthorId());
            notification.setUserName(liker.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending like notification: " + e.getMessage());
        }
    }

    private void sendUnlikeNotification(Post post, User unliker) {
        // Unlike notifications are typically not sent
    }

    private void sendNewVoteNotification(Post post, User voter, boolean upvote) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType(upvote ? "POST_UPVOTED" : "POST_DOWNVOTED");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage(voter.getFullName() + " " + (upvote ? "upvoted" : "downvoted") + " your post");
            notification.setUserId(post.getAuthorId());
            notification.setUserName(voter.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending vote notification: " + e.getMessage());
        }
    }

    private void sendVoteChangedNotification(Post post, User voter, boolean upvote) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType(upvote ? "POST_UPVOTED" : "POST_DOWNVOTED");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage(voter.getFullName() + " changed their vote to " + (upvote ? "upvote" : "downvote"));
            notification.setUserId(post.getAuthorId());
            notification.setUserName(voter.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending vote changed notification: " + e.getMessage());
        }
    }

    private void sendVoteRemovedNotification(Post post, User voter, boolean upvote) {
        // Vote removal notifications are typically not sent
    }

    private void sendNewBookmarkNotification(Post post, User bookmarker) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("POST_BOOKMARKED");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage(bookmarker.getFullName() + " bookmarked your post");
            notification.setUserId(post.getAuthorId());
            notification.setUserName(bookmarker.getFullName());
            notification.setRelatedEntityId(post.getId());
            notification.setRelatedEntityType("POST");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending bookmark notification: " + e.getMessage());
        }
    }

    private void sendBookmarkRemovedNotification(Post post, User bookmarker) {
        // Bookmark removal notifications are typically not sent
    }

    private void sendNewCommentNotification(Post post, Comment comment, User commenter) {
        try {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("NEW_COMMENT");
            notification.setPostId(post.getId());
            notification.setPostContent(post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent());
            notification.setMessage(commenter.getFullName() + " commented on your post");
            notification.setUserId(post.getAuthorId());
            notification.setUserName(commenter.getFullName());
            notification.setRelatedEntityId(comment.getId());
            notification.setRelatedEntityType("COMMENT");

            notificationService.sendUserNotification(post.getAuthorId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending comment notification: " + e.getMessage());
        }
    }
}