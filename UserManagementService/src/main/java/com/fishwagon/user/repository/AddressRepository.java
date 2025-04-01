package com.fishwagon.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fishwagon.user.model.Address;

import jakarta.transaction.Transactional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserId(Long userId);  // Fetch all addresses for a user
    
    //void deleteByUserId(Long userId);  
    
    /*The below method optimizes bulk delete operations by using a direct 
     * JPQL query instead of the default method.*/
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Address a WHERE a.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

