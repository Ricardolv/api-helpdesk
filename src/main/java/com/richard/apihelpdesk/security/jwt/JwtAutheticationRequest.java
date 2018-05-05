package com.richard.apihelpdesk.security.jwt;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class JwtAutheticationRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@NonNull
	private String email;
	
	@NonNull
	private String password;
	
	
	
	

}
