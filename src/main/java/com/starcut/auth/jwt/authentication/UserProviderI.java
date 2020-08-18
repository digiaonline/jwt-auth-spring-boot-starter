package com.starcut.auth.jwt.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface UserProviderI<U> {

	public U findByToken(Jws<Claims> token);
}
