# Chat Service - Real-Time Messaging Platform

A robust, scalable real-time messaging service built with Spring Boot that enables secure one-to-one chat communication with media sharing capabilities.

## 🌟 Key Features

- 🔐 **Secure Authentication** - OAuth2/JWT integration with Keycloak
- 💬 **Real-time Messaging** - WebSocket with STOMP protocol
- 📁 **Media Support** - Text, images, audio, and video messaging
- 🔄 **User Synchronization** - Automatic user profile updates from JWT claims
- 📱 **RESTful API** - Clean, well-documented endpoints
- 🛡️ **Message Status Tracking** - Sent/Seen indicators
- 📊 **Online Presence** - Real-time user availability detection

## 🏗️ Architecture Overview

```mermaid
graph TD
    A[Client Applications] --> B{API Gateway}
    B --> C[Chat Service]
    C --> D[(PostgreSQL)]
    C --> E[Keycloak Auth]
    C --> F[WebSocket Broker]
    
    subgraph "Chat Service"
        direction TB
        C --> G[REST Controllers]
        C --> H[Business Services]
        C --> I[Data Repositories]
        C --> J[WebSocket Layer]
        C --> K[Security Layer]
    end
    
    subgraph "External Services"
        D
        E
    end
```

## 📦 Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: OAuth2 Resource Server, JWT
- **Real-time**: WebSocket + STOMP
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

## 🧱 Core Domain Model

```mermaid
classDiagram
    class User {
        +String id
        +String firstName
        +String lastName
        +String email
        +LocalDateTime lastSeen
        +isUserOnline()
    }
    
    class Chat {
        +String id
        +getChatName()
        +getUnreadMessages()
        +getLastMessage()
        +getLastMessageTime()
    }
    
    class Message {
        +Long id
        +String content
        +MessageType type
        +MessageState state
        +String senderId
        +String receiverId
        +String mediaFilePath
    }
    
    class MessageType {
        <<enumeration>>
        TEXT
        IMAGE
        VIDEO
        AUDIO
    }
    
    class MessageState {
        <<enumeration>>
        SENT
        SEEN
    }
    
    User "1" --> "many" Chat : sends
    User "1" --> "many" Chat : receives
    Chat "1" --> "many" Message
```

## 🔄 Service Communication Flow

```mermaid
graph LR
    A[Controllers] --> B[Services]
    B --> C[Repositories]
    B --> D[External Services]
    C --> E[(Database)]
    D --> F[File Storage]
    D --> G[WebSocket]
    
    style A fill:#ffe4c4,stroke:#333
    style B fill:#e6e6fa,stroke:#333
    style C fill:#98fb98,stroke:#333
    style D fill:#87ceeb,stroke:#333
    style E fill:#ffb6c1,stroke:#333
    style F fill:#dda0dd,stroke:#333
    style G fill:#f0e68c,stroke:#333
```

## 🎯 Component Interaction Patterns

### 1. User Authentication & Synchronization

```mermaid
sequenceDiagram
    participant Client
    participant Security
    participant Filter
    participant UserService
    participant UserRepository
    participant Keycloak
    
    Client->>Security: HTTP Request + JWT
    Security->>Filter: Validate Token
    Filter->>UserService: synchronizeWithIdp(JWT)
    UserService->>Keycloak: Validate Claims
    UserService->>UserRepository: findOrCreate(User)
    UserRepository-->>UserService: User Entity
    UserService-->>Filter: Sync Complete
    Filter-->>Security: Continue
```

### 2. Message Delivery Flow

```mermaid
sequenceDiagram
    participant ClientA
    participant MessageController
    participant MessageService
    participant MessageRepository
    participant NotificationService
    participant ClientB
    
    ClientA->>MessageController: POST /messages
    MessageController->>MessageService: saveMessage()
    MessageService->>MessageRepository: save(Message)
    MessageRepository-->>MessageService: Saved Message
    MessageService->>NotificationService: sendNotification()
    NotificationService->>ClientB: WebSocket /user/{id}/chat
    MessageService-->>MessageController: Success
    MessageController-->>ClientA: 201 Created
```

### 3. Chat Creation Process

```mermaid
sequenceDiagram
    participant Client
    participant ChatController
    participant ChatService
    participant ChatRepository
    participant UserRepository
    
    Client->>ChatController: POST /chats
    ChatController->>ChatService: createChat()
    ChatService->>ChatRepository: findExistingChat()
    alt Chat Exists
        ChatRepository-->>ChatService: Existing Chat
    else New Chat
        ChatService->>UserRepository: findUsers()
        UserRepository-->>ChatService: Sender & Receiver
        ChatService->>ChatRepository: save(new Chat)
        ChatRepository-->>ChatService: Saved Chat
    end
    ChatService-->>ChatController: Chat ID
    ChatController-->>Client: Response
```

## 📡 WebSocket Communication

```mermaid
graph LR
    A[WebSocketConfig] --> B[STOMP Endpoint /ws]
    B --> C[Message Broker]
    C --> D[User Destinations]
    
    subgraph "Notification Types"
        E[New Message]
        F[Message Seen]
        G[Media Shared]
    end
    
    E --> D
    F --> D
    G --> D
```

