package com.richard.apihelpdesk.security.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.richard.apihelpdesk.entity.User;
import com.richard.apihelpdesk.entity.enums.ProfileEnum;

public class JwtUserFactory {
	
	private JwtUserFactory() {
		
	}

	public static JwtUser create(final User  user) {
		return new JwtUser(user.getId(), user.getEmail(), user.getPassword(), mapToGrantedAuthorities(user.getProfile()));
	}

	private static Collection<? extends GrantedAuthority> mapToGrantedAuthorities(final ProfileEnum profile) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(profile.toString()));
		return authorities;
	}
}
