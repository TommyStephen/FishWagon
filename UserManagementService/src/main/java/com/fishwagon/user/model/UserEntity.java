package com.fishwagon.user.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;  // ✅ Store hashed password

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false)
    private String profilePic;

    private LocalDate createdAt;

    @Column(name = "role_id", nullable = false)
    private Long roleId;  // ✅ Store role ID as a simple field

    public UserEntity() {}

    public UserEntity(Long id, String username, String password, String mobile, String profilePic, Long roleId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
        this.profilePic = profilePic;
        this.roleId = roleId;
        this.createdAt = LocalDate.now();
    }

    public Long getId() {
        return id;
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

    public void setPassword(String password) {
        this.password = password;
    }

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	
}
