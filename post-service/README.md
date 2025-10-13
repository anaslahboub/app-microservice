# Post Service

## Overview

The Post Service is a microservice responsible for managing social media posts within the chat microservices ecosystem. It provides a complete set of features for creating, retrieving, updating, and interacting with posts, including likes, comments, bookmarks, and content moderation.

## Key Features

1. **Post Management**
   - Create posts with text content and image attachments
   - Retrieve posts with pagination support
   - Update post status (approve/reject/pending)
   - Delete posts
   - Pin important posts

2. **Social Interactions**
   - Like/unlike posts
   - Comment on posts with nested replies
   - Bookmark/unbookmark posts
   - View trending posts

3. **Content Organization**
   - Search through approved posts
   - Filter posts by author
   - View bookmarked posts
   - Manage pending posts for moderation

4. **Security & Access Control**
   - JWT-based authentication with Keycloak integration
   - Role-based authorization
   - CORS configuration for web clients

5. **Performance & Scalability**
   - Rate limiting to prevent abuse
   - Caching with Redis
   - File serving for uploaded content
   - Pagination for efficient data retrieval

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/posts` | POST | Create a new post |
| `/api/v1/posts` | GET | Get all posts |
| `/api/v1/posts/my-posts` | GET | Get posts created by the authenticated user |
| `/api/v1/posts/trending` | GET | Get trending posts |
| `/api/v1/posts/my-pending` | GET | Get pending posts created by the authenticated user |
| `/api/v1/posts/{post-id}/status` | PATCH | Update post status |
| `/api/v1/posts/{post-id}/pin` | PATCH | Pin/unpin a post |
| `/api/v1/posts/{post-id}` | DELETE | Delete a post |
| `/api/v1/posts/{post-id}/like` | POST | Toggle like on a post |
| `/api/v1/posts/{post-id}/liked` | GET | Check if post is liked by user |
| `/api/v1/posts/{post-id}/bookmark` | POST | Toggle bookmark on a post |
| `/api/v1/posts/{post-id}/bookmarked` | GET | Check if post is bookmarked by user |
| `/api/v1/posts/bookmarks` | GET | Get bookmarked posts by user |
| `/api/v1/posts/search` | GET | Search through posts |
| `/api/v1/posts/{postId}/comments` | POST | Add a comment to a post |
| `/api/v1/posts/{postId}/comments` | GET | Get comments with replies |
| `/api/v1/posts/{postId}/comments/paginated` | GET | Get paginated main comments |



## Configuration Files Explanation

### 1. CacheConfig.java

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        // Uses configured ObjectMapper with JavaTimeModule
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager(
                "posts", "trendingPosts", "pinnedPosts", "userPosts",
                "pendingPosts", "groupPosts", "searchResults", "groupSearchResults"
        );
    }
}
```

**Why we use it:**
- Implements a dual caching strategy with Redis as primary and in-memory as fallback
- Improves performance by caching frequently accessed data (posts, trending posts, etc.)
- Provides graceful degradation when Redis is unavailable
- Uses @ConditionalOnProperty to allow flexible deployment configurations

**How it's helpful:**
- Reduces database load by caching responses
- Improves response times for frequently requested data
- Ensures service availability even when Redis is down
- Allows configurable cache expiration (10 minutes) to keep data fresh

### 2. FeignConfig.java

```java
@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getDetails();
                template.header("Authorization", "Bearer " + jwt.getTokenValue());
            }
        };
    }
}
```

**Why we use it:**
- Configures Feign clients for inter-service communication
- Handles error decoding for better error messages
- Automatically adds JWT tokens to outgoing requests

**How it's helpful:**
- Simplifies communication with other microservices
- Ensures secure communication by propagating authentication tokens
- Provides better error handling for service-to-service calls

### 3. JacksonConfig.java

```java
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

**Why we use it:**
- Configures JSON serialization/deserialization globally
- Handles Java 8 time types properly

**How it's helpful:**
- Ensures consistent date/time formatting in API responses
- Prevents issues with JSON serialization of LocalDateTime objects
- Provides a single source of truth for JSON configuration

### 4. JwtAuthConverter.java

```java
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Merges standard roles + Keycloak realm roles
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRealmRoles(jwt).stream()
        ).collect(Collectors.toSet());

        // Creates authentication token with extracted authorities
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    /**
     * Gets the claim representing the user (here the "sub" = unique identifier)
     */
    private String getPrincipalClaimName(Jwt jwt) {
        return jwt.getClaim(JwtClaimNames.SUB);
    }

    /**
     * Extracts Keycloak roles located in "realm_access.roles"
     */
    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return Set.of();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");

        // Creates a Spring Security authority for each role
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
```

**Why we use it:**
- Bridges Keycloak JWT tokens with Spring Security
- Extracts user roles from Keycloak-specific JWT structure

**How it's helpful:**
- Enables role-based access control using Keycloak roles
- Simplifies authentication across the microservice ecosystem
- Provides a standardized way to handle JWT tokens

### 5. OpenApiConfig.java

```java
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 🔐 Tells Swagger to use your Bearer security scheme for all requests
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT token obtained from Keycloak: 'Bearer eyJ...'")))
                .info(new Info()
                        .title("Group Service API")
                        .version("1.0")
                        .description("API for managing educational groups secured with Keycloak")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
```

**Why we use it:**
- Configures Swagger/OpenAPI documentation
- Integrates security documentation with JWT

**How it's helpful:**
- Provides interactive API documentation
- Makes it easy for developers to test endpoints
- Documents security requirements for API usage

### 6. RateLimitingConfig.java

```java
@Configuration
public class RateLimitingConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public Map<String, Bucket> buckets() {
        return buckets;
    }

    public Bucket newBucket() {
        // Allow 100 requests per minute
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> newBucket());
    }

    public String getClientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
