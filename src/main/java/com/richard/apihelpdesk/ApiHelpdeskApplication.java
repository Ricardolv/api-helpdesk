package com.richard.apihelpdesk;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.richard.apihelpdesk.entity.User;
import com.richard.apihelpdesk.entity.enums.ProfileEnum;
import com.richard.apihelpdesk.repository.UserRepository;

@SpringBootApplication
public class ApiHelpdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiHelpdeskApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			initUsers(userRepository, passwordEncoder);
		};
	}

	private void initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		User admin = new User();
		admin.setEmail("admin@teste.com");
		admin.setPassword(passwordEncoder.encode("admin"));
		admin.setProfile(ProfileEnum.ROLE_ADMIN);
		
		Optional<User> find = userRepository.findByEmail("admin@teste.com");
	
		if (!find.isPresent()) {
			userRepository.save(admin);
		}
	}
}
