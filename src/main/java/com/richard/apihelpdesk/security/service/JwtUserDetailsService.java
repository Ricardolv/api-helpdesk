package com.richard.apihelpdesk.security.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.richard.apihelpdesk.entity.User;
import com.richard.apihelpdesk.security.jwt.JwtUserFactory;
import com.richard.apihelpdesk.service.UserService;

@Service
public class JwtUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Optional<User> userOptional = userService.findByEmail(email);
		
		if (!userOptional.isPresent()) {
			throw new UsernameNotFoundException(String.format("No user found wiht username '%s'.", email));
		} else {
			return JwtUserFactory.create(userOptional.get());
		}
		
	}

}
