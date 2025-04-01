package com.fishwagon.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    // Create a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Constructor injection of dependencies
    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Log the request URI for debugging
        String requestURI = request.getRequestURI();
        logger.debug("Processing request for URI: {}", requestURI);
        logger.debug("Request Method: {}", request.getMethod());
        logger.debug("Authorization Header: {}", request.getHeader("Authorization"));

        // Skip authentication for public endpoints
        if (requestURI.startsWith("/users/public/")) {
            logger.debug("Public endpoint detected, skipping authentication: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        try {
            // Retrieve the Authorization header
            String authHeader = request.getHeader("Authorization");
            
            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header for URI: {}", requestURI);
                sendUnauthorizedError(response, "Missing or Invalid Authorization Header");
                return;
            }

            // Log (safely) that a valid header was found
            logger.debug("Valid Authorization header found for URI: {}", requestURI);

            // Extract the JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Extract username from token
            String username = jwtUtil.extractUsername(token);
            logger.debug("User Name Extracted is "+username);

            // Proceed with authentication if username is present and no existing authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Comprehensive token validation
                if (jwtUtil.validateToken(token, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                            );

                    // Set authentication details
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    logger.debug("User authenticated: {}", username);
                } else {
                    logger.warn("Token validation failed for user: {}", username);
                }
            }

            // Continue the filter chain
            chain.doFilter(request, response);

        } catch (Exception e) {
            // Catch and log any unexpected exceptions
            logger.error("Error in JWT authentication process", e);
            sendUnauthorizedError(response, "Authentication Failed");
        }
    }

    /**
     * Send a structured unauthorized error response
     * 
     * @param response HttpServletResponse
     * @param message Error message
     * @throws IOException if writing to response fails
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"status\":\"error\",\"message\":\"%s\"}", 
            message
        ));
    }
}














































































































//package com.fishwagon.user.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//    private final UserDetailsService userDetailsService;
//
//    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
//        this.jwtUtil = jwtUtil;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String requestURI = request.getRequestURI();
//
//        // ✅ Allow public endpoints
//        if (requestURI.startsWith("/users/public/")) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // 🔒 Get JWT token from Authorization header
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Missing or Invalid Authorization Header");
//            return;
//        }
//        System.out.println("*******************"+authHeader);
//
//        String token = authHeader.substring(7); // Remove "Bearer " prefix
//        String username = jwtUtil.extractUsername(token);
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//            if (jwtUtil.validateToken(token, userDetails.getUsername())) {
//                // ✅ If token is valid, create authentication token
//                UsernamePasswordAuthenticationToken authenticationToken =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            }
//        }
//
//        chain.doFilter(request, response);
//    }
//}
