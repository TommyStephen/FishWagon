package com.fishwagon.auth.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter is a custom security filter that intercepts incoming HTTP requests 
 * to validate JWT tokens and authenticate users.
 * 
 * It extends OncePerRequestFilter to ensure that this filter is 
 * executed once per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for JwtAuthenticationFilter.
     *
     * @param jwtUtil            
     * Utility class for handling JWT operations such as parsing 
     * and validation.
     * @param userDetailsService 
     * Service to load user details based on username.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters incoming requests to check for a valid JWT token 
     * in the Authorization header.
     * If a valid token is found, it sets the authentication 
     * in the SecurityContext.
     *
     * @param request  Incoming HTTP request.
     * @param response Outgoing HTTP response.
     * @param chain    Filter chain to pass the request to the 
     * next filter.
     * @throws ServletException If an error occurs during filtering.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Retrieve the Authorization header from the request
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the Authorization header is missing or does 
        //not start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	// Proceed with the next filter in the chain
            chain.doFilter(request, response); 
            return;
        }

        // Extract the JWT token by removing the "Bearer " prefix
        String token = authHeader.substring(7);

        try {
            // Extract username from the token
            String username = jwtUtil.extractUsername(token);

            // Load user details from the database or in-memory storage
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token against the user details
            if (jwtUtil.validateToken(token, userDetails)) {
                // Set authentication in the SecurityContext to authorize the request
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                );
            }

        } catch (Exception e) {
            // Log authentication failure and return an Unauthorized response
            System.err.println("JWT authentication failed: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid JWT Token");
            return;
        }

        // Continue the request processing
        chain.doFilter(request, response);
    }
}
