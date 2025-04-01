package com.fishwagon.auth.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration  // ✅ Marks this class as a Spring configuration class
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsService userDetailsService;
 // ✅ Custom JWT authentication filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter; 

    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures security settings such as authentication, 
     * session management, authorization rules, exception handling, 
     * and JWT authentication.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain...");

        http
        // ✅ Disables CSRF protection (for APIs, CSRF is usually disabled)    
        	.csrf(csrf -> csrf.disable())  
            .sessionManagement(session -> {
                logger.info("Setting session management to STATELESS.");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })  
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/public/**").permitAll()  
                // ✅ Public endpoints: No authentication required for these URLs
                .requestMatchers("/.well-known/jwks.json").permitAll() 
                .anyRequest().authenticated()  
                // ✅ All other requests require authentication
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {  
                    // ✅ Custom handling of unauthorized access (returns 401 Unauthorized)
                    logger.warn("Unauthorized access attempt: {}", authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized: " + authException.getMessage());
                })
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  
            // ✅ Adds JWT filter before Spring Security's UsernamePasswordAuthenticationFilter
            // This ensures requests are authenticated based on JWT before other authentication mechanisms.

        logger.info("Security filter chain configured successfully.");
        return http.build();  // ✅ Builds and returns the security filter chain
    }

    /**
     * Defines a password encoder bean that securely hashes passwords 
     * using the BCrypt algorithm.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        logger.info("Creating BCryptPasswordEncoder bean.");
        return new BCryptPasswordEncoder();  // ✅ Passwords are securely hashed using BCrypt
    }
    
    /**
     * Configures the AuthenticationManager to use DaoAuthenticationProvider, 
     * which retrieves user details from the database.
     */
    @Bean
    AuthenticationManager authenticationManager() {
        logger.info("Configuring AuthenticationManager with DaoAuthenticationProvider.");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);  // ✅ Load user details from database
        authProvider.setPasswordEncoder(passwordEncoder());  // ✅ Use BCrypt password encoder

        logger.info("AuthenticationManager setup complete.");
        return new ProviderManager(List.of(authProvider));  
        // ✅ ProviderManager manages authentication using DaoAuthenticationProvider
    }
}
