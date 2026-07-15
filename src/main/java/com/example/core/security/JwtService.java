package com.example.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

// codes here are from JwtTokenProvider
@Service
public class JwtService {

  private static final String SECRET_BASE64 =
      "YmFzZTY0LWVuY29kZWQtc3VwZXItc2VjcmV0LWtleS1mb3Itand0LXNhbXBsZS0yMDI2";
  private static final long EXPIRATION_TIME_MS = 3600000; // 1 hour

  public String generateToken(UserDetails user) {
    String username = user.getUsername();
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

    // Optional: Custom claims you want to embed inside the token (e.g.,
    // roles/permissions)
    Map<String, Object> extraClaims = new HashMap<>();
    extraClaims.put("role", "ROLE_USER");

    return Jwts.builder()
        .claims(extraClaims) // Set custom payloads (claims)
        .subject(username) // Set standard 'sub' claim
        .issuedAt(now) // Set standard 'iat' claim
        .expiration(expiryDate) // Set standard 'exp' claim
        .signWith(getSigningKey(), Jwts.SIG.HS256) // Use new Jwts.SIG registry for signing
        .compact(); // Convert into the final dot-separated string
  }

  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, UserDetails user) {
    try {
      Claims claims = extractAllClaims(token);
      // Verify token isn't expired
      return !claims.getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      // Token is either expired, has an invalid signature, or is completely corrupted
      System.err.println("Invalid JWT token: " + e.getMessage());
      return false;
    }
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey()) // Provide the exact key used to sign the token
        .build() // Create the parser
        .parseSignedClaims(token) // Verify signature and parse the JWS
        .getPayload(); // Retrieve the claims payload
  }
}
