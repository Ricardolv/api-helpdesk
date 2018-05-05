package com.richard.apihelpdesk.security.model;

import com.richard.apihelpdesk.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CurrentUser {

	private String token;
	private User user;
}
