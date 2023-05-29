package com.demo.ecommerce.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.ecommerce.models.User;
import com.demo.ecommerce.services.UserAuthService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserAuthService userAuthService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// get jwt token

		// String requestTokenHeader = request.getHeader("Authorization");

		String requestTokenHeader = request.getHeader("JWT");

		User user = null;

		String username = null;

		String jwtToken = null;

		// whether token starts with Bearer

		// if(requestTokenHeader!=null && requestTokenHeader.startsWith("Bearer ")) {
		if (requestTokenHeader != null) {

			// jwtToken = requestTokenHeader.substring(7);
			jwtToken = requestTokenHeader;

			try {

				// username = jwtUtil.extractUsername(jwtToken);

				user = jwtUtil.getUser(jwtToken);

				if (user != null) {
					username = user.getUsername();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			// validate the token

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						user, null, user.getAuthorities());

				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			} else {
				System.out.println("Token is not validated");
			}
		}
		// now this request can be forwarded
		filterChain.doFilter(request, response);
	}
}
