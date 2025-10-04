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
    
    // ========================================
    // STEP 3: HTTP REQUEST PREPARATION
    // ========================================
    
    // Create HTTP parameters for the chat creation API call
    // These parameters identify who is starting the chat (sender) and who will receive it (receiver)
    const params = new HttpParams()
      .set('sender-id', senderId)        // Current user's ID
      .set('receiver-id', contact.id as string);  // Selected contact's ID
    
    // Construct the API endpoint URL for creating a new chat
    const url = `${this.chatService.rootUrl}/api/v1/chats`;
    
    // Create HTTP context (currently empty, but can be used for additional request metadata)
    const context = new HttpContext();
    
    // ========================================
    // STEP 4: API CALL & RESPONSE HANDLING
    // ========================================
    
    // Make HTTP POST request to create the chat
    // firstValueFrom converts the Observable to a Promise for easier handling
    firstValueFrom(
      this.http.post<{response: string}>(url, null, { params, context })
    ).then((res) => {
      // ========================================
      // STEP 5: CHAT OBJECT CREATION
      // ========================================
      
      // Create a new ChatResponse object with the returned chat ID and contact information
      const chat: ChatResponse = {
        id: res.response,                                    // Chat ID returned from the API
        name: contact.firstName + ' ' + contact.lastName,   // Display name for the chat
        recipientOnline: contact.online,                    // Online status of the recipient
        lastMessageTime: contact.lastSeen,                  // Last seen timestamp
        senderId: senderId,                                 // Current user's ID
        receiverId: contact.id                              // Contact's ID
      };
      
      // ========================================
      // STEP 6: UI STATE UPDATES
      // ========================================
      
      // Add the new chat to the beginning of the chats list (most recent first)
      this.chats().unshift(chat);
      
      // Hide the contact search interface since chat has been created
      this.searchNewContact = false;
      
      // Emit event to notify parent component that a chat has been selected/created
      // This allows the parent to switch to the chat view or perform other actions
      this.chatSelected.emit(chat);
      
    }).catch(error => {
      // ========================================
      // STEP 7: ERROR HANDLING
      // ========================================
      
      // Log any errors that occur during chat creation
      console.error('Error creating chat:', error);
    });
  }

  /**
   * Handles click events on existing chat items in the chat list
   * When a user clicks on an existing chat, this function notifies the parent component
   * @param chat - The chat object that was clicked
   */
  chatClicked(chat: ChatResponse) {
    // ========================================
    // EMIT CHAT SELECTION EVENT
    // ========================================
    
    // Emit the selected chat to the parent component
    // This allows the parent to switch to the chat view or perform other actions
    this.chatSelected.emit(chat);
  }

  /**
   * Formats and truncates the last message preview for display in the chat list
   * This ensures consistent message preview lengths in the UI
   * @param lastMessage - The last message text to format (can be undefined)
   * @returns Formatted message string with ellipsis if truncated
   */
  wrapMessage(lastMessage: string | undefined): string {
    // ========================================
    // STEP 1: VALIDATE MESSAGE EXISTENCE
    // ========================================
    
    // Check if message exists and is within the acceptable length (20 characters or less)
    if (lastMessage && lastMessage.length <= 20) {
      // Return the message as-is if it's short enough
      return lastMessage;
    }
    
    // ========================================
    // STEP 2: TRUNCATE LONG MESSAGES
    // ========================================
    
    // For longer messages, truncate to 17 characters and add ellipsis
    // This creates a consistent preview length of 20 characters total (17 + '...')
    return lastMessage?.substring(0, 17) + '...';
  }
}