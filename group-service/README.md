# Group Service

This is a microservice for managing educational groups in a learning platform. It provides functionality for creating and managing groups, members, and real-time notifications.

## Features

### Group Management
- Teachers can create groups with name, description, and subject
- Teachers become automatic administrators of their groups
- Groups can be archived or deleted

### Member Management
- Admins can add/remove members to/from groups
- Members can voluntarily leave groups
- Admins can designate co-administrators

### Role-based Access Control
- Teachers have admin privileges for their groups
- Students have limited access to group information
- Security implemented with OAuth2/JWT via Keycloak

### Real-time Notifications
- WebSocket support for real-time updates
- Notifications for member changes, group updates, and archiving

### Search and Statistics
- Search groups by teacher, subject, or keywords
- Statistics on group activity and membership

## API Endpoints

### Group Management
- `POST /api/groups` - Create a new group
- `GET /api/groups` - Get all groups
- `GET /api/groups/{id}` - Get a specific group
- `GET /api/groups/teacher/{teacherId}` - Get groups by teacher
- `GET /api/groups/user/{userId}` - Get groups for a user
- `PUT /api/groups/{id}` - Update a group
- `DELETE /api/groups/{id}` - Delete a group
- `PUT /api/groups/{id}/archive` - Archive a group
- `POST /api/groups/search` - Search groups

### Member Management
- `POST /api/groups/{groupId}/members` - Add a member to a group
- `GET /api/groups/{groupId}/members` - Get all members of a group
- `DELETE /api/groups/{groupId}/members/{userId}` - Remove a member from a group
- `POST /api/groups/{groupId}/members/{userId}/leave` - Leave a group
- `POST /api/groups/{groupId}/members/{userId}/co-admin` - Designate co-admin

### Statistics
- `GET /api/statistics/groups` - Get overall group statistics
- `GET /api/statistics/groups/teacher` - Get statistics for teacher's groups

## WebSocket Endpoints
- `/ws` - WebSocket endpoint for real-time notifications
- `/topic/group/{groupId}` - Group-specific notifications
- `/user/{userId}/queue/notifications` - User-specific notifications

## Technologies Used
- Spring Boot 3.5.6
- PostgreSQL
- OAuth2/JWT with Keycloak
- WebSocket for real-time notifications
- JPA for data persistence

## Configuration
The service is configured through `application.yml`:
- Database connection settings
- OAuth2/JWT configuration for Keycloak
- WebSocket settings
- Server port (default: 8082)

## Running the service
