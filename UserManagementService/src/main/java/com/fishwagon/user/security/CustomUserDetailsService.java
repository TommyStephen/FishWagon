package com.fishwagon.user.security;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fishwagon.user.model.Role;
import com.fishwagon.user.model.UserEntity;
import com.fishwagon.user.repository.RoleRepository;
import com.fishwagon.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        System.out.println("✅ Found User: " + user.getUsername());
        System.out.println("✅ Password from DB: " + user.getPassword());

        Optional<Role> roleOptional = roleRepository.findById(user.getRoleId());

        if (roleOptional.isPresent()) {
            System.out.println("✅ Fetched Role: " + roleOptional.get().getRoleName());
        } else {
            System.out.println("⚠️ Role ID " + user.getRoleId() + " not found in user_roles table!");
        }

        // ✅ Ensure role has "ROLE_" prefix
        String roleName = roleOptional.map(Role::getRoleName)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)  
                .orElseThrow(() -> new UsernameNotFoundException("⚠️ Role ID " + user.getRoleId() + " not found!"));

        return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority(roleName)));
    }
}
