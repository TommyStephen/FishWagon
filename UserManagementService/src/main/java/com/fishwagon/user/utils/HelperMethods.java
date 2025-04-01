package com.fishwagon.user.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HelperMethods {

	public static String bCryptPassword(String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);
        System.out.println("Hashed password: " + hashedPassword);
        return hashedPassword;
		
	}
}
