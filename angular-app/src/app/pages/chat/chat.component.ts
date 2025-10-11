import {AfterViewChecked, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ChatResponse} from '../../services/chat-services/models/chat-response';
import {DatePipe} from '@angular/common';
import * as Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import {FormsModule} from '@angular/forms';
import {PickerComponent} from '@ctrl/ngx-emoji-mart';
import { KeycloakService } from '../../utils/keycloak/KeycloakService';
import { getChatsByReceiver } from '../../services/chat-services/fn/chats/get-chats-by-receiver';
import { getAllMessages } from '../../services/chat-services/fn/message/get-all-messages';
import { saveMessage } from '../../services/chat-services/fn/message/save-message';
import { setMessageToSeen } from '../../services/chat-services/fn/message/set-message-to-seen';
import { uploadMedia as uploadMediaApi } from '../../services/chat-services/fn/message/upload-media';
import { Api } from '../../services/chat-services/api';
import { ChatListComponent } from '../../components/chat-list/chat-list.component';
import { MessageRequest, MessageResponse, NotificationDto } from '../../services/chat-services/models';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    DatePipe,
    PickerComponent,
    FormsModule,
    ChatListComponent
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class MainComponent implements OnInit, OnDestroy, AfterViewChecked {

  selectedChat: ChatResponse = {};
  chats: Array<ChatResponse> = [];
  chatMessages: Array<MessageResponse> = [];
  socketClient: any = null;
  messageContent: string = '';
  showEmojis = false;
  @ViewChild('scrollableDiv') scrollableDiv!: ElementRef<HTMLDivElement>;
  private notificationSubscription: any;

  constructor(
    private chatService: Api,
    private messageService: Api,
    private keycloakService: KeycloakService  ,
  ) {
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  ngOnDestroy(): void {
    if (this.socketClient !== null) {
      this.socketClient.disconnect();
      this.notificationSubscription.unsubscribe();
      this.socketClient = null;
    }
  }

  ngOnInit(): void {
    this.initWebSocket();
    this.getAllChats();
  }

  chatSelected(chatResponse: ChatResponse) {
    this.selectedChat = chatResponse;
    this.getAllChatMessages(chatResponse.id as string);
    this.setMessagesToSeen();
    this.selectedChat.unreadCount = 0;
  }

  isSelfMessage(message: MessageResponse): boolean {
    return message.senderId === this.keycloakService.userId;
  }

  async sendMessage() {
    if (!this.messageContent) return;
    const messageRequest: MessageRequest = {
      chatId: this.selectedChat.id,
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: this.messageContent,
      type: 'TEXT',
    };
    await this.messageService.invoke(saveMessage, { body: messageRequest });
    const message: MessageResponse = {
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: this.messageContent,
      type: 'TEXT',
      state: 'SENT',
      createdAt: new Date().toString()
    };
    this.selectedChat.lastMessage = this.messageContent;
    this.chatMessages.push(message);
    this.messageContent = '';
    this.showEmojis = false;
  }

  keyDown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  }

  onSelectEmojis(emojiSelected: any) {
    const emoji = emojiSelected.emoji;
    this.messageContent += emoji.native;
  }

  onClick() {
    this.setMessagesToSeen();
  }

  async uploadMedia(target: EventTarget | null) {
    const file = this.extractFileFromTarget(target);
    if (file !== null) {
      const reader = new FileReader();
      reader.onload = () => {
        if (reader.result) {

          const mediaLines = reader.result.toString().split(',')[1];

          this.messageService.invoke(uploadMediaApi, {
            'chat-id': this.selectedChat.id as string,
            body: { file }
          }).then(() => {
            const message: MessageResponse = {
              senderId: this.getSenderId(),
              receiverId: this.getReceiverId(),
              content: 'Attachment',
              type: 'IMAGE',
              state: 'SENT',
              media: mediaLines,
              createdAt: new Date().toString()
            };
            this.chatMessages.push(message);
          });
        }
      }
      reader.readAsDataURL(file);
    }
  }

  logout() {
    this.keycloakService.logout();
  }

  userProfile() {
    this.keycloakService.accountManagement();
  }

  private async setMessagesToSeen() {
    await this.messageService.invoke(setMessageToSeen, {
      'chat-id': this.selectedChat.id as string
    });
  }

  private getAllChats() {
    this.chatService.invoke( 
      getChatsByReceiver).then(
        (chats) => {
          this.chats = chats;
        }
      );
  }

  private getAllChatMessages(chatId: string) {

    this.messageService.invoke(
      getAllMessages,
      {
        'chat-id': chatId
      }
    ).then(
      (messages) => {
        this.chatMessages = messages;
      }
    );
  }

  private initWebSocket() {
    if (this.keycloakService.keycloak?.tokenParsed?.sub) {
      let ws = new SockJS('http://localhost:8081/ws');
      this.socketClient = Stomp.over(ws);
      const subUrl = `/user/${this.keycloakService.keycloak.tokenParsed?.sub}/chat`;
      this.socketClient.connect({'Authorization': 'Bearer ' + this.keycloakService.keycloak.token},
        () => {
          this.notificationSubscription = this.socketClient.subscribe(subUrl,
            (message: any) => {
              const notification: NotificationDto = JSON.parse(message.body);
              this.handleNotification(notification);

            },
            () => console.error('Error while connecting to webSocket')
            );
        }
      );
    }
  }

  private handleNotification(notification: NotificationDto) {
    console.log(notification);
    if (!notification) return;
    if (this.selectedChat && this.selectedChat.id === notification.chatId) {
      switch (notification.type) {
        case 'MESSAGE':
        case 'IMAGE':
          const message: MessageResponse = {
            senderId: notification.senderId,
            receiverId: notification.receiverId,
            content: notification.content,
            type: notification.messageType,
            media: notification.media?.[0],
            createdAt: new Date().toString()
          };
          if (notification.type === 'IMAGE') {
            this.selectedChat.lastMessage = 'Attachment';
          } else {
            this.selectedChat.lastMessage = notification.content;
          }
          this.chatMessages.push(message);
          break;
        case 'SEEN':
          this.chatMessages.forEach(m => m.state = 'SEEN');
          break;
      }
    } else {
      const destChat = this.chats.find(c => c.id === notification.chatId);
      if (destChat && notification.type !== 'SEEN') {
        if (notification.type === 'MESSAGE') {
          destChat.lastMessage = notification.content;
        } else if (notification.type === 'IMAGE') {
          destChat.lastMessage = 'Attachment';
        }
        destChat.lastMessageTime = new Date().toString();
        destChat.unreadCount! += 1;
      } else if (notification.type === 'MESSAGE') {
        const newChat: ChatResponse = {
          id: notification.chatId,
          senderId: notification.senderId,
          receiverId: notification.receiverId,
          lastMessage: notification.content,
          name: notification.chatName,
          unreadCount: 1,
          lastMessageTime: new Date().toString()
        };
        this.chats.unshift(newChat);
      }
    }
  }

  private getSenderId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.senderId as string;
    }
    return this.selectedChat.receiverId as string;
  }

  private getReceiverId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.receiverId as string;
    }
    return this.selectedChat.senderId as string;
  }

  private scrollToBottom() {
    if (this.scrollableDiv) {
      const div = this.scrollableDiv.nativeElement;
      div.scrollTop = div.scrollHeight;
    }
  }

  private extractFileFromTarget(target: EventTarget | null): File | null {
    const htmlInputTarget = target as HTMLInputElement;
    if (target === null || htmlInputTarget.files === null) {
      return null;
    }
    return htmlInputTarget.files[0];
  }
}