```

**Why we use it:**
- Implements API rate limiting to prevent abuse
- Protects the service from denial-of-service attacks

**How it's helpful:**
- Ensures fair usage of the API
- Prevents individual users from overwhelming the service
- Maintains service availability for all users

### 7. RedisConfig.java

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }
}
```

**Why we use it:**
- Configures Redis connection and serialization
- Sets up RedisTemplate for programmatic Redis access

**How it's helpful:**
- Enables efficient data storage and retrieval in Redis
- Provides JSON serialization for complex objects
- Supports transactions for data consistency

### 8. RedisHealthIndicator.java

```java
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            String response = (String) redisTemplate.execute((RedisCallback<String>) connection -> 
                connection.ping()
            );
            if ("PONG".equals(response)) {
                return Health.up().withDetail("redis", "Available").build();
            } else {
                return Health.down().withDetail("redis", "Unexpected response: " + response).build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("redis", "Connection failed: " + e.getMessage()).build();
        }
    }
}
```

**Why we use it:**
- Provides health checks for Redis connectivity
- Integrates with Spring Boot Actuator

**How it's helpful:**
- Enables monitoring of Redis availability
- Helps with troubleshooting connectivity issues
- Supports graceful degradation when Redis is down

### 9. SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.requestMatchers("/auth/**",
                            "/v2/api-docs",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-resources",
                            "/swagger-resources/**",
                            "/configuration/ui",
                            "/configuration/security",
                            "/swagger-ui/**",
                            "/webjars/**",
                            "/swagger-ui.html",
                            "/ws/**",
                            "/post-uploads/**")  // Allow access to uploaded files
                    .permitAll()
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200","http://localhost:8082","http://localhost:8083"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:8082","http://localhost:8083"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Why we use it:**
- Configures comprehensive security for the service
- Integrates with Keycloak for authentication
- Sets up CORS for web client access

**How it's helpful:**
- Protects all endpoints with JWT authentication
- Allows necessary public endpoints (docs, health checks)
- Prevents CSRF attacks with stateless session management
- Enables web client access with proper CORS configuration

### 10. WebConfig.java

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

**Why we use it:**
- Provides additional CORS configuration
- Ensures consistent cross-origin resource sharing

**How it's helpful:**
- Allows web clients to access the API from different origins
- Supports all HTTP methods needed by the application
- Enables credentials for authenticated requests

### 11. WebMvcConfig.java

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Value("${application.file.uploads.post-output-path:./post-uploads}")
    private String fileUploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/posts/**")
                .addPathPatterns("/api/v1/files/**");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure the path ends with a slash and is properly formatted for URLs
        Path path = Paths.get(fileUploadPath).toAbsolutePath().normalize();
        File directory = path.toFile();
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Convert to proper file URL format
        String fileLocation = "file:///" + directory.getAbsolutePath().replace("\\", "/") + "/";
        
        System.out.println("Serving static files from: " + fileLocation);
        
        // Serve files from the post-uploads directory
        registry.addResourceHandler("/post-uploads/**")
                .addResourceLocations(fileLocation);
    }
}
```

**Why we use it:**
- Registers interceptors for rate limiting
- Configures static resource handling for uploaded files

**How it's helpful:**
- Applies rate limiting to API endpoints
- Enables serving of uploaded files through HTTP
- Automatically creates upload directories if needed

### 12. WebSocketConfig.java

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
```

**Why we use it:**
- Configures WebSocket support for real-time communication
- Sets up STOMP messaging protocol

**How it's helpful:**
- Enables real-time features like notifications
- Provides a standardized way to handle WebSocket connections
- Supports both topics (broadcast) and queues (direct messaging)

## Dependencies

The service uses the following key technologies:
- **Spring Boot**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **PostgreSQL**: Primary database
- **Redis**: Caching layer
- **Keycloak**: Identity and access management
- **Swagger/OpenAPI**: API documentation
- **Bucket4j**: Rate limiting
- **Eureka**: Service discovery

## Environment Variables

The service requires the following environment configurations:
- Database connection details (URL, username, password)
- Keycloak server details for JWT validation
- Redis server details for caching
- File storage path for uploaded content

See [application.yml](file:///d:/Project/app-chat-microservices/post-service/src/main/resources/application.yml) for detailed configuration options.