## 🗃️ Data Flow Architecture

```mermaid
graph TB
    subgraph "REQUEST LAYER"
        A[REST Controllers]
    end
    
    subgraph "BUSINESS LOGIC"
        B[Service Layer]
    end
    
    subgraph "DATA ACCESS"
        C[Repositories]
        D[(PostgreSQL)]
    end
    
    subgraph "EXTERNAL INTEGRATIONS"
        E[File Storage]
        F[WebSocket Broker]
        G[OAuth2 Provider]
    end
    
    A --> B
    B --> C
    C --> D
    B --> E
    B --> F
    A --> G
```

## 🔧 Class Relationships Map

```mermaid
graph LR
    A[Controllers] --> B[Services]
    B --> C[Repositories]
    B --> D[Mappers]
    B --> E[Utils]
    C --> F[Entities]
    
    subgraph "API Layer"
        A
    end
    
    subgraph "Business Layer"
        B
    end
    
    subgraph "Data Layer"
        C
        D
        E
        F
    end
    
    style A fill:#87ceeb
    style B fill:#98fb98
    style C fill:#ffb6c1
    style D fill:#dda0dd
    style E fill:#f0e68c
    style F fill:#ffe4c1
```

## 🚀 REST API Endpoints

### User Management
```
GET /api/v1/users              # Get all users except self
```

### Chat Management
```
POST /api/v1/chats             # Create/reuse chat
GET /api/v1/chats              # Get user's chats
```

### Message Management
```
POST /api/v1/messages          # Send text message
POST /api/v1/messages/upload-media  # Upload media
PATCH /api/v1/messages         # Mark messages as seen
GET /api/v1/messages/chat/{id} # Get chat messages
```

## 🔐 Security Flow

```mermaid
graph LR
    A[Incoming Request] --> B[Security Filter]
    B --> C[JWT Validation]
    C --> D[Role Extraction]
    D --> E[User Sync]
    E --> F[Controller]
    
    subgraph "Security Chain"
        B
        C
        D
        E
    end
```

## 📁 File Storage Architecture

```mermaid
graph TD
    A[MessageService] --> B[FileService]
    B --> C[Local Storage]
    C --> D[Uploads Directory]
    
    A --> E[MessageRepository]
    E --> F[Database]
    F --> G[Message.mediaFilePath]
    
    subgraph "Storage Components"
        C
        F
    end
```

## 📊 Service Layer Responsibilities

### UserService
- User profile management
- User listing and filtering
- Online status calculation

### ChatService
- Chat session creation
- Chat history retrieval
- Unread message counting

### MessageService
- Message persistence
- Media file handling
- Notification dispatching
- Message state management

### NotificationService
- WebSocket message broadcasting
- User-specific routing
- Real-time delivery

## ⚙️ Configuration Highlights

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chat-db
    username: ${DB_USERNAME:anas}
    password: ${DB_PASSWORD:anas}
  jpa:
    hibernate:
      ddl-auto: update
```

### WebSocket Configuration
```yaml
websocket:
  endpoint: /ws
  allowed-origins: http://localhost:4200
```

### File Upload Settings
```yaml
application:
  file:
    uploads:
      media-output-path: ./uploads
spring:
  servlet:
    multipart:
      max-file-size: 50MB
```

## 🏃 Running the Service

### Prerequisites
1. Java 21+
2. PostgreSQL database
3. Keycloak or OAuth2 provider

### Quick Start
```bash
# Navigate to chat-service directory
cd chat-service

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/chat-service-0.0.1-SNAPSHOT.jar
```

### Docker Deployment
```bash
# Build Docker image
docker build -t chat-service .

# Run container
docker run -p 8080:8080 chat-service
```

## 🧪 Testing Endpoints

### Swagger UI
Once running, access API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### WebSocket Testing
Connect to WebSocket endpoint:
```
ws://localhost:8080/ws
```

## 📈 Performance Considerations

### Database Optimization
- Proper indexing on user_id and chat_id columns
- Eager loading for frequently accessed relationships
- Connection pooling configuration

### WebSocket Scalability
- User-specific message routing
- Efficient payload serialization
- Connection lifecycle management

### File Storage
- Local file system for development
- Cloud storage (S3, Azure Blob) for production
- CDN integration for media delivery

## 🔧 Troubleshooting

### Common Issues
1. **Database Connection**: Verify PostgreSQL is running and credentials are correct
2. **JWT Validation**: Ensure Keycloak is accessible and issuer URL matches
3. **WebSocket CORS**: Check allowed origins in WebSocket configuration
4. **File Uploads**: Verify upload directory permissions

### Health Checks
```bash
# Application health
GET /actuator/health

# Database connectivity
GET /actuator/health/db

# WebSocket status
GET /actuator/health/websocket
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [WebSocket with Spring](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---
*This documentation provides a comprehensive overview of the chat service architecture, component interactions, and operational guidelines.*