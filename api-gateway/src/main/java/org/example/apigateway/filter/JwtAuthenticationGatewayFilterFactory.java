package org.example.apigateway.filter;

import org.example.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

  private final JwtUtil jwtUtil;

  // Constructor único - NO uses @RequiredArgsConstructor aquí
  public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
    super(Config.class);
    this.jwtUtil = jwtUtil;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getPath().toString();

      // Skip JWT validation for public endpoints
      if (isPublicEndpoint(path)) {
        return chain.filter(exchange);
      }

      // Extract JWT token
      String token = extractToken(request);
      if (token == null) {
        return unauthorized(exchange, "Missing authorization token");
      }

      // Validate token
      if (!jwtUtil.validateToken(token)) {
        return unauthorized(exchange, "Invalid token");
      }

      // Add user info to headers for microservices
      ServerHttpRequest modifiedRequest = addUserHeaders(request, token);

      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/actuator") ||
            path.equals("/api/auth/validate");
  }

  private String extractToken(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private ServerHttpRequest addUserHeaders(ServerHttpRequest request, String token) {
    String email = jwtUtil.getEmailFromToken(token);
    String role = jwtUtil.getRoleFromToken(token);

    return request.mutate()
            .header("X-User-Email", email)
            .header("X-User-Role", role)
            .build();
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  public static class Config {
    // Clase de configuración vacía
  }
}