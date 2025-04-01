package com.fishwagon.user.dto;

public class UserAuthDTO {

	private String username;
    private String password;  // ✅ Include hashed password for authentication
    private Long roleId;  // ✅ Include role for authorization

    public UserAuthDTO() {
		
	}
    public void setUsername(String username) {
		this.username = username;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
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
