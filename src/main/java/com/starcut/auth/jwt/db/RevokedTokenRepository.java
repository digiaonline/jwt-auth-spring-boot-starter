package com.starcut.auth.jwt.db;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starcut.auth.jwt.db.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

	public void deleteByExpirationLessThan(Instant now);
}
