package com.fishwagon.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fishwagon.user.dto.UserAuthDTO;
import com.fishwagon.user.dto.UserRegistrationDTO;
import com.fishwagon.user.model.UserEntity;
import com.fishwagon.user.repository.RoleRepository;
import com.fishwagon.user.repository.UserRepository;
import com.fishwagon.user.security.JwtFilter;
import com.fishwagon.user.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public UserController(RoleRepository roleRepository, UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // ✅ Public API (No Authentication Required)
    @GetMapping("/public/test")
    public String testUser() {
        return "User Management Service is working!";
    }

    // ✅ Public API to Get User by Username
    @GetMapping("/public/findByUsername/{username}")
    public ResponseEntity<UserAuthDTO> getUserByUsername(@PathVariable String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = userOptional.get();
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUsername(user.getUsername());
        userAuthDTO.setPassword(user.getPassword());
        userAuthDTO.setRoleId(user.getRoleId());

        return ResponseEntity.ok(userAuthDTO);
    }

    // 🔒 Restricted to ADMIN Only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/findAll")
    public List<UserEntity> getAllUsers() {
    	
    	logger.debug("INSIDE FIND ALL METHOD");
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Current Authentication: {}", authentication);
        logger.debug("User Principal: {}", authentication.getPrincipal());
        logger.debug("User Authorities: {}", authentication.getAuthorities());
        return userService.getAllUsers();
    }

    // 🔒 Restricted to ADMIN & MANAGER
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public Optional<UserEntity> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // ✅ Public API to Register User
    @PostMapping("/public/createUser")
    public UserRegistrationDTO createUser(@RequestBody UserRegistrationDTO req) {
        return userService.saveUser(req);
    }

    // 🔒 Only ADMIN Can Delete Users
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // ✅ Public API to Fetch Role Name by ID
    @GetMapping("/public/getRoleName/{roleId}")
    public ResponseEntity<String> getRoleNameById(@PathVariable Long roleId) {
        return roleRepository.findById(roleId)
                .map(role -> ResponseEntity.ok(role.getRoleName()))
                .orElse(ResponseEntity.notFound().build());
    }
}
