package com.fishwagon.auth.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fishwagon.auth.dto.UserAuthDTO;

@FeignClient(name = "UserManagementService", url = "http://localhost:8082")  
public interface UserClient {
    @GetMapping("/users/public/findByUsername/{username}")
    UserAuthDTO getUserByUsername(@PathVariable String username);  // ✅ Return object directly
    
    @GetMapping("/users/public/getRoleName/{roleId}")
    String getRoleNameById(@PathVariable Long roleId);  // ✅ Fetch role name dynamically
}
