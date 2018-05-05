package com.richard.apihelpdesk.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.richard.apihelpdesk.entity.User;

public interface UserService {
	
	User findByEmail(String email);
	
	User createOrUpdate(User user);
	
	Optional<User> findById(String userId);
	
	void delete(String id);
	
	Page<User> findAll(int page, int count);

}
