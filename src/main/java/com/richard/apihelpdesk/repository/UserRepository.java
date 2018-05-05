package com.richard.apihelpdesk.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.richard.apihelpdesk.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
	
	Optional<User> findByEmail(String email);

}
