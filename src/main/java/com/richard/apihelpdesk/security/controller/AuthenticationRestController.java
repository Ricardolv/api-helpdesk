package com.richard.apihelpdesk.security.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.richard.apihelpdesk.entity.User;
import com.richard.apihelpdesk.security.jwt.JwtAutheticationRequest;
import com.richard.apihelpdesk.security.jwt.JwtTokenUtil;
import com.richard.apihelpdesk.security.model.CurrentUser;
import com.richard.apihelpdesk.service.UserService;

@RestController
@CrossOrigin(origins = "*")
public class AuthenticationRestController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private UserService userService;
	
	@PostMapping("/api/auth")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAutheticationRequest autheticationRequest) {
		
		final Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(autheticationRequest.getEmail(), 
															autheticationRequest.getPassword()
					)
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		final UserDetails userDetails = userDetailsService.loadUserByUsername(autheticationRequest.getEmail());
		final String token = jwtTokenUtil.generateToken(userDetails);
		final User user = userService.findByEmail(autheticationRequest.getEmail()).get();
		user.setPassword(null);
		return ResponseEntity.ok(new CurrentUser(token, user));
	}
	
	@PostMapping("/api/refresh")
	public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		String username = jwtTokenUtil.getUserNameFromToken(token);
		final User user = userService.findByEmail(username).get();
		
		if (jwtTokenUtil.canTokenBeRefreshed(token)) {
			String refreshedToken = jwtTokenUtil.refreshedToken(token);
			return ResponseEntity.ok(new CurrentUser(refreshedToken, user));
		} else {
			return ResponseEntity.badRequest().body(null);
		}
	}
}
