package com.anas.chatservice.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse implements Serializable {

    private String id;
    private String name;
    private long unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean isRecipientOnline;
    private String senderId;
    private String receiverId;
}
