package com.fishwagon.user.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class JwtUtil {
	 private static final String SECRET_KEY = "your-secret-key-must-be-long-enough"; 
	 private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    
    private final JwtConfig jwtConfig;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }
    
    public Key getSigningKey() {
        
        return SIGNING_KEY;
    }
    
    public String extractUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    
    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Extract userName from token
            String extractedUsername = extractUsername(token);
            
            // Validate userName matches and token is not expired
            return extractedUsername.equals(userDetails.getUsername()) 
                   && !isTokenExpired(token);
        } catch (Exception e) {
            // Log the validation error
            logger.error("Token validation failed", e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // ✅ Use `jwtUtil` method
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
