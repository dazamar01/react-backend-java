package com.backend.security;

import static com.backend.security.SecurityConstants.HEADER_STRING;
import static com.backend.security.SecurityConstants.ROLE_PREFIX;
import static com.backend.security.SecurityConstants.SECRET;
import static com.backend.security.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	public JWTAuthorizationFilter(AuthenticationManager authManager) {
		super(authManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String header = req.getHeader(HEADER_STRING);

		if (header == null || !header.startsWith(TOKEN_PREFIX)) {
			chain.doFilter(req, res);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

		String token = request.getHeader(HEADER_STRING);

		if (token != null) {

			String jwt = token.replace(TOKEN_PREFIX, "");

			// parse the token.
			String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build().verify(jwt).getSubject();

			Claim rol = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build().verify(jwt).getClaim("scopes");

			if (user != null) {
				
				// System.out.println("ROL: " + rol.asString());

				List<GrantedAuthority> grantedAuthorities = AuthorityUtils
						.commaSeparatedStringToAuthorityList(rol.asString());

				return new UsernamePasswordAuthenticationToken(user, null, grantedAuthorities);

			}
			return null;
		}
		return null;
	}

}
