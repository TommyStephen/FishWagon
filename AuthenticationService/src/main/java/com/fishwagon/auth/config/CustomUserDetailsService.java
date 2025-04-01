package com.fishwagon.auth.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.fishwagon.auth.feignclient.UserClient;
import com.fishwagon.auth.dto.UserAuthDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserClient userClient;

    public CustomUserDetailsService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserAuthDTO user = userClient.getUserByUsername(username);
            if (user == null) {
                logger.error("User not found: {}", username);
                throw new UsernameNotFoundException("User not found: " + username);
            }

            // ✅ Fetch role name dynamically
            String roleName = userClient.getRoleNameById(user.getRoleId());
            System.out.println("🔍 Fetched Role: " + roleName);

            // ✅ Remove "ROLE_" prefix before assigning it to Spring Security
            String springRoleName = roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
            
            return User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(springRoleName)  // ✅ Use role name without "ROLE_"
                    .build();
        } catch (Exception e) {
            logger.error("Error fetching user {} from UserService: {}", username, e.getMessage());
            throw new UsernameNotFoundException("UserService unavailable: " + e.getMessage());
        }
    }

}

