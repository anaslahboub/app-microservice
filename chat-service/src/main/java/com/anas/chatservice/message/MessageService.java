package com.anas.chatservice.message;

import com.anas.chatservice.chat.Chat;
import com.anas.chatservice.chat.ChatRepository;
import com.anas.chatservice.file.FileService;
import com.anas.chatservice.file.FileUtils;
import com.anas.chatservice.notification.Notification;
import com.anas.chatservice.notification.NotificationService;
import com.anas.chatservice.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final NotificationService notificationService;
    private final FileService fileService;

    public void saveMessage(MessageRequest messageRequest) {
        Chat chat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);

        messageRepository.save(message);

        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setMessageType(messageRequest.getType());
        notification.setContent(messageRequest.getContent());
        notification.setSenderId(messageRequest.getSenderId());
        notification.setReceiverId(messageRequest.getReceiverId());
        notification.setType(NotificationType.MESSAGE);
        notification.setChatName(chat.getTargetChatName(message.getSenderId()));
        notification.setRead(false);

        notificationService.createAndSendNotification(messageRequest.getReceiverId(), notification);
    }

    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }

    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        final String recipientId = getRecipientId(chat, authentication);

        messageRepository.setMessagesToSeenByChatId(chatId, MessageState.SEEN);

        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setType(NotificationType.SEEN);
        notification.setReceiverId(recipientId);
        notification.setSenderId(getSenderId(chat, authentication));
        notification.setRead(false);

        notificationService.createAndSendNotification(recipientId, notification);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        final String senderId = getSenderId(chat, authentication);
        final String receiverId = getRecipientId(chat, authentication);

        final String filePath = fileService.saveFile(file, senderId);
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setSenderId(senderId);
        message.setState(MessageState.SENT);
        message.setType(MessageType.IMAGE);
        message.setMediaFilePath(filePath);
        message.setChat(chat);
        messageRepository.save(message);

        Notification notification = new Notification();
        notification.setChatId(chat.getId());
        notification.setType(NotificationType.IMAGE);
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setMessageType(MessageType.IMAGE);
        notification.setMedia(FileUtils.readFileFromLocation(filePath));
        notification.setRead(false);

        notificationService.createAndSendNotification(receiverId, notification);
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }

    private String getRecipientId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }
}