package com.fishwagon.gateway.config;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public endpoints
        if (path.startsWith("/auth/public/") || path.startsWith("/users/public/") || path.equals("/.well-known/jwks.json")) {
            logger.debug("Skipping authentication for public path: {}", path);
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        try {
            // Extract username and validate token
            String username = jwtUtil.extractUsername(token);
            if (username != null) {
                // Extract roles from token
                Claims claims = jwtUtil.extractAllClaims(token);
                List<String> roles = claims.get("roles", List.class);
                
                if (roles == null || roles.isEmpty()) {
                    logger.warn("No roles found in token for user: {}", username);
                    return chain.filter(exchange);
                }
                
                // Convert roles to authorities
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            // Ensure role has ROLE_ prefix for Spring Security
                            String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                            return new SimpleGrantedAuthority(normalizedRole);
                        })
                        .collect(Collectors.toList());
                
                // Create authentication object
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                
                logger.debug("User authenticated: {} with roles: {}", username, roles);
                
                // Set authentication in security context
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
        }
        
        return chain.filter(exchange);
    }
}