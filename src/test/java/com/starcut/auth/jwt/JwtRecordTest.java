package com.starcut.auth.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.starcut.auth.jwt.authentication.JwtAuthorizationProvider;
import com.starcut.auth.jwt.db.JwtRecordRepository;
import com.starcut.auth.jwt.db.RevokedTokenRepository;
import com.starcut.auth.jwt.db.entity.JwtRecord;
import com.starcut.auth.jwt.exception.NonUniqueJtiException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtRecordTest extends JwtAuthSpringBootStarterApplicationTests {

	@Autowired
	private JwtAuthorizationProvider jwtAuthorizationProvider;

	@Autowired
	private JwtRecordRepository jwtRecordRepository;

	@Autowired
	private RevokedTokenRepository revokedTokenRepository;

	@Test
	public void testProduceJwt() throws UnsupportedEncodingException, NonUniqueJtiException {
		String jwt = jwtAuthorizationProvider.produceJwt("test", "1");
		assertNotNull(jwt);
		assertFalse(jwt.isEmpty());
	}

	@Test
	public void testProduceJwtIsRecorded() throws UnsupportedEncodingException, NonUniqueJtiException {
		jwtAuthorizationProvider.produceJwt("test2", "2");
		Optional<JwtRecord> jwtRecordOpt = jwtRecordRepository.findById("2");
		assertTrue(jwtRecordOpt.isPresent());
		JwtRecord jwtRecord = jwtRecordOpt.get();
		assertEquals("2", jwtRecord.getJti());
		assertEquals("test2", jwtRecord.getSubject());
	}

	@Test
	public void testRevokeAllBySubject() throws UnsupportedEncodingException, NonUniqueJtiException {
		jwtAuthorizationProvider.produceJwt("test3", "3");
		jwtAuthorizationProvider.produceJwt("test3", "4");
		jwtAuthorizationProvider.produceJwt("test3", "5");
		jwtAuthorizationProvider.revokeAllTokensForSubject("test3");
		assertTrue(revokedTokenRepository.existsById("3"));
		assertTrue(revokedTokenRepository.existsById("4"));
		assertTrue(revokedTokenRepository.existsById("5"));
	}

	@Test(expected = NonUniqueJtiException.class)
	public void testJtiUniqueness() throws UnsupportedEncodingException, NonUniqueJtiException {
		jwtAuthorizationProvider.produceJwt("test", "jti");
		jwtAuthorizationProvider.produceJwt("test", "jti");
	}

}
