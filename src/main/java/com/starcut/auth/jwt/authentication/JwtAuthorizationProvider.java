package com.starcut.auth.jwt.authentication;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.starcut.auth.jwt.db.JwtRecordRepository;
import com.starcut.auth.jwt.db.RevokedTokenRepository;
import com.starcut.auth.jwt.db.entity.JwtRecord;
import com.starcut.auth.jwt.db.entity.RevokedToken;
import com.starcut.auth.jwt.exception.NonUniqueJtiException;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtAuthorizationProvider {

	@Autowired
	JwtRecordRepository jwtRecordRepository;

	@Autowired
	RevokedTokenRepository revokedTokenRepository;

	@Value("${starcut.auth.jwt.secret}")
	String jwtSecret;

	@Value("${starcut.auth.jwt.lifetime-in-hours}")
	Integer jwtLifetimeInHours;

	public String getJwtSecret() {
		return jwtSecret;
	}

	public String produceJwt(String subject) throws UnsupportedEncodingException, NonUniqueJtiException {
		return produceJwt(subject, UUID.randomUUID().toString(), Instant.now());
	}

	public String produceJwt(String subject, Map<String, Object> claims)
			throws UnsupportedEncodingException, NonUniqueJtiException {
		return produceJwt(subject, UUID.randomUUID().toString(), Instant.now(), claims);
	}

	public String produceJwt(String subject, String jti) throws UnsupportedEncodingException, NonUniqueJtiException {
		return produceJwt(subject, jti, Instant.now());
	}

	public String produceJwt(String subject, String jti, Map<String, Object> claims)
			throws UnsupportedEncodingException, NonUniqueJtiException {
		return produceJwt(subject, jti, Instant.now(), claims);
	}

	public String produceJwt(String subject, String jti, Instant nbf)
			throws UnsupportedEncodingException, NonUniqueJtiException {

		return produceJwt(subject, jti, nbf, new HashMap<String, Object>());
	}

	private void storeJti(String subject, String jti, Instant expiration) throws NonUniqueJtiException {
		if (jwtRecordRepository.existsById(jti)) {
			throw new NonUniqueJtiException();
		}

		JwtRecord jwtRecord = new JwtRecord();
		jwtRecord.setJti(jti);
		jwtRecord.setSubject(subject);
		jwtRecord.setExpiration(expiration);
		jwtRecordRepository.saveAndFlush(jwtRecord);

	}

	public String produceJwt(String subject, String jti, Instant nbf, Map<String, Object> claims)
			throws UnsupportedEncodingException, NonUniqueJtiException {
		Date expirationDate = Date.from(Instant.now().plus(Duration.ofHours(jwtLifetimeInHours)));
		storeJti(subject, jti, expirationDate.toInstant());
		JwtBuilder jwtBuilder = Jwts.builder().setId(jti).setSubject(subject).setIssuedAt(Date.from(Instant.now()))
				.addClaims(claims).setNotBefore(Date.from(nbf))
				.signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes("UTF-8"));
		if (jwtLifetimeInHours > 0) {
			jwtBuilder = jwtBuilder.setExpiration(expirationDate);
		}
		return jwtBuilder.compact();
	}

	public String produceJwtWithSecret(String subject, String jti, String secret, Instant expire)
			throws UnsupportedEncodingException, NonUniqueJtiException {
		storeJti(subject, jti, expire);
		JwtBuilder jwtBuilder = Jwts.builder().setId(jti).setSubject(subject).setIssuedAt(Date.from(Instant.now()))
				.signWith(SignatureAlgorithm.HS256, secret.getBytes("UTF-8"));
		if (expire != null) {
			jwtBuilder = jwtBuilder.setExpiration(Date.from(expire));
		}
		return jwtBuilder.compact();
	}

	public void revokeAllTokensForSubject(String subject) {
		List<JwtRecord> jwtRecords = jwtRecordRepository.findBySubject(subject);
		List<RevokedToken> revokedTokens = new ArrayList<>();
		for (JwtRecord jwtRecord : jwtRecords) {
			RevokedToken revokedToken = new RevokedToken();
			revokedToken.setJti(jwtRecord.getJti());
			revokedToken.setExpiredAt(jwtRecord.getExpiration());
			revokedTokens.add(revokedToken);
		}
		revokedTokenRepository.saveAll(revokedTokens);
	}

	public RevokedTokenRepository getRevokedTokenRepository() {
		return revokedTokenRepository;
	}
}
