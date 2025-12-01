package org.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String jwtSecret;

  public boolean validateToken(String token) {
    try {
      Jwts.parser()
              .setSigningKey(jwtSecret)
              .build()
              .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public String getEmailFromToken(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.getSubject();
  }

  public String getRoleFromToken(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.get("auth", String.class);
  }
}
