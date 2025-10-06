package com.anas.groupservice.repository;

import com.anas.groupservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(String userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedDateDesc(String userId, boolean isRead);

    List<Notification> findByGroupIdOrderByCreatedDateDesc(Long groupId);

    List<Notification> findByUserIdAndGroupIdOrderByCreatedDateDesc(String userId, Long groupId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") String userId);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    @Modifying
    void markAllAsReadByUserId(@Param("userId") String userId);
}