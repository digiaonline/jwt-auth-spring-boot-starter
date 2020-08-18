package com.starcut.auth.jwt.db.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class JwtRecord {

	@Id
	private String jti;

	private String subject;

	private Instant expiration;

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Instant getExpiration() {
		return expiration;
	}

	public void setExpiration(Instant expiration) {
		this.expiration = expiration;
	}

}
