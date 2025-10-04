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
  // ========================================
  // COMPONENT PROPERTIES
  // ========================================
  
  /**
   * Input signal containing the list of existing chats
   * This is populated by the parent component and displays all user conversations
   */
  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  
  /**
   * Boolean flag to control the visibility of the contact search interface
   * When true, shows the list of available contacts to start new chats
   */
  searchNewContact = false;
  
  /**
   * Array containing all available users/contacts that can be selected for new chats
   * This is populated when the user searches for contacts
   */
  contacts: Array<UserResponse> = [];
  
  /**
   * Output event emitter that notifies the parent component when a chat is selected
   * This allows the parent to switch to the chat view or perform other actions
   */
  chatSelected = output<ChatResponse>();

 
  constructor(
    private chatService: Api,
    private userService: Api,
    private keycloakService: KeycloakService,
    private http: HttpClient
  ) {
  }

  /**
   * Initiates a search for available contacts/users to start new chats with
   * This function fetches all users from the system and displays them for selection
   */
  searchContact() {

    this.userService.invoke(getAllUsers, {
    }).then(
   
      
      (users) => {
        this.contacts = users;
        this.searchNewContact = true;
      }
    );
  }

  /**
   * Creates a new chat conversation between the current user and a selected contact
   * @param contact - The user contact to start a chat with
   */
  selectContact(contact: UserResponse) {
    console.log('Keycloak service:', this.keycloakService);
    console.log('Keycloak instance:', this.keycloakService.keycloak);
    console.log('User ID:', this.keycloakService.userId);
    
    const senderId = this.keycloakService.userId;
    

    if (!senderId) {
      console.error('Sender ID is not available. Cannot create chat.');
  
      const keycloakInstance = (this.keycloakService as any).keycloakAngularService?.keycloakInstance;
      if (keycloakInstance?.tokenParsed?.sub) {
        console.log('Using fallback user ID:', keycloakInstance.tokenParsed.sub);
        // Note: In a real implementation, you might want to assign this to senderId
      } else {
        console.error('Fallback user ID is also not available.');
        return; // Exit function if no user ID is available
      }
    }
    

    const params = new HttpParams()
      .set('sender-id', senderId)        // Current user's ID
      .set('receiver-id', contact.id as string);  // Selected contact's ID
    
    const url = `${this.chatService.rootUrl}/api/v1/chats`;
    
    // Create HTTP context (currently empty, but can be used for additional request metadata)
    const context = new HttpContext();
    
    // firstValueFrom converts the Observable to a Promise for easier handling
    firstValueFrom(
      this.http.post<{response: string}>(url, null, { params, context })
    ).then((res) => {
      
      const chat: ChatResponse = {
        id: res.response,                                    // Chat ID returned from the API
        name: contact.firstName + ' ' + contact.lastName,   // Display name for the chat
        recipientOnline: contact.online,                    // Online status of the recipient
        lastMessageTime: contact.lastSeen,                  // Last seen timestamp
        senderId: senderId,                                 // Current user's ID
        receiverId: contact.id                              // Contact's ID
      };
      
      this.chats().unshift(chat);
      this.searchNewContact = false;
      this.chatSelected.emit(chat);
      
    }).catch(error => {
      console.error('Error creating chat:', error);
    });
  }

  /**
   * Handles click events on existing chat items in the chat list
   * When a user clicks on an existing chat, this function notifies the parent component
   * @param chat - The chat object that was clicked
   */
  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);
  }

  /**
   * Formats and truncates the last message preview for display in the chat list
   * This ensures consistent message preview lengths in the UI
   * @param lastMessage - The last message text to format (can be undefined)
   * @returns Formatted message string with ellipsis if truncated
   */
  wrapMessage(lastMessage: string | undefined): string {

    if (lastMessage && lastMessage.length <= 20) {
      return lastMessage;
    }
    return lastMessage?.substring(0, 17) + '...';
  }
}