package com.fishwagon.user.model;

import jakarta.persistence.*;


@Entity
@Table(name = "user_roles")

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roleName; 

	public Role() {
		super();
	}

	public Role(Long id, String name) {
		super();
		this.id = id;
		this.roleName = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setName(String name) {
		this.roleName = name;
	}
    
    
    
}

