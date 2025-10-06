package com.anas.chatservice.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdOrderByCreatedDateDesc(String receiverId);

    List<Notification> findByReceiverIdAndIsReadOrderByCreatedDateDesc(String receiverId, boolean isRead);

    List<Notification> findByChatIdOrderByCreatedDateDesc(String chatId);

    List<Notification> findByReceiverIdAndChatIdOrderByCreatedDateDesc(String receiverId, String chatId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiverId = :receiverId AND n.isRead = false")
    Long countUnreadByReceiverId(@Param("receiverId") String receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiverId = :receiverId AND n.isRead = false")
    void markAllAsReadByReceiverId(@Param("receiverId") String receiverId);
}