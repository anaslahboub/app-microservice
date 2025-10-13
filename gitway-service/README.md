# Gitway Service

## Overview
Gitway Service is a Spring Cloud Gateway service that acts as an API gateway for the chat microservices application. It routes incoming requests to the appropriate microservices and handles cross-cutting concerns like authentication, logging, and rate limiting.

## Key Features
- **API Gateway**: Centralized entry point for all client requests
- **Service Discovery Integration**: Automatically discovers and routes to available microservices
- **Load Balancing**: Distributes requests across multiple instances of services
- **Eureka Client**: Registers with the Eureka discovery service

## Technology Stack
- **Spring Boot 3.3.4**
- **Spring Cloud Gateway**
- **Spring Cloud Netflix Eureka Client**
- **Java 21**

## Configuration
The service is configured through `application.properties`:

```properties
spring.application.name=gitway-service
eureka.instance.prefer-ip-address=true
server.port=7777
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

## Port
The service runs on port `7777`.

## Service Registration
This service registers itself with the Eureka discovery service running on `http://localhost:8761/eureka`.

## Main Components
1. **GitwayServiceApplication**: Main application class with Spring Boot entry point
2. **DiscoveryClientRouteDefinitionLocator**: Bean that enables automatic route discovery for registered services

## Dependencies
- `spring-cloud-starter-gateway`: Core gateway functionality
- `spring-cloud-starter-netflix-eureka-client`: Eureka client integration
- `spring-boot-starter-actuator`: Monitoring and management endpoints

## How It Works
1. The service starts on port 7777
2. Registers with the Eureka discovery service
3. Automatically discovers other services registered with Eureka
4. Routes incoming requests to appropriate services based on service name
5. Acts as a reverse proxy for all microservices in the system

## Route Configuration
Routes are automatically configured based on services registered with Eureka. For example:
- Requests to `/chat-service/**` are routed to the chat service
- Requests to `/post-service/**` are routed to the post service
- Requests to `/group-service/**` are routed to the group service

This eliminates the need for manual route configuration as services are automatically discovered and routed to.