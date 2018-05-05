package com.richard.apihelpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.richard.apihelpdesk.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
	
	User findByEmail(String email);

}
