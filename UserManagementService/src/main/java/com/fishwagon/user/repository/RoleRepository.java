package com.fishwagon.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fishwagon.user.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findById(Long id);  // ✅ Get role by ID
}

