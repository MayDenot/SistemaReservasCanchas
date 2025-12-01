package org.example.microserviceuser.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider {
  private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

  private static final String AUTHORITIES_KEY = "auth";

  private final String secret;
  private final int tokenValidityInSeconds;
  private final SecretKey key;
  private final JwtParser jwtParser;

  public TokenProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.token-validity-in-seconds:86400}") int tokenValidityInSeconds) {
    this.secret = secret;
    this.tokenValidityInSeconds = tokenValidityInSeconds;

    log.info("Initializing TokenProvider with token validity: {} seconds", tokenValidityInSeconds);

    try {
      // Validar que la clave sea lo suficientemente larga
      if (this.secret.length() < 32) {
        log.warn("JWT secret is too short ({} characters). Minimum recommended is 32.", this.secret.length());
      }

      byte[] keyBytes = Decoders.BASE64.decode(this.secret);
      this.key = Keys.hmacShaKeyFor(keyBytes);
      this.jwtParser = Jwts.parser()
              .verifyWith(key)
              .build();

      log.info("TokenProvider initialized successfully");
    } catch (Exception e) {
      log.error("Failed to initialize TokenProvider: {}", e.getMessage());
      throw new RuntimeException("JWT configuration error", e);
    }
  }

  public String createToken(Authentication authentication) {
    String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    long now = System.currentTimeMillis();
    Date validity = new Date(now + this.tokenValidityInSeconds * 1000L);

    return Jwts.builder()
            .subject(authentication.getName())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, Jwts.SIG.HS512)
            .expiration(validity)
            .issuedAt(new Date(now))
            .compact();
  }

  public String generateToken(org.example.microserviceuser.entity.User user) {
    String authorities = user.getUserRole().name();

    long now = System.currentTimeMillis();
    Date validity = new Date(now + this.tokenValidityInSeconds * 1000L);

    return Jwts.builder()
            .subject(user.getEmail())
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, Jwts.SIG.HS512)
            .expiration(validity)
            .issuedAt(new Date(now))
            .compact();
  }

  public Authentication getAuthentication(String token) {
    try {
      Claims claims = jwtParser.parseSignedClaims(token).getPayload();

      Collection<? extends GrantedAuthority> authorities = Arrays
              .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
              .filter(auth -> !auth.trim().isEmpty())
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());

      User principal = new User(claims.getSubject(), "", authorities);

      return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    } catch (JwtException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      throw new RuntimeException("Invalid JWT token", e);
    }
  }

  public boolean validateToken(String authToken) {
    try {
      jwtParser.parseSignedClaims(authToken);
      return true;
    } catch (SecurityException e) {
      log.info("Invalid JWT signature.");
    } catch (MalformedJwtException e) {
      log.info("Invalid JWT token.");
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT token.");
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT token.");
    } catch (IllegalArgumentException e) {
      log.info("JWT token compact of handler are invalid.");
    }
    return false;
  }

  public String getEmailFromToken(String token) {
    try {
      Claims claims = jwtParser.parseSignedClaims(token).getPayload();
      return claims.getSubject();
    } catch (JwtException e) {
      log.warn("Cannot extract email from token: {}", e.getMessage());
      throw new RuntimeException("Cannot extract email from token", e);
    }
  }

  public String getRoleFromToken(String token) {
    try {
      Claims claims = jwtParser.parseSignedClaims(token).getPayload();
      return claims.get(AUTHORITIES_KEY).toString();
    } catch (JwtException e) {
      log.warn("Cannot extract role from token: {}", e.getMessage());
      throw new RuntimeException("Cannot extract role from token", e);
    }
  }
}