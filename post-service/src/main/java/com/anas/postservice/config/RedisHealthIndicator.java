package com.anas.postservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try {
            // Try to get a connection to Redis
            redisConnectionFactory.getConnection().close();
            return Health.up()
                    .withDetail("provider", "Redis")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("provider", "Redis")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}