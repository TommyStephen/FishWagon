package com.fishwagon.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for handling JWT operations such as token creation, 
 * validation, and extraction of claims.
 * This class provides methods for generating JWT tokens and 
 * validating user authentication.
 */
@Component
public class JwtUtil {

    // Secret key used for signing JWT tokens 
	//(must be long enough for HMAC SHA key)
    private static final String SECRET_KEY = "your-secret-key-must-be-long-enough"; 

    // Generate a secure signing key using the HMAC SHA algorithm
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    private String token; // Holds the generated token

    /**
     * Generates a JWT token for the given UserDetails.
     * The token includes user roles and an expiration time of 1 hour.
     *
     * @param userDetails The authenticated user's details.
     * @return The generated JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Fetch roles (authorities) from UserDetails and 
        //store them in a list
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        List<String> roleNames = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Add roles to the claims map
        claims.put("roles", roleNames);

        // Build the JWT token with subject (username), claims, 
        //issue date, expiration, and signature
        token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // User identifier
                .setIssuedAt(new Date()) // Token issue time
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // ✅ 1-hour expiry
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256) // Securely sign the token
                .compact();

        return token;
    }

    /**
     * Extracts all claims (payload) from a given JWT token.
     *
     * @param token The JWT token.
     * @return A Claims object containing the extracted data.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY) // Use the signing key to validate the token
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token The JWT token.
     * @return The username stored in the token.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates a given JWT token against user details.
     * It checks if the token's username matches the user and if 
     * the token has expired.
     *
     * @param token       The JWT token to validate.
     * @param userDetails The user details for verification.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Extract the username from the token
            String username = extractUsername(token);

            // Validate the token against the username and expiration
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        } catch (ExpiredJwtException e) { // Handle expired tokens
            System.err.println("Token expired: " + e.getMessage());
        } catch (SecurityException e) { // ✅ Catch security-related issues (Invalid signature)
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (MalformedJwtException e) { // ✅ Catch malformed tokens
            System.err.println("Malformed JWT: " + e.getMessage());
        } catch (UnsupportedJwtException e) { // ✅ Catch unsupported JWT structures
            System.err.println("Unsupported JWT: " + e.getMessage());
        } catch (IllegalArgumentException e) { // ✅ Catch empty claims or null tokens
            System.err.println("Invalid JWT arguments: " + e.getMessage());
        } catch (Exception e) { // Catch any other unknown exceptions
            System.err.println("JWT validation failed: " + e.getMessage());
        }
        return false; // Return false if validation fails
    }

    /**
     * Checks if the JWT token has expired.
     *
     * @param token The JWT token.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
