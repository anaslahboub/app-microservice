package com.anas.noteservice.repository;

import com.anas.noteservice.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    
    @Query("SELECT n FROM Note n WHERE n.userId = :userId ORDER BY n.createdDate DESC")
    Page<Note> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.groupId = :groupId ORDER BY n.createdDate DESC")
    Page<Note> findByUserIdAndGroupId(@Param("userId") String userId, @Param("groupId") String groupId, Pageable pageable);
    
    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.chatId = :chatId ORDER BY n.createdDate DESC")
    List<Note> findByUserIdAndChatId(@Param("userId") String userId, @Param("chatId") String chatId);
    
    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND (n.title LIKE %:searchTerm% OR n.content LIKE %:searchTerm%) ORDER BY n.createdDate DESC")
    Page<Note> findByUserIdAndSearchTerm(@Param("userId") String userId, @Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.pinned = true ORDER BY n.createdDate DESC")
    List<Note> findPinnedByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(n) FROM Note n WHERE n.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
}