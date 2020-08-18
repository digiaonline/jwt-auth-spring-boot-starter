package com.starcut.auth.jwt.db.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RevokedToken {

	@Id
	private String jti;
	
	private Instant expiration;

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public Instant getExpiration() {
		return expiration;
	}

	public void setExpiredAt(Instant expiration) {
		this.expiration = expiration;
	}
	
}
