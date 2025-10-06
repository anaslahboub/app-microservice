package com.anas.groupservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                            "/ws/**")
                    .permitAll()
                .requestMatchers( "/api/groups").hasAuthority("TEACHER")
                .requestMatchers( "/api/groups/**").hasAuthority("TEACHER")
//                .requestMatchers(HttpMethod.DELETE, "/api/groups/**").hasAuthority("TEACHER")
//                .requestMatchers(HttpMethod.POST, "/api/groups/*/members").hasAuthority("TEACHER")
//                .requestMatchers(HttpMethod.DELETE, "/api/groups/*/members/*").hasAuthority("TEACHER")
//                .requestMatchers(HttpMethod.POST, "/api/groups/*/members/*/co-admin").hasAuthority("TEACHER")
//                .requestMatchers(HttpMethod.GET, "/api/groups/**").authenticated()
//                .requestMatchers(HttpMethod.POST, "/api/groups/*/members/*/leave").authenticated()
//                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasAuthority("ADMIN")
//                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasAuthority("ADMIN")
//                .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasAuthority("ADMIN")
//                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasAuthority("ADMIN")
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
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200","http://localhost:8082"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:8082"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}