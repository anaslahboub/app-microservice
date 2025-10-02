import {Component, input, InputSignal, output} from '@angular/core';
import {ChatResponse} from '../../services/models/chat-response';
import {DatePipe} from '@angular/common';
import {UserResponse} from '../../services/models/user-response';
import { Api } from '../../services/api';
import { KeycloakService } from '../../utils/keycloak/KeycloakService';
import { getAllUsers } from '../../services/functions';
import { HttpClient, HttpParams, HttpContext } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';


@Component({
  selector: 'app-chat-list',
  standalone: true,
  templateUrl: './chat-list.component.html',
  imports: [
    DatePipe
  ],
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent {
  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  searchNewContact = false;
  contacts: Array<UserResponse> = [];
  chatSelected = output<ChatResponse>();

  constructor(
    private chatService: Api,
    private userService: Api,
    private keycloakService: KeycloakService,
    private http: HttpClient
  ) {
  }

  searchContact() {
    this.userService.invoke(getAllUsers, {
    }).then(
      (users) => {
        this.contacts = users;
        this.searchNewContact = true;
      }
    );
  }

  selectContact(contact: UserResponse) {
    // Debugging: Check if userId is available
    console.log('Keycloak service:', this.keycloakService);
    console.log('Keycloak instance:', this.keycloakService.keycloak);
    console.log('User ID:', this.keycloakService.userId);
    
    const senderId = this.keycloakService.userId;
    
    // Check if senderId is valid
    if (!senderId) {
      console.error('Sender ID is not available. Cannot create chat.');
      // Try to get user ID directly from keycloak-angular service as fallback
      const keycloakInstance = (this.keycloakService as any).keycloakAngularService?.keycloakInstance;
      if (keycloakInstance?.tokenParsed?.sub) {
        console.log('Using fallback user ID:', keycloakInstance.tokenParsed.sub);
      } else {
        console.error('Fallback user ID is also not available.');
        return;
      }
    }
    
    // Custom implementation with explicit query parameters
    const params = new HttpParams()
      .set('sender-id', senderId)
      .set('receiver-id', contact.id as string);
    
    const url = `${this.chatService.rootUrl}/api/v1/chats`;
    const context = new HttpContext();
    
    firstValueFrom(
      this.http.post<{response: string}>(url, null, { params, context })
    ).then((res) => {
      const chat: ChatResponse = {
        id: res.response,
        name: contact.firstName + ' ' + contact.lastName,
        recipientOnline: contact.online,
        lastMessageTime: contact.lastSeen,
        senderId: senderId,
        receiverId: contact.id
      };
      this.chats().unshift(chat);
      this.searchNewContact = false;
      this.chatSelected.emit(chat);
    }).catch(error => {
      console.error('Error creating chat:', error);
    });
  }

  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);
  }

  wrapMessage(lastMessage: string | undefined): string {
    if (lastMessage && lastMessage.length <= 20) {
      return lastMessage;
    }
    return lastMessage?.substring(0, 17) + '...';
  }
}