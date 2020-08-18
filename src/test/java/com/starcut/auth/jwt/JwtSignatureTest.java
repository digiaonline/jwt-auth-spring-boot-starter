package com.starcut.auth.jwt;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtSignatureTest {
	
	@Test
	public void testSignatureValidation() throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException, UnsupportedEncodingException {
		try {
			String signature = "SzWkcxXkTL8n4DmWD3wTfKbxwuTWZ8TX2BE5k9oc3ImsA/Inz6ysRYospM7hJpEwxWgOe";
			String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2OGVjMzJjMy0zZjUzLTRmNGEtYWQzMC1lYmQ3MDJiNTU0MjciLCJzdWIiOiIrMzU4NTA0NzgyMzc2IiwiaWF0IjoxNTY3NDExNjQxLCJ0ZWFtX3V1aWQiOiIwMjQ2MzNmZi1mNzBlLTRmNDYtYmI4ZS0wYzhkNjhhODA2NDMiLCJuYmYiOjE1Njc0MTE2NDB9.GmRsen4IkNtuigD32O_crJ4GCI8gmWsOPPS09cL2Jcs";
			Jwts.parser().setSigningKey(signature.getBytes("UTF-8")).parseClaimsJws(jwt);		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
