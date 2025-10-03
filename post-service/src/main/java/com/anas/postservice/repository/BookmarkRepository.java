package com.anas.postservice.repository;

import com.anas.postservice.entities.Bookmark;
import com.anas.postservice.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    @Query("SELECT b FROM Bookmark b WHERE b.post.id = :postId AND b.userId = :userId")
    Optional<Bookmark> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") String userId);
    
    @Query("SELECT b.post FROM Bookmark b WHERE b.userId = :userId ORDER BY b.createdDate DESC")
    Page<Post> findBookmarkedPostsByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.post.id = :postId")
    Long countBookmarksByPostId(@Param("postId") Long postId);
}