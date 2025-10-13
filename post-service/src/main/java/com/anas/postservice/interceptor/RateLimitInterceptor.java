package com.anas.postservice.interceptor;

import com.anas.postservice.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingConfig rateLimitingConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = rateLimitingConfig.getClientId();
        Bucket bucket = rateLimitingConfig.resolveBucket(clientId);

        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            long remainingTokens = bucket.getAvailableTokens();
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
            return true;
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", "60");
            response.getWriter().write("Too many requests. Please try again later.");
            return false;
        }
    }
}