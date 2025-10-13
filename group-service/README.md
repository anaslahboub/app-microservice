# Group Service - Educational Group Management Platform

A comprehensive microservice for managing educational groups in a learning platform. It provides functionality for creating and managing groups, members, posts, and real-time notifications.

## 🌟 Key Features

- 🏫 **Group Management** - Create, update, archive, and delete educational groups
- 👥 **Member Management** - Add/remove members and designate co-administrators
- 📝 **Group Posts** - Share educational content within groups
- 🔐 **Role-based Access Control** - Teachers, students, and administrators with appropriate permissions
- 💬 **Real-time Notifications** - WebSocket support for instant updates
- 🔍 **Search & Statistics** - Find groups and analyze engagement metrics
- 🛡️ **Security** - OAuth2/JWT integration with Keycloak

## 🏗️ Architecture Overview

The Group Service is a microservice that manages educational groups, their members, and related content. It integrates with Keycloak for authentication, PostgreSQL for data persistence, and uses WebSocket for real-time notifications.

## 📦 Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: OAuth2 Resource Server, JWT
- **Real-time**: WebSocket + STOMP
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven
- **Service Discovery**: Eureka Client
- **API Communication**: OpenFeign

## 🚀 Getting Started

### Prerequisites
- Java 21+
- PostgreSQL database
- Keycloak or OAuth2 provider
- Maven 3.6+

### Running the Service

#### Using Maven
```bash
# Navigate to group-service directory
cd group-service

# Run with Maven
./mvnw spring-boot:run
```

#### Using Docker
```bash
# Build the JAR first
./mvnw clean package

# Build Docker image
docker build -t group-service .

# Run container
docker run -p 8082:8082 group-service
```

### Configuration
The service is configured through `application.yml`:

- **Port**: 8082
- **Database**: PostgreSQL connection details
- **Security**: OAuth2/JWT configuration
- **Eureka**: Service discovery settings

## 📡 API Endpoints

### Group Management
```
POST /api/groups                  # Create a new group
GET /api/groups                   # Get all groups
GET /api/groups/{id}              # Get a specific group
GET /api/groups/teacher/{teacherId} # Get groups by teacher
GET /api/groups/user/{userId}     # Get groups for a user
PUT /api/groups/{id}              # Update a group
DELETE /api/groups/{id}           # Delete a group
PUT /api/groups/{id}/archive      # Archive a group
POST /api/groups/search           # Search groups
```

### Member Management
```
POST /api/groups/{groupId}/members              # Add a member to a group
GET /api/groups/{groupId}/members               # Get all members of a group
DELETE /api/groups/{groupId}/members/{userId}   # Remove a member from a group
POST /api/groups/{groupId}/members/{userId}/leave # Leave a group
POST /api/groups/{groupId}/members/{userId}/co-admin # Designate co-admin
```

### Group Posts
```
POST /api/groups/{groupId}/posts                # Create a post in a group
GET /api/groups/{groupId}/posts                 # Get all posts in a group
PUT /api/groups/{groupId}/posts/{postId}        # Update a post
DELETE /api/groups/{groupId}/posts/{postId}     # Delete a post
```

### Statistics
```
GET /api/statistics/groups                      # Get overall group statistics
GET /api/statistics/groups/teacher              # Get statistics for teacher's groups
```

### WebSocket Endpoint
```
/ws                                             # WebSocket connection endpoint
```

## 📁 Project Structure

```
src/main/java/com/anas/groupservice/
├── config/           # Configuration classes (Security, WebSocket, OpenAPI)
├── controller/       # REST controllers for groups, members, posts, etc.
├── dto/              # Data Transfer Objects for API requests/responses
├── entity/           # JPA entities representing database tables
├── mapper/           # Object mapping utilities
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic services
├── util/             # Utility classes
└── GroupServiceApplication.java # Main application class
```

## 🔧 Key Components

### WebSocket Configuration
- Uses STOMP over WebSocket for real-time communication
- Configured with `/ws` endpoint
- Supports SockJS fallback
- Broadcasts to `/topic` and user-specific `/user` destinations

### Security
- OAuth2 Resource Server configuration
- JWT validation with Keycloak
- Role-based access control (Teacher, Student, Admin)

### File Storage
- Group file upload capability
- Configurable storage path
- Large file support

## 🧪 Testing

### API Documentation
Once running, access API documentation at:
```
http://localhost:8082/swagger-ui.html
```

### WebSocket Testing
Connect to WebSocket endpoint:
```
ws://localhost:8082/ws
```

## 📈 Service Integration

This service integrates with:
- **Keycloak**: For user authentication and authorization
- **PostgreSQL**: For data persistence
- **Eureka**: For service discovery in the microservice architecture
- **Other Services**: Through OpenFeign for inter-service communication

## 🛠️ Development Setup

1. Clone the repository
2. Configure `application.yml` with your environment settings
3. Run the service using Maven or Docker

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [WebSocket with Spring](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)