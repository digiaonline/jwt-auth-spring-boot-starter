package com.starcut.auth.jwt.authentication;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenExtractor {  
	public static String HEADER_PREFIX = "Bearer ";

	public String extract(String header) {
		if (header == null || header.isEmpty()) {
			throw new AuthenticationServiceException("Authorization header cannot be blank!");
		}

		if (header.length() < HEADER_PREFIX.length()) {
			throw new AuthenticationServiceException("Invalid authorization header size.");
		}

		return header.substring(HEADER_PREFIX.length(), header.length());
	}
}