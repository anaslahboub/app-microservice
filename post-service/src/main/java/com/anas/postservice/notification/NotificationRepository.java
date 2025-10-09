package com.anas.postservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdOrderByCreatedDateDesc(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdAndIsReadOrderByCreatedDateDesc(@Param("userId") String userId, @Param("isRead") boolean isRead);

    @Query("SELECT n FROM Notification n WHERE n.postId = :postId ORDER BY n.createdDate DESC")
    List<Notification> findByPostIdOrderByCreatedDateDesc(@Param("postId") Long postId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") String userId);
}