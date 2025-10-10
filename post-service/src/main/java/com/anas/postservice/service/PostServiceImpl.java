package com.anas.postservice.service;

import com.anas.postservice.client.UserServiceClient;
import com.anas.postservice.dto.*;
import com.anas.postservice.enumeration.PostStatus;
import com.anas.postservice.exception.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    // Removed VoteRepository
    // private final VoteRepository voteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserServiceClient userServiceClient;
    private final FileService fileService;
    private final NotificationService notificationService;

    public Page<Post> getAllPosts(Pageable pageable) {
        log.info("Fetching posts from database - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Post>  posts = postRepository.findAllOrderByCreatedDateDesc(pageable);
         return posts;
    }
    public Page<Post> getPostsByAuthorId(String authorId, Pageable pageable) {
        log.info("fetchin posts for current user with id {}", authorId);
        return postRepository.findPostsByAuthorId(authorId, pageable);
    }

    public Page<Post> getTrendingPosts(Pageable pageable) {

        return postRepository.findTrendingPosts(pageable);
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.getPostById(id);
    }

    public Page<Post> getPendingPostsByAuthorId(String authorId, Pageable pageable) {
        return postRepository.findPendingPostsByAuthorId(authorId, pageable);
    }

    public Page<Post> searchApprovedPosts(String query, Pageable pageable) {
        return postRepository.searchApprovedPosts(query, pageable);
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
    public void deletePost(Long postId, String userId) {
        // Une seule requête à la base de données
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Vérification des autorisations
        if (!post.getAuthorId().equals(userId)) {
            throw new UnauthorizedActionException("You don't have permission to delete this post");
        }

        postRepository.deleteById(postId);
    }


    /*@Transactional
    public Like toggleLike(Long postId, String userId) {
        User user = fetchAndValidateUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        return likeRepository.findByPostIdAndUserId(postId, userId)
                .map(like -> {
                    // Unlike
                    likeRepository.delete(like);
                    post.setLikeCount(post.getLikeCount() - 1);
                    Post updatedPost = postRepository.save(post);
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
    */

    @Transactional
    public LikeResponse toggleLike(Long postId, String userId) {
        User user = fetchAndValidateUser(userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // Unlike - suppression seulement
            Like like = existingLike.get();
            likeRepository.delete(like);
            post.setLikeCount(post.getLikeCount() - 1);
            Post updatedPost = postRepository.save(post);
            sendUnlikeNotification(updatedPost, user);

            return new LikeResponse(null, false, "unliked", updatedPost.getLikeCount());
        } else {
            // Like - création seulement
            Like like = new Like();
            like.setPost(post);
            like.setUserId(userId);
            Like savedLike = likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            Post updatedPost = postRepository.save(post);
            sendLikeNotification(updatedPost, user);

            return new LikeResponse(savedLike, true, "liked", updatedPost.getLikeCount());
        }
    }

    public boolean isPostLikedByUser(Long postId, String userId) {
        return likeRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }


    /*
    @Transactional
    public Bookmark toggleBookmark(Long postId, String userId) {
         User user= fetchAndValidateUser(userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Check if user already bookmarked the post
        return bookmarkRepository.findByPostIdAndUserId(postId, userId)
                .map(bookmark -> {
                    // Remove bookmark
                    bookmarkRepository.delete(bookmark);
                    post.setBookmarkCount(post.getBookmarkCount() - 1);
                    Post updatedPost = postRepository.save(post);
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
    /*/

    @Transactional
    public BookmarkResult toggleBookmark(Long postId, String userId) {
        User user = fetchAndValidateUser(userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByPostIdAndUserId(postId, userId);

        if (existingBookmark.isPresent()) {
            // Remove bookmark
            bookmarkRepository.delete(existingBookmark.get());
            post.setBookmarkCount(post.getBookmarkCount() - 1);
            postRepository.save(post);
            sendBookmarkRemovedNotification(post, user);

            return new BookmarkResult(null, false, post.getBookmarkCount(), "removed");
        } else {
            // Create bookmark
            Bookmark bookmark = new Bookmark();
            bookmark.setPost(post);
            bookmark.setUserId(userId);
            Bookmark savedBookmark = bookmarkRepository.save(bookmark);
            post.setBookmarkCount(post.getBookmarkCount() + 1);
            postRepository.save(post);
            sendNewBookmarkNotification(post, user);

            return new BookmarkResult(savedBookmark, true, post.getBookmarkCount(), "added");
        }
    }

    public boolean isPostBookmarkedByUser(Long postId, String userId) {
        return bookmarkRepository.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public Page<Post> getBookmarkedPostsByUser(String userId, Pageable pageable) {
        return bookmarkRepository.findBookmarkedPostsByUserId(userId, pageable);
    }

    public CommentResponse addComment(Long postId, String content, String userId, Long parentCommentId) {
        // Validation
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        // Vérifier le post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Créer le commentaire
        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setPost(post);
        comment.setAuthorId(userId);

        // Gérer la réponse si parentCommentId existe
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ParentCommentNotFoundException(parentCommentId));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Mettre à jour le compteur
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Convertir en Response
        return mapToCommentResponse(savedComment);
    }

    public List<CommentResponse> getCommentsWithReplies(Long postId) {
        // Récupérer tous les commentaires du post
        List<Comment> allComments = commentRepository.findByPostIdOrderByCreatedDateDesc(postId);

        return buildCommentTree(allComments);
    }

    public Page<CommentResponse> getMainCommentsByPostId(Long postId, Pageable pageable) {
        Page<Comment> mainComments = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);

        return mainComments.map(this::mapToCommentResponse);
    }

    private List<CommentResponse> buildCommentTree(List<Comment> allComments) {
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> mainComments = new ArrayList<>();

        // Premier passage : créer tous les DTOs
        for (Comment comment : allComments) {
            CommentResponse response = mapToCommentResponse(comment);
            commentMap.put(comment.getId(), response);

            if (comment.getParentComment() == null) {
                mainComments.add(response);
            }
        }

        // Deuxième passage : organiser les réponses
        for (Comment comment : allComments) {
            if (comment.getParentComment() != null) {
                CommentResponse parentResponse = commentMap.get(comment.getParentComment().getId());
                if (parentResponse != null) {
                    if (parentResponse.getReplies() == null) {
                        parentResponse.setReplies(new ArrayList<>());
                    }
                    parentResponse.getReplies().add(commentMap.get(comment.getId()));
                }
            }
        }

        return mainComments;
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setAuthorId(comment.getAuthorId());
        response.setPostId(comment.getPost().getId());
        response.setCreatedDate(comment.getCreatedDate());
        response.setReply(comment.getParentComment() != null);

        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }

        response.setReplies(new ArrayList<>()); // Initialiser la liste des réponses

        return response;

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