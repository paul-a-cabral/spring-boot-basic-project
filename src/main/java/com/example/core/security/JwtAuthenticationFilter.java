package com.example.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

// Implementation of the JWT authentication filter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    logger.debug("Bearer token detected. Processing JWT authentication..");

    // "Bearer " is exactly 7 characters long (including the space).
    String jwt = authHeader.substring(7);
    String username = jwtService.extractUsername(jwt);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (username != null && authentication == null) {

      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (jwtService.isTokenValid(jwt, userDetails)) {

        // we'll create a new UsernamePasswordAuthenticationToken (this time
        // representing a successfully authenticated user), place it into the
        // SecurityContextHolder, and let the rest of Spring Security work exactly as if
        // the user had logged in with HTTP Basic.

        // Create an authentication token with the user's details and authorities
        Authentication authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // most production examples add one more line before calling setAuthentication().
        // it's commonly included because other components may use that metadata for auditing or
        // logging
        // authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Cleanup logic if needed
  }
}
