package org.example.apigateway.config;

import org.example.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder, JwtAuthenticationFilter filter) {
    return builder.routes()
            // User Service - auth endpoints públicos
            .route("user-service-auth", r -> r.path("/api/auth/login", "/api/auth/register")
                    .uri("http://localhost:8081")) // ← URL directa

            // User Service - otros endpoints protegidos
            .route("user-service", r -> r.path("/api/users/**", "/api/auth/validate")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8081"))

            // Club Service - protegido
            .route("club-service", r -> r.path("/api/clubs/**")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8086"))

            // Court Service - protegido
            .route("court-service", r -> r.path("/api/courts/**")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8085"))

            // Reservation Service - protegido
            .route("reservation-service", r -> r.path("/api/reservations/**")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8082"))

            // Payment Service - protegido
            .route("payment-service", r -> r.path("/api/payments/**")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8083"))

            // Notification Service - protegido
            .route("notification-service", r -> r.path("/api/notifications/**")
                    .filters(f -> f.filter(filter))
                    .uri("http://localhost:8084"))
            .build();
  }
}