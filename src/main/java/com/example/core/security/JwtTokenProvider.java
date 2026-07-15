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

public class JwtTokenProvider {

  // IMPORTANT: Your secret key MUST be at least 256 bits (32 bytes) long for HS256.
  // This is a Base64-encoded representation of a sufficiently long key.
  private static final String SECRET_BASE64 =
      "YmFzZTY0LWVuY29kZWQtc3VwZXItc2VjcmV0LWtleS1mb3Itand0LXNhbXBsZS0yMDI2";

  // Token validity period (e.g., 1 hour in milliseconds)
  private static final long EXPIRATION_TIME_MS = 3600000;

  /**
   * Helper method to decode our Base64 secret key into a cryptographically secure SecretKey object.
   */
  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Generate a JWT Token for a specific user.
   *
   * @param username The subject/owner of the token
   * @return A signed, compact JWT string
   */
  public String generateToken(String username) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

    // Optional: Custom claims you want to embed inside the token (e.g., roles/permissions)
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

  /**
   * Parse and extract all claims from a given token. During the parsing phase, JJWT automatically
   * verifies the signature and expiration date.
   *
   * @param token The raw JWT string
   * @return The payload claims if valid
   * @throws JwtException if the token is altered, expired, or malformed
   */
  public Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey()) // Provide the exact key used to sign the token
        .build() // Create the parser
        .parseSignedClaims(token) // Verify signature and parse the JWS
        .getPayload(); // Retrieve the claims payload
  }

  /** Extract the username (subject) from the token. */
  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  /** Verify if a token is authentic and still active. */
  public boolean validateToken(String token) {
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
}
