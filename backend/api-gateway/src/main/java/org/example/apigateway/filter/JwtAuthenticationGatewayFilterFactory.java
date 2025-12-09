package org.example.apigateway.filter;

import org.example.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

  private final JwtUtil jwtUtil;

  private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
          "/api/auth/",
          "/api/courts",
          "/v3/api-docs",
          "/swagger-ui",
          "/actuator"
  );

  public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
    super(Config.class);
    this.jwtUtil = jwtUtil;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String path = request.getPath().toString();
      HttpMethod method = request.getMethod();

      // Saltar JWT para rutas públicas y OPTIONS
      if (isPublicEndpoint(path) || method == HttpMethod.OPTIONS) {
        return chain.filter(exchange);
      }

      // Extraer token
      String token = extractToken(request);
      if (token == null || !jwtUtil.validateToken(token)) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      // Agregar headers del usuario para microservicios
      ServerHttpRequest modifiedRequest = addUserHeaders(request, token);

      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    };
  }

  private boolean isPublicEndpoint(String path) {
    return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
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

  public static class Config {
    // Configuración vacía
  }
}
