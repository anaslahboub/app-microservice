# Chat Microservices Application

A comprehensive microservices-based social platform built with Spring Boot and Angular, featuring real-time messaging, group management, and social media capabilities.

## 🌟 Overview

This project is a full-featured microservices application that provides a complete social platform experience with real-time communication capabilities. The system is composed of multiple specialized services that work together to deliver a seamless user experience.

## 🏗️ Architecture

The application follows a microservices architecture pattern with the following key components:

### Backend Services
- **Chat Service**: Real-time one-to-one messaging with media sharing
- **Group Service**: Educational group management with member administration
- **Post Service**: Social media functionality with posts, likes, and comments
- **Discovery Service**: Eureka server for service discovery and registration
- **Gateway Service**: API gateway for routing and cross-cutting concerns

### Frontend
- **Angular Application**: Modern web interface for accessing all platform features

### Infrastructure
- **PostgreSQL**: Primary database for data persistence
- **Keycloak**: Identity and access management
- **Redis**: Caching layer for improved performance
- **Docker**: Containerization for easy deployment

## 🚀 Key Features

### Real-time Communication
- Secure one-to-one messaging
- Media sharing (images, audio, video)
- Online presence detection
- Message status tracking (sent/seen)

### Social Platform
- Post creation and sharing
- Like and comment functionality
- Bookmarking system
- Trending content discovery

### Group Management
- Educational group creation and administration
- Member management with role-based access
- Group posts and discussions
- Search and statistics

### Security & Authentication
- OAuth2/JWT integration with Keycloak
- Role-based access control
- Secure API endpoints

### Performance & Scalability
- Service discovery with Eureka
- API gateway for request routing
- Caching with Redis
- Rate limiting for abuse prevention

## 📦 Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: OAuth2 Resource Server, JWT
- **Real-time**: WebSocket + STOMP
- **Build Tool**: Maven
- **Service Discovery**: Eureka
- **API Gateway**: Spring Cloud Gateway

### Frontend
- **Framework**: Angular 20.2.1
- **Language**: TypeScript
- **Build Tool**: Angular CLI

### Infrastructure
- **Containerization**: Docker
- **Database**: PostgreSQL
- **Identity Management**: Keycloak
- **Caching**: Redis

## 🏃 Running the Application

### Prerequisites
- Java 21+
- Node.js 18+
- Docker and Docker Compose
- Maven 3.6+

### Quick Start
1. Start the infrastructure services:
   ```bash
   docker-compose up -d
   ```

2. Start each microservice individually:
   ```bash
   # In each service directory (chat-service, group-service, etc.)
   ./mvnw spring-boot:run
   ```

3. Start the Angular frontend:
   ```bash
   cd angular-app
   npm install
   ng serve
   ```

4. Access the application at `http://localhost:4200`

## 📁 Project Structure

```
app-chat-microservices/
├── angular-app/          # Angular frontend application
├── chat-service/         # Real-time messaging service
├── discovery-service/    # Eureka service discovery
├── gitway-service/       # API gateway service
├── group-service/        # Educational group management
├── post-service/         # Social media posts service
├── docker-compose.yml    # Infrastructure services configuration
└── README.md             # This file
```

## 📈 Service Communication

The microservices communicate through:
- **REST APIs** for synchronous communication
- **WebSocket** for real-time features
- **Service Discovery** via Eureka
- **API Gateway** for centralized access

## 🔧 Development Setup

1. Clone the repository
2. Start infrastructure services with Docker Compose
3. Configure each service with appropriate environment settings
4. Run services individually or as a complete system

## 📚 Additional Resources

Each microservice contains its own detailed README with specific implementation details, API documentation, and configuration guides.

---
*This project demonstrates a modern approach to building scalable, maintainable microservices applications with real-time capabilities.*