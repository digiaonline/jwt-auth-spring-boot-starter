package com.starcut.auth.jwt.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityNotFoundException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.starcut.auth.common.CustomUserToken;
import com.starcut.auth.common.RoleProviderI;
import com.starcut.auth.common.db.UserI;
import com.starcut.auth.common.jwt.JwtAuthenticatedUser;
import com.starcut.auth.jwt.db.RevokedTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SignatureException;

public class JwtAuthenticationWithUserProviderFilter<T extends UserProviderI<U>, U extends UserI> extends GenericFilterBean  {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationWithUserProviderFilter.class);

	public static final String JWT_TOKEN_HEADER_PARAM = "X-Authorization";

	String jwtSecret;

	T userProvider;

	RoleProviderI<U> roleProvider;

	RevokedTokenRepository revokedTokenRepository;

	public JwtAuthenticationWithUserProviderFilter(JwtAuthorizationProvider provider, T userProvider, RoleProviderI<U> roleProvider)
			throws InstantiationException, IllegalAccessException {
		this.jwtSecret = provider.getJwtSecret();
		this.userProvider = userProvider;
		this.revokedTokenRepository = provider.getRevokedTokenRepository();
		this.roleProvider = roleProvider;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			chain.doFilter(request, response);
			return;
		}

		if (httpRequest.getRequestURI().startsWith("/auth")) {
			chain.doFilter(request, response);
			return;
		}

		String headerParam = httpRequest.getHeader(JWT_TOKEN_HEADER_PARAM);
		String jwt;
		try {
			JwtTokenExtractor extractor = new JwtTokenExtractor();
			jwt = extractor.extract(headerParam);
		} catch (AuthenticationServiceException e) {
			LOGGER.debug("No token provided");
			chain.doFilter(request, response);
			return;
		}

		try {
			Jws<Claims> token = Jwts.parser().setSigningKey(jwtSecret.getBytes("UTF-8")).parseClaimsJws(jwt);
			if (revokedTokenRepository.existsById(token.getBody().getId())) {
				LOGGER.error("Trying to use a revoked token: " + token.getBody().getId());
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			String subject = token.getBody().getSubject();
			U user = userProvider.findByToken(token);

			Map<String, String> claims = new HashMap<>();
			for (Entry<String, Object> entry : token.getBody().entrySet()) {
				if (entry.getValue() instanceof String) {
					claims.put(entry.getKey(), (String) entry.getValue());
				} else if (entry.getValue() instanceof Integer || entry.getValue() instanceof Long) {
					claims.put(entry.getKey(), "" + entry.getValue());
				}
			}
			JwtAuthenticatedUser<U> authenticatedUser = new JwtAuthenticatedUser<U>(subject, null, jwt, claims);
			SecurityContextHolder.getContext().setAuthentication(new CustomUserToken<JwtAuthenticatedUser<U>>(
					authenticatedUser, roleProvider.getGrantedAuthoritiesFor(user)));

			LOGGER.info("Subject " + subject + " has been authenticated. Giving "
					+ roleProvider.getGrantedAuthoritiesFor(user).toString() + " roles.");
		} catch (SignatureException e) {
			LOGGER.error("Invalid signature for the token: " + jwt);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (ExpiredJwtException e) {
			LOGGER.error("Expired token: " + jwt);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (EntityNotFoundException e) {
			LOGGER.error("Cannot find the user with the principal present in the token: " + jwt);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (MalformedJwtException e) {
			LOGGER.error("Token is not a JWT: " + jwt);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		} catch (PrematureJwtException e) {
			LOGGER.error("The transfer is not completed yet", e);
			httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		chain.doFilter(request, response);
	}

}