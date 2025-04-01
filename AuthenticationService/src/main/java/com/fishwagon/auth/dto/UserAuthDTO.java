package com.fishwagon.auth.dto;

public class UserAuthDTO {
	
	private String username;
    private String password;  // ✅ Include hashed password for authentication
    private Long roleId;  // ✅ Include role for authorization

    public UserAuthDTO(String username, String password, Long roleId) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Long getRoleId() {
        return roleId;
    }

}
