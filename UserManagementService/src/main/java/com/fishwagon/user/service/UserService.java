package com.fishwagon.user.service;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fishwagon.user.dto.UserRegistrationDTO;
import com.fishwagon.user.model.Address;
import com.fishwagon.user.model.Role;
import com.fishwagon.user.model.UserEntity;
import com.fishwagon.user.repository.AddressRepository;
import com.fishwagon.user.repository.RoleRepository;
import com.fishwagon.user.repository.UserRepository;
import com.fishwagon.user.utils.HelperMethods;

import jakarta.transaction.Transactional;

@Service
public class UserService {
	
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, AddressRepository addressRepository,
    		RoleRepository roleRepository) {
        this.userRepository = userRepository;
		this.addressRepository = addressRepository;
		this.roleRepository = roleRepository;
    }
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional
    public UserRegistrationDTO saveUser(UserRegistrationDTO req) {
        // Encrypt password
        String password = HelperMethods.bCryptPassword(req.getPassword());
        // Validate Role Before Assigning
        Role role = roleRepository.findById(req.getRoleId())
            .orElseThrow(() -> new RuntimeException("Role not found!")); // 🚀 Ensure role exists

        // Save UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(req.getUsername());
        userEntity.setPassword(password);
        userEntity.setMobile(req.getMobile());
        userEntity.setProfilePic(req.getProfilePic());
        userEntity.setCreatedAt(LocalDate.now());
        userEntity.setRoleId(role.getId()); // ✅ Use existing role

        UserEntity savedUser = userRepository.save(userEntity);

        // Save Address
        Address address = new Address();
        address.setId(req.getAddressId());
        address.setUserId(savedUser.getId());
        address.setCity(req.getCity());
        address.setCountry(req.getCountry());
        address.setPostalCode(req.getPostalCode());
        address.setState(req.getState());
        address.setStreet(req.getStreet());

        Address savedAddress = addressRepository.save(address);

        // Construct response DTO
        UserRegistrationDTO response = new UserRegistrationDTO();
        response.setId(savedUser.getId());
        response.setAddressId(savedAddress.getId());
        response.setRoleId(role.getId());
        response.setRoleName(role.getRoleName()); // ✅ Include role name in response
        response.setUsername(savedUser.getUsername());
        response.setMobile(savedUser.getMobile());
        response.setProfilePic(savedUser.getProfilePic());
        response.setCity(savedAddress.getCity());
        response.setCountry(savedAddress.getCountry());
        response.setStreet(savedAddress.getStreet());
        response.setState(savedAddress.getState());
        response.setPostalCode(savedAddress.getPostalCode());

        return response;
    }


    @Transactional
    public void deleteUser(Long userId) {
        // Delete child records (addresses) first
        addressRepository.deleteByUserId(userId);
        
        // Now delete the user
        userRepository.deleteById(userId);
    }
}
